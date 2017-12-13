import com.typesafe.sbt.packager.linux._
import NativePackagerHelper._

name := "kl2kv"

version in Debian := "3.0.0~rc1-1"

description := "kl2kv - transfer data from a klima database to kvalobs."

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


//changelog in Debian := baseDirectory.value / "debian_changelog"

packageSummary := "Utility to transfer data from a klima database to kvalobs."

packageDescription := """Utility to transfer data from a klima database to the \"kvalobs\"."""

debianPackageDependencies in Debian ++= Seq("kvdistuser","kv2kl")

bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / ".."/ "scripts" / "kv2kl-extra.sh")


