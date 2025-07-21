package org.example;


import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Properties;


public class HttpSServer {
    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("C:/Users/jteam/IdeaProjects/ServerMHTTP/out/artifacts/ServerPreviewMHTTP_jar/config.properties")) {
            prop.load(input);
            System.out.println("Config properties loaded");
        }catch (Exception e){
            System.err.println("Error loading config properties");
        }

        int port = Integer.parseInt(prop.getProperty("HttpsPort", "8443"));
        String keystorePath = prop.getProperty("keystorePath");
        String keystorePassword = prop.getProperty("keystorePassword");

        String staticDir = prop.getProperty("staticDir", "static");
        StaticFileHandler staticFileHandler = new StaticFileHandler(staticDir);
        Router router = new Router(staticFileHandler);

        try{
            KeyStore keys = KeyStore.getInstance("JKS");
            keys.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keys, keystorePassword.toCharArray());

            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory sslsf = ssl.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) sslsf.createServerSocket(port);

            System.out.println("Server listening on port " + port);

                while(true){
                    SSLSocket client = (SSLSocket) serverSocket.accept();
                    System.out.println("Accepted HTTPS connection from " + client.getInetAddress());
                    new Thread(new ClientHandler(client, router)).start();
                }

    }catch (Exception e){
        System.err.println("HTTPS Server Error: " + e.getMessage());
        e.printStackTrace();
        }
    }
}
