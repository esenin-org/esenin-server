lazy val root = (project in file("."))
  .settings(
    name := "nlp-server",
    organization := "com.vovapolu",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Seq(
      "com.spotify" % "docker-client" % "8.11.1"
    ),
    mainClass in assembly := Some("com.vovapolu.nlpserver.NlpServerMain"),
    test in assembly := {}
  )
