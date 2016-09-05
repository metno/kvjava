name := "kvclient"

description := "kvalobs client library"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

libraryDependencies ++= Seq	(
	"org.apache.kafka" % "kafka-clients" %"0.8.2.2",
	"org.apache.kafka" %% "kafka" % "0.8.2.2"
)