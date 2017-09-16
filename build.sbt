name := "calm4"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.0.0-RC2"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.9"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.3",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3" % Test
)

libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.8"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.4"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.3"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.5.3"
libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.4.4"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.15"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.5.3"

libraryDependencies += "com.lihaoyi" % "ammonite" % "1.0.2" % "test" cross CrossVersion.full

sourceGenerators in Test += Def.task {
  val file = (sourceManaged in Test).value / "amm.scala"
  IO.write(file, """object amm extends App { ammonite.Main().run() }""")
  Seq(file)
}.taskValue