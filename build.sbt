val skuber = "io.github.doriordan" %% "skuber" % "1.7.1-RC2"
val jacksonYaml = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.9.1"
val snakeYaml = "org.yaml" % "snakeyaml" % "1.18"
val cats = "org.typelevel" %% "cats-core" % "1.0.0-MF"

// These versions are required by skuber
scalacOptions += "-target:jvm-1.8"
scalaVersion := "2.11.8"

scalacOptions in Test ++= Seq("-Yrangepos")

version in ThisBuild := "0.1"

// NOTE: not the long-term planned profile name or organization
sonatypeProfileName := "io.github.doriordan"

publishMavenStyle in ThisBuild := true

licenses in ThisBuild := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage in ThisBuild := Some(url("https://github.com/doriordan"))

scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/doriordan/skuber-util"),
    "scm:git@github.com:doriordan/skuber-util.git"
  )
)

developers in ThisBuild := List(Developer(id="doriordan", name="David ORiordan", email="doriordan@gmail.com", url=url("https://github.com/doriordan")))

lazy val commonSettings = Seq(
  organization := "io.github.doriordan",
  scalaVersion := "2.11.8",
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomIncludeRepository := { _ => false }
)

lazy val skuberUtilSettings = Seq(
  name := "skuber-util",
  libraryDependencies ++= Seq(skuber,jacksonYaml, snakeYaml, cats).
				map(_.exclude("commons-logging","commons-logging"))
)

publishArtifact in root := false

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(skuberUtilSettings: _*)
