package org.example;

import java.io.FileInputStream;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    public static void main(String[] args) {

        InputStream test = ClassLoader.getSystemClassLoader().getResourceAsStream("form.html");
        if (test == null) {
            System.out.println("form.html NOT found in classpath.");
        } else {
            System.out.println("form.html FOUND in classpath.");
        }


        Properties props = new Properties();
        try(InputStream input = new FileInputStream("C:/Users/jteam/IdeaProjects/ServerMHTTP/out/artifacts/ServerPreviewMHTTP_jar/config.properties")){
            props.load(input);
            System.out.println("Configuration file loaded.");
        }
        catch(IOException ex){
            System.err.println("Error loading configuration file.");
        }

        int port = Integer.parseInt(props.getProperty("port", "8080"));
        String staticDir = props.getProperty("staticDir", "static");

        StaticFileHandler staticFileHandler = new StaticFileHandler(staticDir);
        Router router = new Router(staticFileHandler);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HTTP Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, router);
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }
}


// default page: http://localhost:8080/
// fetching metadata: http://localhost:8080/api/fetch?url=https://example.com
// template rendering: http://localhost:8080/api/fetch?url=https://example.com&format=html
