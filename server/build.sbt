import com.trueaccord.scalapb.{ScalaPbPlugin ⇒ PB}

organization := "ru.rknrl"

name := "server"

version := "1.0"

scalaVersion := "2.11.7"

PB.protobufSettings

version in PB.protobufConfig := "2.6.1"

PB.includePaths in PB.protobufConfig += file("/Users/tolyayanot/dev/rknrl/protobuf-rpc/plugin/src/main/proto")

PB.includePaths in PB.protobufConfig += file("/Users/tolyayanot/dev/rknrl/protobuf-rpc/plugin/src/main/protobuf")

PB.generatedTargets in PB.protobufConfig := Seq((file(sourceDirectory.value + "/generated/scala"), "*.scala"))

PB.flatPackage in PB.protobufConfig := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",

  "com.typesafe.akka" %% "akka-slf4j" % "2.4.1",
  "ch.qos.logback" % "logback-classic" % "1.0.9",

  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.1",

  "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.4.19" % PB.protobufConfig,

  "com.github.mauricio" %% "mysql-async" % "0.2.15",
  "net.liftweb" %% "lift-json" % "3.0-M2",

  "io.spray" %% "spray-util" % "1.3.2",
  "io.spray" %% "spray-can" % "1.3.2",
  "io.spray" %% "spray-routing" % "1.3.2",

  "org.json" % "json" % "20151123",
  "commons-codec" % "commons-codec" % "1.9",

  "ru.rknrl" %% "common-scala" % "1.0",
  "ru.rknrl" %% "rpc" % "1.0",
  "ru.rknrl" %% "akka-cross-domain-policy" % "1.0"
)

parallelExecution in Test := false

test in assembly := {}

assemblyJarName in assembly := "castles.jar"

mainClass in assembly := Some("ru.rknrl.castles.Main")