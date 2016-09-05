name := "kvjava"


// Enables publishing to maven repo
publishMavenStyle := true

lazy val commonSettings = Seq(

	organization := "no.met.kvalobs",

	version := "1.0-SNAPSHOT",

	libraryDependencies ++= Seq	(
		"log4j" % "log4j" % "1.2.17",
		"junit" % "junit" % "4.10" % Test,
		"org.hsqldb" % "hsqldb" % "2.3.3" % Test
	),

	resolvers ++= Seq( 
		//"metno repo" at "http://maven.met.no/content/groups/public",
		"maven org" at "https://repo1.maven.org/maven2",
		"apache repository" at "https://repository.apache.org/content/repositories"	
	)
)

lazy val kvutil = (project in file("kvutil")).
	settings(commonSettings: _*)
	
lazy val kvclient = (project in file("kvclient")).
	dependsOn(kvutil).
	settings(commonSettings: _*)

lazy val klcommon = (project in file("klcommon")).
	dependsOn(kvutil, kvclient).
	settings(commonSettings: _*)

lazy val kv2kl = (project in file("kv2kl")).
	dependsOn(kvutil, kvclient, klcommon).
	settings(commonSettings: _*)


