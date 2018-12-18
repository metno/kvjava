name := "klcommon"

description := "Common kvalobs library"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false


libraryDependencies +=  "com.mashape.unirest" % "unirest-java" % "1.4.9"

//libraryDependencies ++=  Seq(
//  "com.mashape.unirest" % "unirest-java" % "1.4.9",
//  "org.apache.httpcomponents" %"httpclient" %"4.3.6",
//  "org.apache.httpcomponents" % "httpasyncclient" % "4.0.2",
//  "org.apache.httpcomponents" % "httpmime" % "4.3.6",
//  "org.json" % "json" % "20180813"
//)