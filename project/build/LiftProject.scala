import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) with assembly.AssemblyBuilder {
  val liftVersion = "2.3-M1"

  // uncomment the following if you want to use the snapshot repo
  // val scalatoolsSnapshot = ScalaToolsSnapshots

  // If you're using JRebel for Lift development, uncomment
  // this line
  // override def scanDirectories = Nil

  //Assembly builder stuff	
  override def compileOptions = super.compileOptions ++ Seq(Optimize)
  override def mainClass = Some("WebServerStart")


  override def libraryDependencies = Set(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "org.eclipse.jetty" % "jetty-webapp"  % "7.1.6.v20100715" % "compile,test -> default" ,
    "junit" % "junit" % "4.5" % "test->default",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"
  ) ++ super.libraryDependencies
}
