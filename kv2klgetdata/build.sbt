import com.typesafe.sbt.packager.linux._
import NativePackagerHelper._

name := "kv2klgetdata"

description := "kv2klgetdata - get data from kvalobs and load into a klima database"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

libraryDependencies ++= Seq	(
	"org.postgresql" % "postgresql" % "9.4.1211",
	"com.oracle" % "ojdbc14" % "10.2.0.1.0"
)


//changelog in Debian := baseDirectory.value / "debian_changelog"

packageSummary := "Utility to transfer data from a kvalobs data base to the klimadb"

packageDescription := """Utility to transfer data from a kvalobs data base to the \"klimadb\"."""

debianPackageDependencies in Debian ++= Seq("kvdistuser","kv2kl")

bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / ".."/ "scripts" / "kv2kl-extra.sh")


