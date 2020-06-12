package play.api.libs.json.ops.v4

import play.api.libs.json.{Json, Reads}

import scala.language.reflectiveCalls
import scala.reflect.macros.whitebox
import language.experimental.macros
import scala.collection.generic

/**
 * Provides macros similar to those in [[Json]] but with modified functionality to better handle our common json
 * parsing use cases, for example missing fields (instead of empty arrays).
 */
object PlayJsonMacros extends TolerantContainerFormats {

  /**
   * Same as [[Json.reads]] but when reading containers such as [[List]], [[Seq]], [[Set]], etc., if the field
   * is missing will return an empty container instead of failing to validate the model.
   *
   * NOTE: this doesn't seem to work with Array, but does work with [[scala.collection.mutable.ArraySeq]] and
   *       other collections which extend [[Traversable]]
   *
   * @see [[TolerantContainerPath.readNullableContainer]]
   * @tparam A The model you're generating a [[Reads]] for
   * @return a [[Reads]] that will deserialize [[A]], with empty containers for fields that are missing from the json.
   */
  def nullableReads[A]: Reads[A] = macro nullableReadsImpl[A]

  /**
   * copied from [[play.api.libs.json.JsMacroImpl.readsImpl]] implementation and modified to use the readNullableContainer helper
   * @see https://github.com/playframework/playframework/blob/2.3.x/framework/src/play-json/src/main/scala/play/api/libs/json/JsMacroImpl.scala#L11
   */
  def nullableReadsImpl[A: c.WeakTypeTag](c: whitebox.Context): c.Expr[Reads[A]] = {
    import c.universe.Flag._
    import c.universe._

    val companioned = weakTypeOf[A].typeSymbol
    val companionSymbol = companioned.companion
    val companionType = companionSymbol.typeSignature

    val libsPkg = Select(Select(Ident(TermName("play")), TermName("api")), TermName("libs"))
    val jsonPkg = Select(libsPkg, TermName("json"))
    val functionalSyntaxPkg = Select(Select(libsPkg, TermName("functional")), TermName("syntax"))
    val utilPkg = Select(jsonPkg, TermName("util"))

    val jsPathSelect = Select(jsonPkg, TermName("JsPath"))
    val readsSelect = Select(jsonPkg, TermName("Reads"))
    val lazyHelperSelect = Select(utilPkg, TypeName("LazyHelper"))

    val importFunctionalSyntax = Import(functionalSyntaxPkg, List(ImportSelector(termNames.WILDCARD, -1, null, -1)))

    companionType.decl(TermName("unapply")) match {
      case NoSymbol => c.abort(c.enclosingPosition, "No unapply function found")
      case s =>
        val unapply = s.asMethod
        val unapplyReturnTypes = unapply.returnType match {
          case TypeRef(_, _, Nil) =>
            c.abort(c.enclosingPosition, s"Apply of ${companionSymbol} has no parameters. Are you using an empty case class?")
          case TypeRef(_, _, args) =>
            args.head match {
              case t @ TypeRef(_, _, Nil) => Some(List(t))
              case t @ TypeRef(_, _, args) =>
                if (t <:< typeOf[Option[_]]) Some(List(t))
                else if (t <:< typeOf[Seq[_]]) Some(List(t))
                else if (t <:< typeOf[Set[_]]) Some(List(t))
                else if (t <:< typeOf[Map[_, _]]) Some(List(t))
                else if (t <:< typeOf[Product]) Some(args)
              case _ => None
            }
          case _ => None
        }

        companionType.decl(TermName("apply")) match {
          case NoSymbol => c.abort(c.enclosingPosition, "No apply function found")
          case s =>
            // searches apply method corresponding to unapply
            val applies = s.asMethod.alternatives
            val apply = applies.collectFirst {
              case apply: MethodSymbol
                if apply.paramLists.headOption.map(_.map(_.asTerm.typeSignature)) == unapplyReturnTypes => apply
            }
            apply match {
              case Some(apply) =>
                val params = apply.paramLists.head //verify there is a single parameter group

                val inferedImplicits = params.map(_.typeSignature).map { implType =>

                  // innerType is only used if we're working with a container and using readNullableContainer below
                  val (isRecursive, tpe, innerType) = implType match {
                    case TypeRef(_, _, args) =>
                      // Option[_] needs special treatment because we need to use XXXOpt
                      if (implType.typeConstructor <:< typeOf[Option[_]].typeConstructor)
                        (args.exists(_.typeSymbol == companioned), args.head, args.head)
                      else if (implType.typeConstructor <:< typeOf[Traversable[_]].typeConstructor)
                        (args.exists(_.typeSymbol == companioned), implType, args.head)
                      else
                        (args.exists(_.typeSymbol == companioned), implType, implType)
                    case _ =>
                      (false, implType, implType)
                  }

                  // builds reads implicit from expected type
                  val neededReadsImplicitType = appliedType(weakTypeOf[Reads[_]].typeConstructor, tpe :: Nil)
                  // infers implicit
                  val neededReadsImplicit = c.inferImplicitValue(neededReadsImplicitType)

                  // builds canBuildFrom implicit type if type is Traversable
                  val neededCanBuildFromImplicit = if (tpe != innerType) {
                    val neededCanBuildFromImplicitType =
                      appliedType(weakTypeOf[generic.CanBuildFrom[_, _, _]].typeConstructor, List(tpe, innerType, tpe))
                    c.inferImplicitValue(neededCanBuildFromImplicitType)
                  } else
                    neededReadsImplicit

                  (implType, neededReadsImplicit, neededCanBuildFromImplicit, isRecursive, tpe, innerType)
                }

                // if any implicit is missing, abort
                // else goes on
                inferedImplicits.collect {
                  case (t, readsImplicit, _, rec, _, _) if readsImplicit == EmptyTree && !rec => t
                } match {
                  case List() =>
                    val namedImplicits = params.map(_.name).zip(inferedImplicits)

                    val helperMember = Select(This(typeNames.EMPTY), TermName("lazyStuff"))

                    var hasRec = false

                    // combines all reads into CanBuildX
                    val canBuild = namedImplicits.map {
                      case (name, (t, readsImplicit, cbfImplicit, rec, tpe, innerType)) =>
                        // inception of (__ \ name).read(readsImplicit)
                        val jspathTree = Apply(
                          Select(jsPathSelect, TermName(scala.reflect.NameTransformer.encode("\\"))),
                          List(Literal(Constant(name.decodedName.toString)))
                        )

                        if (!rec) {
                          val readTree =
                            if (t.typeConstructor <:< typeOf[Option[_]].typeConstructor)
                              Apply(
                                Select(jspathTree, TermName("readNullable")),
                                List(readsImplicit)
                              )
                            // If Traversable, then apply readNullableContainer helper instead
                            else if (t.typeConstructor <:< typeOf[Traversable[_]].typeConstructor) {
                              val justContainer = tpe.typeConstructor
                              val app = Apply(
                                c.Expr[Any](q"${Select(jspathTree, TermName("readNullableContainer"))}[$justContainer,$innerType]").tree,
                                List(readsImplicit, cbfImplicit)
                              )
                              app
                            } else Apply(
                              Select(jspathTree, TermName("read")),
                              List(readsImplicit)
                            )

                          readTree
                        } else {
                          hasRec = true
                          val readTree =
                            if (t.typeConstructor <:< typeOf[Option[_]].typeConstructor)
                              Apply(
                                Select(jspathTree, TermName("readNullable")),
                                List(
                                  Apply(
                                    Select(Apply(jsPathSelect, List()), TermName("lazyRead")),
                                    List(helperMember)
                                  )
                                )
                              )
                            // If Traversable, then apply readNullableContainer helper instead
                            else if (t.typeConstructor <:< typeOf[Traversable[_]].typeConstructor)
                              Apply(
                                Select(jspathTree, TermName("readNullableContainer")),
                                if (tpe.typeConstructor <:< typeOf[List[_]].typeConstructor)
                                  List(
                                    Apply(
                                      Select(readsSelect, TermName("list")),
                                      List(helperMember)
                                    )
                                  )
                                else if (tpe.typeConstructor <:< typeOf[Set[_]].typeConstructor)
                                  List(
                                    Apply(
                                      Select(readsSelect, TermName("set")),
                                      List(helperMember)
                                    )
                                  )
                                else if (tpe.typeConstructor <:< typeOf[Seq[_]].typeConstructor)
                                  List(
                                    Apply(
                                      Select(readsSelect, TermName("seq")),
                                      List(helperMember)
                                    )
                                  )
                                else if (tpe.typeConstructor <:< typeOf[Map[_, _]].typeConstructor)
                                  List(
                                    Apply(
                                      Select(readsSelect, TermName("map")),
                                      List(helperMember)
                                    )
                                  )
                                else List(helperMember)
                              )

                            else {
                              Apply(
                                Select(jspathTree, TermName("lazyRead")),
                                if (tpe.typeConstructor <:< typeOf[List[_]].typeConstructor)
                                  List(
                                    Apply(
                                      Select(readsSelect, TermName("list")),
                                      List(helperMember)
                                    )
                                  )
                                else if (tpe.typeConstructor <:< typeOf[Set[_]].typeConstructor)
                                  List(
                                    Apply(
                                      Select(readsSelect, TermName("set")),
                                      List(helperMember)
                                    )
                                  )
                                else if (tpe.typeConstructor <:< typeOf[Seq[_]].typeConstructor)
                                  List(
                                    Apply(
                                      Select(readsSelect, TermName("seq")),
                                      List(helperMember)
                                    )
                                  )
                                else if (tpe.typeConstructor <:< typeOf[Map[_, _]].typeConstructor)
                                  List(
                                    Apply(
                                      Select(readsSelect, TermName("map")),
                                      List(helperMember)
                                    )
                                  )
                                else List(helperMember)
                              )
                            }

                          readTree
                        }
                    }.reduceLeft { (acc, r) =>
                      Apply(
                        Select(acc, TermName("and")),
                        List(r)
                      )
                    }

                    // builds the final Reads using apply method
                    val applyMethod =
                      Function(
                        params.foldLeft(List[ValDef]())((l, e) =>
                          l :+ ValDef(Modifiers(PARAM), TermName(e.name.encodedName.toString), TypeTree(), EmptyTree)
                        ),
                        Apply(
                          Select(Ident(companionSymbol.name), TermName("apply")),
                          params.foldLeft(List[Tree]())((l, e) =>
                            l :+ Ident(TermName(e.name.encodedName.toString))
                          )
                        )
                      )

                    // if case class has one single field, needs to use inmap instead of canbuild.apply
                    val finalTree = if (params.length > 1) {
                      Apply(
                        Select(canBuild, TermName("apply")),
                        List(applyMethod)
                      )
                    } else {
                      Apply(
                        Select(canBuild, TermName("map")),
                        List(applyMethod)
                      )
                    }

                    if (!hasRec) {
                      c.Expr[Reads[A]](
                        q"""{
                          $importFunctionalSyntax
                          $finalTree
                        }"""
                      )
                    } else {
                      val defineClassWithLazyStuff = ClassDef(
                        Modifiers(Flag.FINAL),
                        TypeName("$anon"),
                        List(),
                        Template(
                          List(
                            AppliedTypeTree(
                              lazyHelperSelect,
                              List(
                                Ident(weakTypeOf[Reads[A]].typeSymbol),
                                Ident(weakTypeOf[A].typeSymbol)
                              )
                            )
                          ),
                          noSelfType,
                          List(
                            DefDef(
                              Modifiers(),
                              termNames.CONSTRUCTOR,
                              List(),
                              List(List()),
                              TypeTree(),
                              Apply(
                                Select(Super(This(typeNames.EMPTY), typeNames.EMPTY), termNames.CONSTRUCTOR),
                                List()
                              )
                            ),
                            ValDef(
                              Modifiers(Flag.OVERRIDE | Flag.LAZY),
                              TermName("lazyStuff"),
                              AppliedTypeTree(Ident(weakTypeOf[Reads[A]].typeSymbol), List(TypeTree(weakTypeOf[A]))),
                              finalTree
                            )
                          )
                        )
                      )
                      val constructInstance = Apply(Select(New(Ident(TypeName("$anon"))), termNames.CONSTRUCTOR), List())
                      val lazyStuff = TermName("lazyStuff")

                      c.Expr[Reads[A]](
                        q"""{
                          $importFunctionalSyntax
                          $defineClassWithLazyStuff
                          $constructInstance
                        }.$lazyStuff"""
                      )
                    }
                  case l => c.abort(c.enclosingPosition, s"No implicit Reads for ${l.mkString(", ")} available.")
                }

              case None => c.abort(c.enclosingPosition, "No apply function found matching unapply return types")
            }

        }
    }
  }
}
