import com.typesafe.sbt.packager.linux._
import NativePackagerHelper._

name := "kv2kl"

description := "kv2kl - listen to data from kvalobs and load into a klima database"

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

debianPackageDependencies in Debian ++= Seq("kvdistuser")

linuxPackageMappings in Debian += packageTemplateMapping(s"/etc/kvalobs")() withGroup("kvdist") withPerms("0775")

linuxPackageMappings in Debian += packageTemplateMapping(s"/var/lib/kvalobs/run")() withGroup("kvdist") withPerms("0775")

linuxPackageMappings in Debian += packageTemplateMapping(s"/var/log/kvalobs")() withGroup("kvdist") withPerms("0775")

linuxPackageMappings in Debian += packageTemplateMapping(s"/var/lib/kvalobs")() withGroup("kvdist") withPerms("0775")

linuxPackageSymlinks in Debian += LinuxSymlink(s"/etc/kvalobs", s"/usr/share/kv2kl/etc/kv2kl.conf.template" )

bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / ".." / "/scripts" / "kv2kl-extra.sh")


