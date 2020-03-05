resolvers += Classpaths.sbtPluginReleases
resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")
resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("https://dl.bintray.com/content/sbt/sbt-plugin-releases")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.rallyhealth.sbt" %% "sbt-git-versioning" % "1.2.2")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
