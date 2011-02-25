import sbt._

class Plugins(info: sbt.ProjectInfo) extends sbt.PluginDefinition(info) {
  val retronymSnapshotRepo = "retronym's repo" at "http://retronym.github.com/repo/releases"
  val onejarSBT = "com.github.retronym" % "sbt-onejar" % "0.2"
}