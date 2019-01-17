//import com.typesafe.sbt.packager.linux._
//import NativePackagerHelper._

name := "kv2klgetdata"

description := "kv2klgetdata - get data from kvalobs and load into a klima database"

version in Debian := "3.0.0~rc5-1"

// Enables publishing to maven repo
publishMavenStyle := true


// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

libraryDependencies ++= Seq	(
	"org.postgresql" % "postgresql" % "9.4.1211",
	"com.oracle" % "ojdbc8" % "12.2.0.1"
)

debianChangelog in Debian := Some( baseDirectory.value / "debian_changelog")

packageSummary := "Utility to transfer data from a kvalobs data base to the klimadb"

packageDescription := """Utility to transfer data from a kvalobs data base to the \"klimadb\"."""

debianPackageDependencies in Debian ++= Seq("kvdistuser","openjdk-8-jdk","kv2kl")

bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / ".."/ "scripts" / "kv2kl-extra.sh")


