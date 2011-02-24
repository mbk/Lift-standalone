import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.util.URIUtil;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;





public class WebServerStart  {

    private static final int JETTY_PORT_DEFAULT = 8080;
    private static final String JETTY_PORT_NAME = "jettyPort";

    private static final String JETTY_SSL_PORT_NAME = "jettySslPort";
    private static final String JETTY_SSL_KEY_PASSWORD_NAME = "jettySslKeyPassword";
    private static final String JETTY_SSL_KEY_STOREFILE_NAME = "jettySslKeyStoreFile";


    private static final int JETTY_MAX_IDLE = 30000;


    public static void main(String[] args) throws Exception {

        SelectChannelConnector connector = new SelectChannelConnector();
        SslSelectChannelConnector sslConnector = null;
        connector.setPort(Integer.getInteger(JETTY_PORT_NAME, JETTY_PORT_DEFAULT));
        connector.setMaxIdleTime(Integer.getInteger("jettyMaxIdle", JETTY_MAX_IDLE));

        if( Integer.getInteger(JETTY_SSL_PORT_NAME) != null ) {
            sslConnector = new SslSelectChannelConnector();
            sslConnector.setPort(Integer.getInteger(JETTY_SSL_PORT_NAME));
            sslConnector.setKeyPassword(System.getProperty(JETTY_SSL_KEY_PASSWORD_NAME));
            String keystoreFile = System.getProperty(JETTY_SSL_KEY_STOREFILE_NAME);
            if (keystoreFile != null && keystoreFile != "") {
	            sslConnector.setKeystore(keystoreFile);
            }
        }
        String tempDir = System.getProperty("jettyTempDir");

        Thread.currentThread().setContextClassLoader(WebAppClassLoader.class.getClassLoader());

        ProtectionDomain protectionDomain = WebServerStart.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();

        WebAppContext context = new WebAppContext();
        WebAppClassLoader webAppClassLoader = new WebAppClassLoader(WebServerStart.class.getClassLoader(),context);
        context.setClassLoader(webAppClassLoader);
        context.setContextPath(URIUtil.SLASH);
        context.setWar(location.toExternalForm());

        if( tempDir != null ) {
            File tempDirectory = new File(tempDir);
            context.setTempDirectory(tempDirectory);
        }

        Server server = new Server();
        if( sslConnector != null ) {
            server.setConnectors(new Connector[]{connector,sslConnector});
        }
        else {
            server.setConnectors(new Connector[]{connector});
        }
        server.setHandler(context);
        server.setSendServerVersion(false);

        try {
            server.start();
            server.join();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}


/**import org.eclipse.jetty.webapp.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.*;
import java.io.File;
import java.net.*;
import java.security.ProtectionDomain;



public class WebServerStart  {
	
		
		static void main(String[] args) {	
		
			try {

				System.out.println("Entering main");		
		
			
				ProtectionDomain protectionDomain = WebServerStart.class.getProtectionDomain();
				URL location = protectionDomain.getCodeSource().getLocation();	
				System.out.println("Locations found");		
				URL [] locations = new URL[1];
				locations[0] = location;			
				Thread.currentThread().setContextClassLoader(new URLClassLoader(locations));			
				System.out.println("Locations set for current thread's classloader");		
			
				//SelectChannelConnector scc = new SelectChannelConnector();
				//scc.setPort(8080);
				Server server = new Server();
				//SelectChannelConnector[] sccs = new SelectChannelConnector[1];
				//sccs[0] = scc; 
				//server.setConnectors(sccs);
				System.out.println("Connectors created and set");
						



				WebAppContext context = new WebAppContext();
				System.out.println("WebAppContext created");
				context.setServer(server);
				context.setContextPath ("/");

				//WebAppClassLoader webAppClassLoader = new WebAppClassLoader(context);
				//Thread.currentThread().setContextClassLoader(webAppClassLoader);
				//System.out.println("webAppClassLoader created and current thread updated");
			
				//context.setClassLoader(webAppClassLoader);

						
				context.setWar(location.toExternalForm());
				System.out.println("war set");
				server.setHandler(context);
				System.out.println("server handler set");



				System.out.println("Starting Jetty");
			    server.start();
				System.out.println("Calling join");
			    server.join();
			
			} catch (Exception e){
			 	e.printStackTrace();
			}
		}
}*/