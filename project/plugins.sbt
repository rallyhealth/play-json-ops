resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")
resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.2.0")
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")
