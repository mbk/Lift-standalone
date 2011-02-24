import org.eclipse.jetty.webapp.{WebAppContext,WebAppClassLoader}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio._
import org.eclipse.jetty.util.{URIUtil}
import java.io.{File}
import java.net.{URL,URLClassLoader}
import java.security.ProtectionDomain

object WebServerStart   {
	
		def main(args:Array[String]) {			
			
			val protectionDomain = WebServerStart.getClass().getProtectionDomain()
			val location = protectionDomain.getCodeSource().getLocation()
			Console println("**** location = " + location.toExternalForm)
			val locations = Array[URL](location)			
			Thread.currentThread().setContextClassLoader(new URLClassLoader(locations))


			val context = new WebAppContext();
			val webAppClassLoader = new WebAppClassLoader(context)
			//Thread.currentThread().setContextClassLoader(webAppClassLoader);
			
			context.setClassLoader(webAppClassLoader)
			context.setContextPath(URIUtil.SLASH)
			
			val tempDir = System.getProperty("jettyTempDir")
			if( tempDir != null ) {
	            val tempDirectory = new File(tempDir);
	            context.setTempDirectory(tempDirectory);
	        }
			

			context.setWar(location.toExternalForm)
			
			val scc = new SelectChannelConnector
			scc.setPort(8080)
			val server = new Server
			server.setConnectors(Array(scc));
			server.setHandler(context)

			try {
				Console println("Starting Jetty")
			    server.start()
			    server.join()
			} catch {
				case e: Exception => { e printStackTrace}
			}
		}
}