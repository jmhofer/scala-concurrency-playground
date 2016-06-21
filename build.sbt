organization := "org.zalando"
name := "benchmarks-scala-nonblocking"
version := "1.0.0-SNAPSHOT"

enablePlugins(JmhPlugin)

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:_")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.7",
  "com.typesafe.akka" %% "akka-stream" % "2.4.7",
  "com.softwaremill.macwire" %% "macros" % "2.2.3" % "provided",
  "io.reactivex" %% "rxscala" % "0.26.1"
)

cancelable in Global := true
