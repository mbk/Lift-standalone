import org.eclipse.jetty.webapp.{WebAppContext}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio._

object WebServerStart   {
	
		def main(args:Array[String]) {
			val server = new Server
			val scc = new SelectChannelConnector
			scc.setPort(8080)
			server.setConnectors(Array(scc))
		
			val context = new WebAppContext();
			context.setContextPath("/*")
			context.setWar("src/main/webapp")
			server.setHandler(context)
			Console println("Jetty started")
		    server.start()
		    server.join()
		}
}