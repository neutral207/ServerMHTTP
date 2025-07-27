package org.example;

import java.io.FileInputStream;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

public class HttpServer {
    public static final long START_TIME = System.currentTimeMillis();
    public static final AtomicLong requestCount = new AtomicLong(0);

    public static void main(String[] args) {

        InputStream pretest = ClassLoader.getSystemClassLoader().getResourceAsStream("index.html");
        if (pretest == null) {
            Logger.debug("index.html NOT found in classpath.");
        } else {
            Logger.info("index.html FOUND in classpath.");
        }

        InputStream test = ClassLoader.getSystemClassLoader().getResourceAsStream("form.html");
        if (test == null) {
            Logger.debug("form.html NOT found in classpath.");
        } else {
            Logger.info("form.html FOUND in classpath.");
        }


        Properties props = new Properties();
        try(InputStream input = new FileInputStream("C:/Users/jteam/IdeaProjects/ServerMHTTP/out/artifacts/ServerPreviewMHTTP_jar/config.properties")){
            props.load(input);
            Logger.info("Configuration file loaded.");
        }
        catch(IOException ex){
            Logger.error("Error loading configuration file.");
        }

        int port = Integer.parseInt(props.getProperty("port", "8080"));
        String staticDir = props.getProperty("staticDir", "static");

        StaticFileHandler staticFileHandler = new StaticFileHandler(staticDir);
        Router router = new Router(staticFileHandler);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Logger.debug("HTTP Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, router);
                Logger.info("Accepted connection from " + clientSocket.getInetAddress());
                new Thread(handler).start();
            }

        } catch (IOException e) {
            Logger.error("Server exception: " + e.getMessage());
        }
    }

    public static void  incrementRequestCount(){
        requestCount.incrementAndGet();
    }
    public static long getRequestCount(){
        return requestCount.get();
    }
}


// default homepage page: http://localhost:8080/

// fetching metadata: http://localhost:8080/api/v1/fetch?url=https://example.com or typing a valid URL into the box

// template rendering: http://localhost:8080/api/v1/fetch?url=https://example.com&format=html
