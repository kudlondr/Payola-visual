logLevel := Level.Warn

resolvers ++= Seq(
    DefaultMavenRepository,
    "SBT IDEA Repository" at "http://mpeltonen.github.com/maven/",
	"Typesafe Repository" at "http://repo.typesafe.com/typesafe/ivy-releases/",
	"SBT plugins Repository" at "http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/",
	"doc Repository" at "http://repo.typesafe.com/typesafe/releases/",
	"Maven central Repository" at "http://repo1.maven.org/maven2/",
    Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns),
	Resolver.url("sbt-plugin-releases on bintray", new URL("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
)

//addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.0.0")
//addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")


// https://github.com/jrudolph/sbt-dependency-graph/
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.6.0")
//addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.9.0")
//addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.6")

addSbtPlugin("play" % "sbt-plugin" % "2.1-09142012")
//addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.9")

libraryDependencies += "play" %% "play" % "2.1-09142012"
//libraryDependencies += "com.typesafe.play" %% "play" % "2.3.9"
