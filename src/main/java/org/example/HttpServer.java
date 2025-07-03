package org.example;

import java.io.FileInputStream;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private static final String WEB_ROOT = "static";

    public static void main(String[] args) {
        Properties props = new Properties();
        try(InputStream input = new FileInputStream("config.properties")){
            props.load(input);
            System.out.println("Configuration file loaded.");
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

        int port = Integer.parseInt(props.getProperty("port", "8080"));
        String staticDir = props.getProperty("staticDir", "static");


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HTTP Server started on port " + port);

            while (true) {
                StaticFileHandler staticFileHandler = new StaticFileHandler(staticDir);
                Router router = new Router(staticFileHandler);


                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, router);
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

