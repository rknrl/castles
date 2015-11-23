organization := "ru.rknrl"

name := "server"

version := "1.0"

scalaVersion := "2.11.2"

import com.trueaccord.scalapb.{ScalaPbPlugin ⇒ PB}

PB.protobufSettings

version in PB.protobufConfig := "2.4.1"

PB.includePaths in PB.protobufConfig += file("/Users/tolyayanot/dev/rknrl/protobuf-rpc/plugin/src/main/proto")
PB.generatedTargets in PB.protobufConfig := Seq((file(baseDirectory.value + "/src/generated/scala"), "*.scala"))
scalaSource in PB.protobufConfig := file(baseDirectory.value + "/src/generated/scala")
PB.flatPackage in PB.protobufConfig := true

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",

  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test",

  "com.typesafe.akka" %% "akka-slf4j" % "2.3.6",
  "ch.qos.logback" % "logback-classic" % "1.0.9",

  "io.spray" %% "spray-util" % "1.3.2",
  "io.spray" %% "spray-can" % "1.3.2",
  "io.spray" %% "spray-routing" % "1.3.2",

  "com.github.mauricio" %% "mysql-async" % "0.2.15",

  "net.liftweb" %% "lift-json" % "3.0-M2",

  "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.4.8" % PB.protobufConfig,

  "ru.rknrl" %% "common-server" % "1.0",
  "ru.rknrl" %% "social-server" % "1.0",
  "ru.rknrl" %% "rpc" % "1.0"
)

parallelExecution in Test := false

test in assembly := {}
assemblyJarName in assembly := "castles.jar"
mainClass in assembly := Some("ru.rknrl.castles.Main")