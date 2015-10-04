import sbt._
import Keys._

object BuildSettings {

  lazy val basicSettings = seq(
    version               := "0.1.0-SNAPSHOT",
    organization          := "foo.bar",
    startYear             := Some(2015),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion          := "2.11.7",
    resolvers             ++= Dependencies.resolutionRepos,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
  )

  lazy val everuntimeSettings = basicSettings
  lazy val eveannotationsSettings = basicSettings
  lazy val eveannotationProcessorSettings = basicSettings
  lazy val eveExampleSettings = basicSettings

}
