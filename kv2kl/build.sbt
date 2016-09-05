name := "kv2kl"

description := "kv2kl - listen to data from kvalobs and load into a klima database"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

libraryDependencies ++= Seq	(
	"org.postgresql" % "postgresql" % "9.4.1209"
)