import sbt.File
import com.typesafe.sbt.packager.linux._
import NativePackagerHelper._

name := "kv2kl"

description := "kv2kl - listen to data from kvalobs and load into a klima database"

version in Debian := "3.0.0~rc11-1"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

//credentials += Credentials("OAM 11g", "login.oracle.com", "borgem.moe@met.no", "Xgtlfp9al")

//libraryDependencies ++= Seq	(
//	"org.postgresql" % "postgresql" % "9.4.1212.jre7",
//	"com.oracle" % "ojdbc14" % "10.2.0.1.0"
//)


libraryDependencies ++= Seq	(
	"org.postgresql" % "postgresql" % "9.4.1212.jre7",
	"com.oracle" % "ojdbc8" % "12.2.0.1"


	//"com.oracle" % "ojdbc7" % "12.1.0.2"
)


//changelog in Debian := Some( baseDirectory.value / "debian_changelog")

debianChangelog in Debian := Some( baseDirectory.value / "debian_changelog" )

packageSummary := "Utility to transfer data from a kvalobs data base to the klimadb"

packageDescription := """Utility to transfer data from a kvalobs data base to the \"klimadb\"."""

debianPackageDependencies in Debian ++= Seq("kvdistuser", "openjdk-8-jdk")

linuxPackageMappings in Debian += packageTemplateMapping(s"/etc/kvalobs")() withGroup("kvdist") withPerms("0775")

linuxPackageMappings in Debian += packageTemplateMapping(s"/var/lib/kvalobs/run")() withGroup("kvdist") withPerms("0775")

linuxPackageMappings in Debian += packageTemplateMapping(s"/var/log/kvalobs")() withGroup("kvdist") withPerms("0775")

linuxPackageMappings in Debian += packageTemplateMapping(s"/var/lib/kvalobs")() withGroup("kvdist") withPerms("0775")

linuxPackageSymlinks in Debian += LinuxSymlink(s"/etc/kvalobs", s"/usr/share/kv2kl/etc/kv2kl.conf.template" )

bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / ".." / "/scripts" / "kv2kl-extra.sh")


