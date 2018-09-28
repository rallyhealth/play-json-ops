resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")
resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.2.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
