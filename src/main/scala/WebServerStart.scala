import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import java.io.{File}
import java.net.{URL,URLClassLoader}
import java.security.ProtectionDomain



object WebServerStart extends Application {
	
	
			val scc = new SelectChannelConnector
			scc.setPort(8080)
			val server = new Server
			server.setConnectors(Array(scc))



			val context = new WebAppContext();
			System.out.println("WebAppContext created")
			context setServer(server)
			context setContextPath ("/")
			val protectionDomain = WebServerStart.getClass().getProtectionDomain()
			val location = protectionDomain.getCodeSource().getLocation()
			val webAppClassLoader = new WebAppClassLoader(context)
			Thread.currentThread().setContextClassLoader(webAppClassLoader);
			
			context.setClassLoader(webAppClassLoader)			
			
			context.setWar(location.toExternalForm)
			server.setHandler(context)

			try {
			    server.start()
			    server.join()
			} catch {
				case e: Exception => { e printStackTrace}
			}
		//}
}
