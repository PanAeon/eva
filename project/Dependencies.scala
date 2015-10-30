import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val scalaTest =  "org.scalatest" %% "scalatest" % "2.2.4"

  val bytebuddy = "net.bytebuddy" % "byte-buddy" % "0.7-rc1"  withSources() withJavadoc()

  val javaslang = "com.javaslang" % "javaslang" % "1.2.2"  withSources() withJavadoc()

  val hsqldb = "org.hsqldb" % "hsqldb" % "2.3.3"

  val hikariCP = "com.zaxxer" % "HikariCP" % "2.4.1"

  val jsqlparser = "com.github.jsqlparser" % "jsqlparser" % "0.9.3"  withJavadoc()

  val commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.4"  withSources() withJavadoc()

  val parboiled = "org.parboiled" %% "parboiled-scala" % "1.1.7"  withSources() withJavadoc()
  

  lazy val eveRuntimeDependencies = compile(bytebuddy, javaslang, 
      hsqldb, hikariCP, jsqlparser, commonsLang3, parboiled) ++
      test(scalaTest)

        //<dependency>
        //    <groupId>org.postgresql</groupId>
        //    <artifactId>postgresql</artifactId>
        //    <version>9.4-1202-jdbc42</version>
        //</dependency>
        //<dependency>
        //    <groupId>mysql</groupId>
        //    <artifactId>mysql-connector-java</artifactId>
        //    <version>5.1.36</version>
        //</dependency>
      //   <!-- ORACLE database driver 
      //    see http://www.mkyong.com/maven/how-to-add-oracle-jdbc-driver-in-your-maven-local-repository/
      //   <dependency>
      //     <groupId>com.oracle</groupId>
      //     <artifactId>ojdbc7</artifactId>
      //     <version>12.1.0.2</version>
      //   </dependency>
      // -->


      

}