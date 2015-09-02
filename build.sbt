import Dependencies._
import Util._

def internalPath   = file("internal")

def commonSettings: Seq[Setting[_]] = Seq(
  scalaVersion := "2.10.5",
  // publishArtifact in packageDoc := false,
  resolvers += Resolver.typesafeIvyRepo("releases"),
  resolvers += Resolver.sonatypeRepo("snapshots"),
  // concurrentRestrictions in Global += Util.testExclusiveRestriction,
  testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-w", "1"),
  javacOptions in compile ++= Seq("-target", "6", "-source", "6", "-Xlint", "-Xlint:-serial"),
  incOptions := incOptions.value.withNameHashing(true),
  crossScalaVersions := Seq(scala210, scala211),
  bintrayPackage := (bintrayPackage in ThisBuild).value,
  bintrayRepository := (bintrayRepository in ThisBuild).value
)

def testedBaseSettings: Seq[Setting[_]] =
  commonSettings ++ testDependencies

lazy val utilRoot: Project = (project in file(".")).
  // configs(Sxr.sxrConf).
  aggregate(
    utilInterface, utilControl, utilCollection, utilApplyMacro, utilComplete,
    utilLogging, utilRelation, utilLogic, utilCache, utilTracking
  ).
  settings(
    inThisBuild(Seq(
      organization := "org.scala-sbt",
      version := "0.1.0-SNAPSHOT",
      homepage := Some(url("https://github.com/sbt/util")),
      description := "Util module for sbt",
      licenses := List("BSD New" -> url("https://github.com/sbt/sbt/blob/0.13/LICENSE")),
      scmInfo := Some(ScmInfo(url("https://github.com/sbt/util"), "git@github.com:sbt/util.git")),
      developers := List(
        Developer("harrah", "Mark Harrah", "@harrah", url("https://github.com/harrah")),
        Developer("eed3si9n", "Eugene Yokota", "@eed3si9n", url("https://github.com/eed3si9n")),
        Developer("jsuereth", "Josh Suereth", "@jsuereth", url("https://github.com/jsuereth")),
        Developer("dwijnand", "Dale Wijnand", "@dwijnand", url("https://github.com/dwijnand"))
      ),
      bintrayReleaseOnPublish := false,
      bintrayOrganization := Some("sbt"),
      bintrayRepository := "maven-releases",
      bintrayPackage := "util"
    )),
    commonSettings,
    name := "Util Root",
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )

// defines Java structures used across Scala versions, such as the API structures and relationships extracted by
//   the analysis compiler phases and passed back to sbt.  The API structures are defined in a simple
//   format from which Java sources are generated by the datatype generator Projproject
lazy val utilInterface = (project in internalPath / "util-interface").
  settings(
    commonSettings,
    javaOnlySettings,
    name := "Util Interface",
    // projectComponent,
    exportJars := true
    // resourceGenerators in Compile <+= (version, resourceManaged, streams, compile in Compile) map generateVersionFile,
    // apiDefinitions <<= baseDirectory map { base => (base / "definition") :: (base / "other") :: (base / "type") :: Nil },
    // sourceGenerators in Compile <+= (apiDefinitions,
    //   fullClasspath in Compile in datatypeProj,
    //   sourceManaged in Compile,
    //   mainClass in datatypeProj in Compile,
    //   runner,
    //   streams) map generateAPICached
  )

lazy val utilControl = (project in internalPath / "util-control").
  settings(
    commonSettings,
    // Util.crossBuild,
    name := "Util Control",
    crossScalaVersions := Seq(scala210, scala211)
  )

lazy val utilCollection = (project in internalPath / "util-collection").
  settings(
    testedBaseSettings,
    Util.keywordsSettings,
    // Util.crossBuild,
    name := "Util Collection",
    crossScalaVersions := Seq(scala210, scala211)
  )

lazy val utilApplyMacro = (project in internalPath / "util-appmacro").
  dependsOn(utilCollection).
  settings(
    testedBaseSettings,
    name := "Util Apply Macro",
    libraryDependencies += scalaCompiler.value
  )

// Command line-related utilities.
lazy val utilComplete = (project in internalPath / "util-complete").
  dependsOn(utilCollection, utilControl).
  settings(
    testedBaseSettings,
    // Util.crossBuild,
    name := "Util Completion",
    libraryDependencies ++= Seq(jline, sbtIO),
    crossScalaVersions := Seq(scala210, scala211)
  )

// logging
lazy val utilLogging = (project in internalPath / "util-logging").
  dependsOn(utilInterface).
  settings(
    testedBaseSettings,
    publishArtifact in (Test, packageBin) := true,
    name := "Util Logging",
    libraryDependencies += jline
  )

// Relation
lazy val utilRelation = (project in internalPath / "util-relation").
  settings(
    testedBaseSettings,
    name := "Util Relation"
  )

// A logic with restricted negation as failure for a unique, stable model
lazy val utilLogic = (project in internalPath / "util-logic").
  dependsOn(utilCollection, utilRelation).
  settings(
    testedBaseSettings,
    name := "Util Logic"
  )

// Persisted caching based on SBinary
lazy val utilCache = (project in internalPath / "util-cache").
  dependsOn(utilCollection).
  settings(
    commonSettings,
    name := "Util Cache",
    libraryDependencies ++= Seq(sbinary, sbtSerialization, scalaReflect.value, sbtIO) ++ scalaXml.value
  )

// Builds on cache to provide caching for filesystem-related operations
lazy val utilTracking = (project in internalPath / "util-tracking").
  dependsOn(utilCache).
  settings(
    commonSettings,
    name := "Util Tracking",
    libraryDependencies ++= Seq(sbtIO)
  )
