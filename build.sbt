val Http4sVersion = "0.18.0"
val Specs2Version = "4.0.2"
val LogbackVersion = "1.2.3"
val CircleVersion = "0.9.1"

lazy val commonSettings = Seq(
  organization := "com.vovapolu",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.4"
)


lazy val config = (project in file("config"))
  .settings(
    name := "esenin-config",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.github.pureconfig" %% "pureconfig" % "0.9.0"
    )
  )

lazy val server = (project in file("server"))
  .dependsOn(config)
  .settings(
    commonSettings,
    name := "esenin-server",
    libraryDependencies ++= Seq(
      "com.spotify" % "docker-client" % "8.11.1"
    ),
    mainClass in assembly := Some("com.vovapolu.esenin.server.EseninServerMain"),
    test in assembly := {}
  )


lazy val proxy = (project in file("proxy"))
  .dependsOn(config)
  .enablePlugins(DockerPlugin)
  .settings(
    commonSettings,
    name := "esenin-proxy",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "io.circe" %% "circe-generic" % CircleVersion,
      "io.circe" %% "circe-generic-extras" % CircleVersion,
      "io.circe" %% "circe-literal" % CircleVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.specs2" %% "specs2-core" % Specs2Version % "test",
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    ),
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    mainClass in assembly := Some("com.vovapolu.esenin.proxy.EseninProxy"),
    test in assembly := {},
    dockerfile in docker := {
      val artifact = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("openjdk:8-jre")
        add(artifact, artifactTargetPath)
        expose(9000)
        entryPoint("java", "-jar", artifactTargetPath)
      }
    },
    imageNames in docker := Seq(
      ImageName("esenin/proxy:latest")
    )
  )
