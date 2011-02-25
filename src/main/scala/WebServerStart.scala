/**import org.eclipse.jetty.webapp.{WebAppContext,WebAppClassLoader}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio._
import org.eclipse.jetty.util.{URIUtil}
import java.io.{File}
import java.net.{URL,URLClassLoader}
import java.security.ProtectionDomain



object WebServerStart extends Application {
	
		
		def main(args:Array[String]) {	
			System.out.println("Entering main")		
			val scc = new SelectChannelConnector
			scc.setPort(8080)
			val server = new Server
			server.setConnectors(Array(scc));
			System.out.println("Connectors created and set")
						
			//val locations = Array[URL](location)			
			//Thread.currentThread().setContextClassLoader(new URLClassLoader(locations))


			val context = new WebAppContext();
			System.out.println("WebAppContext created")
			context setServer(server)
			context setContextPath ("/")
			val protectionDomain = WebServerStart.getClass().getProtectionDomain()
			val location = protectionDomain.getCodeSource().getLocation()
			System.out.println("Locations found")
			val webAppClassLoader = new WebAppClassLoader(context)
			Thread.currentThread().setContextClassLoader(webAppClassLoader);
			System.out.println("webAppClassLoader created and current thread updated")
			
			//context.setClassLoader(webAppClassLoader)
			//context.setContextPath(URIUtil.SLASH)

			
			
			context.setWar(location.toExternalForm)
			System.out.println("war set")
			server.setHandler(context)
			System.out.println("server handler set")

			try {
				System.out.println("Starting Jetty")
			    server.start()
				System.out.println("Calling join")
			    server.join()
			} catch {
				case e: Exception => { e printStackTrace}
			}
		}
}

*/