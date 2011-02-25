import sbt._
import java.util.jar.{Manifest,Attributes}
import java.io.File
import _root_.sbt._
import _root_.sbt.FileUtilities._
import Attributes.Name.CLASS_PATH

import java.util.jar.Attributes.Name._
import java.lang.String

import java.io.PrintWriter
import scala.collection.mutable
import scala.io.Source

trait AssemblyBuilder extends BasicScalaProject {
	override def classpathFilter = super.classpathFilter -- "*-sources.jar" -- "*-javadoc.jar"

	def assemblyExclude(base: PathFinder) =
	(base / "META-INF" ** "*") --- // generally ignore the hell out of META-INF
	(base / "META-INF" / "services" ** "*") --- // include all service providers
	(base / "META-INF" / "maven" ** "*") --- // include all Maven POMs and such
	(base / "WebServerStart.class")
	def assemblyOutputPath = outputPath / assemblyJarName
	def assemblyJarName = name + "-assembly-" + this.version + ".jar"
	def assemblyTemporaryPath = outputPath / "assembly-libs"
	def assemblyClasspath = runClasspath
	def assemblyExtraJars = mainDependencies.scalaJars +++ (outputPath / "output.war")
	def assemblyConflictingFiles(path: Path) = List((path / "META-INF" / "LICENSE"),
	(path / "META-INF" / "license"),
	(path / "META-INF" / "License"))

	def assemblyPaths(tempDir: Path, classpath: PathFinder, extraJars: PathFinder, exclude: PathFinder => PathFinder) = {
		val (libs, directories) = classpath.get.toList.partition(ClasspathUtilities.isArchive)
		val services = mutable.Map[String, mutable.ArrayBuffer[String]]()
		for(jar <- extraJars.get ++ libs) {
			val jarName = jar.asFile.getName
			log.info("Including %s".format(jarName))
			FileUtilities.unzip(jar, tempDir, log).left.foreach(error)
			FileUtilities.clean(assemblyConflictingFiles(tempDir), true, log)
			val servicesDir = tempDir / "META-INF" / "services"
			if (servicesDir.asFile.exists) {
				for (service <- (servicesDir ** "*").get) {
					val serviceFile = service.asFile
					if (serviceFile.exists && serviceFile.isFile) {
						val entries = services.getOrElseUpdate(serviceFile.getName, new mutable.ArrayBuffer[String]())
						for (provider <- Source.fromFile(serviceFile).getLines) {
							if (!entries.contains(provider)) {
								entries += provider
							}
						}
					}
				}
			}
		}

		for ((service, providers) <- services) {
			log.debug("Merging providers for %s".format(service))
			val serviceFile = (tempDir / "META-INF" / "services" / service).asFile
			val writer = new PrintWriter(serviceFile)
			for (provider <- providers.map { _.trim }.filter { !_.isEmpty }) {
				log.debug("-  %s".format(provider))
				writer.println(provider)
			}
			writer.close()
		}

		val base = (Path.lazyPathFinder(tempDir :: directories) ##)
		(descendents(base, "*") --- exclude(base)).get
	}

	def assemblyTask(tempDir: Path, classpath: PathFinder, extraJars: PathFinder, exclude: PathFinder => PathFinder) = {
		packageTask(Path.lazyPathFinder(assemblyPaths(tempDir, classpath, extraJars, exclude)), assemblyOutputPath, packageOptions)
	}

	lazy val assembly = assemblyTask(assemblyTemporaryPath,
		assemblyClasspath,
		assemblyExtraJars,
		assemblyExclude
		).dependsOn(packageAction).describedAs("Builds an optimized, single-file deployable JAR.")
	}


class LiftEmbedJetty( info: ProjectInfo ) extends DefaultWebProject(info) with AssemblyBuilder {

	val description = "Creates a Lift application war with embedded jetty"
	override def defaultWarName = "output.war"
	val liftVersion = "2.3-M1"
	
    override def compileOptions = super.compileOptions ++ Seq(Optimize)
	override def mainClass = Some("WebServerStart")

	val jettyEmbedVersion = "7.1.6.v20100715"
	val jettyEmbedConf = config("jettyEmbed")
	def jettyEmbedClasspath = managedClasspath(jettyEmbedConf)

	//Scala causes the $ sign madness
	val warMainClass = "WebServerStart"
	//val warMainClass2 = "WebServerStart$"
	
	var warClassPath = "WEB_INF/classes/ WEB_INF/lib/"
	
	val warManifestVersion = "1.0"

	override def packageOptions = List(new MainClass(warMainClass), new ManifestAttributes((CLASS_PATH,warClassPath)))

	override def libraryDependencies = Set(
	    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
	    "org.eclipse.jetty" % "jetty-webapp" % jettyEmbedVersion % "compile, test -> default",
	    "junit" % "junit" % "4.5" % "test->default",
	    "ch.qos.logback" % "logback-classic" % "0.9.26",
	    "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"
	  ) ++ super.libraryDependencies


	override protected def prepareWebappAction = prepareEmbeddedWebappTask(webappResources, temporaryWarPath, webappClasspath, mainDependencies.scalaJars) dependsOn(compile, copyResources)

	override protected def packageAction = {
		packageTask(descendents(temporaryWarPath ##, "*"), warPath, packageOptions) dependsOn(prepareWebappAction) describedAs "Creates a standalone war";
	}

	protected def prepareEmbeddedWebappTask(webappContents: PathFinder, warPath: => Path, classpath: PathFinder, extraJars: => Iterable[File]): Task =
	prepareEmbeddedWebappTask(webappContents, warPath, classpath, Path.lazyPathFinder(extraJars.map(Path.fromFile)))

	protected def prepareEmbeddedWebappTask(webappContents: PathFinder, warPath: => Path, classpath: PathFinder, extraJars: PathFinder): Task = {
		task {
			val webInfPath = warPath / "WEB-INF"
			val webLibDirectory = webInfPath / "lib"
			val classesTargetDirectory = webInfPath / "classes"
			val startupFile = classesTargetDirectory / "WebServerStart.class"
			//val startupFile2 = classesTargetDirectory / "WebServerStart$.class"
			//val startupFile = classesTargetDirectory / (warMainClass)

			val (libs, directories) = classpath.get.toList.partition(ClasspathUtilities.isArchive)
			val (embedLibs, embedDirectories) = jettyEmbedClasspath.get.toList.partition(ClasspathUtilities.isArchive)

			val classesAndResources = descendents(Path.lazyPathFinder(directories) ##, "*")

			if(log.atLevel(Level.Debug)) directories.foreach(d => log.debug(" Copying the contents of directory " + d + " to " + classesTargetDirectory))

			import FileUtilities.{copy, copyFile, copyFlat, copyFilesFlat, clean => fclean}

			embedLibs.foreach( embedLib => FileUtilities.unzip(embedLib,warPath,log) )

			(copy(webappContents.get, warPath, log).right flatMap { copiedWebapp =>
				copy(classesAndResources.get, classesTargetDirectory, log).right flatMap { copiedClasses =>
					copyFlat(libs, webLibDirectory, log).right flatMap { copiedLibs =>
						copyFilesFlat(extraJars.get.map(_.asFile), webLibDirectory, log).right flatMap {
							copiedExtraLibs => {
								fclean( warPath / "META-INF" / "MANIFEST.MF", log )
								/*try {
									log.info("war path on copy main server start = " + warPath)
									copyFile( startupFile , warPath / "WebServerStart.class" , log )
									//copyFile( startupFile2 , warPath / "WebServerStart$.class" , log )
								}
								catch {
									case e: Exception => log.info( "Unable to copy startup class due to : " + e.getMessage )
								}*/
								None.toLeft()
							}
						}
					}
				}
			}).left.toOption
		}
	}
}



