name := "calm4"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.0.0-RC2"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.9"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.3",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.3" % Test
)

        