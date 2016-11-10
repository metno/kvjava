name := "kvclient"

description := "kvalobs client library"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

libraryDependencies ++= Seq	(
	"org.apache.kafka" % "kafka-clients" %"0.10.0.1",
	"org.slf4j" % "slf4j-log4j12" %  "1.7.21"
	//"org.apache.kafka" %% "kafka" % "0.10.0.1"
)