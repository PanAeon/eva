import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object EveBuild extends Build {
  import BuildSettings._
  import Dependencies._

  val resolutionRepos = Seq(
    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

  lazy val parent = Project(id = "eve",
    base = file("."))
    .aggregate (runtimeProject, annotationsProject, annotationProcessorProject, exampleProject)
    .settings(basicSettings: _*)

  lazy val annotationsProject = Project(id = "eve-annotations", base = file("eve-annotations"))
    .settings(eveannotationsSettings: _*)
    .settings(crossPaths := false)
    .settings(autoScalaLibrary := false)
    .settings(EclipseKeys.projectFlavor := EclipseProjectFlavor.Java)

  lazy val annotationProcessorProject = Project(id = "eve-annotation-processor", base = file("eve-annotation-processor"))
    .settings(eveannotationProcessorSettings: _*)
    .dependsOn(annotationsProject)
    .settings(exportJars := true)

  lazy val runtimeProject = Project(id = "eve-runtime", base = file("eve-runtime"))
    .settings(everuntimeSettings: _*)
    .dependsOn(annotationsProject)
    .dependsOn(annotationProcessorProject)
    .settings(libraryDependencies ++= eveRuntimeDependencies)

   lazy val exampleProject = Project(id = "eve-example", base = file("eve-example"))
    .settings(eveExampleSettings: _*)
    //.settings(javacOptions ++= List("-XprintRounds"))
    // .settings(javacOptions ++= List("-processor", "foo.bar.annotations.processor.SqlPreProcessor", "-proc:only", "-XprintRounds"))
    //.settings((fork in (Compile, run)) := true)
    .dependsOn(annotationsProject, annotationProcessorProject, runtimeProject)
}
