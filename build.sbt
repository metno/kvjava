name := "kvjava"


// Enables publishing to maven repo
publishMavenStyle := true

lazy val commonSettings = Seq(
	organization := "no.met.kvalobs",
	version := "3.0.0-SNAPSHOT",
	version in Debian := "3.0.0~rc2-1",
	maintainer in Linux := "Børge Moe <borge.moe@met.no>",
	parallelExecution in Test := false,

	libraryDependencies ++= Seq	(
		"log4j" % "log4j" % "1.2.17",
		"junit" % "junit" % "4.10" % Test,
		"org.hsqldb" % "hsqldb" % "2.3.4" % Test,
		"com.novocode" % "junit-interface" % "0.11" % Test
	)

)

lazy val kvdistuser = (project in file("kvdistuser"))
	.settings(commonSettings: _*)
	.settings(
		packageSummary := "Install the kvdist user.",
		debianChangelog in Debian := Some( baseDirectory.value / "debian_changelog"),
		packageDescription := """The user that is supposed to run the kv2kl data transfer utilities.""",
	   maintainerScripts in Debian := maintainerScriptsAppend((maintainerScripts in Debian).value)(
  	"templates" -> IO.readLines( baseDirectory.value / "src" / "debian" / "DEBIAN" / "templates").mkString("\n") ),
  	debianPackageDependencies in Debian ++= Seq("adduser", "debconf")
	)
	.enablePlugins(DebianPlugin)

lazy val kvutil = (project in file("kvutil")).
	settings(commonSettings: _*)
	
lazy val kvclient = (project in file("kvclient")).
	dependsOn(kvutil).
	settings(commonSettings: _*)

lazy val klcommon = (project in file("klcommon")).
	dependsOn(kvutil, kvclient).
	settings(commonSettings: _*)

lazy val kv2kl = (project in file("kv2kl"))
	.dependsOn(kvutil, kvclient, klcommon, kvdistuser)
	.settings(commonSettings: _*)
	.enablePlugins(JavaAppPackaging,DebianPlugin)

lazy val kv2klgetdata = (project in file("kv2klgetdata"))
	.dependsOn(kvutil, kvclient, klcommon, kvdistuser)
	.settings(commonSettings: _*)
	.enablePlugins(JavaAppPackaging,DebianPlugin)
