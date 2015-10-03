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
    .aggregate (runtimeProject, annotationsProject)
    .settings(basicSettings: _*)

  lazy val runtimeProject = Project(id = "eve-runtime", base = file("eve-runtime"))
    .settings(everuntimeSettings: _*)
    .settings(libraryDependencies ++= eveRuntimeDependencies)
    .settings(crossPaths := false)
    .settings(autoScalaLibrary := false)
    .settings(EclipseKeys.projectFlavor := EclipseProjectFlavor.Java)

  lazy val annotationsProject = Project(id = "eve-annotations", base = file("ever-annotations"))
    .settings(eveannotationsSettings: _*)
    .settings(crossPaths := false)
    .settings(autoScalaLibrary := false)
    .settings(EclipseKeys.projectFlavor := EclipseProjectFlavor.Java)
}
