package org.example;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.*;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Router router;
    private final OutputStream rawOut;


    public ClientHandler(Socket clientSocket, Router router) throws IOException{
        this.clientSocket = clientSocket;
        this.router = router;
        this.rawOut = clientSocket.getOutputStream();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))){
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] tokens = requestLine.split(" ");

            String method = tokens[0];
            String path = tokens[1];

            System.out.println("Client Request: " + requestLine);


            switch (method) {
                case "GET" -> router.handle(path, this);
                case "HEAD" -> handleHead(path);
                case "OPTIONS" -> sendOptionsResponse();
                default -> sendMethodNotAllowed();
            }


        } catch (IOException e) {
            System.err.println("Error Handling client request: " + e.getMessage());
            System.err.println("[" + java.time.LocalDateTime.now() + "] ⚠️ Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                System.err.println("Error closing client socket: " + ex.getMessage());
                System.err.println("[" + java.time.LocalDateTime.now() + "] ⚠️ Error: " + ex.getMessage());
            }
        }
    }

    private void handleHead(String resource) throws IOException {
        if(resource.equals("/")){
            resource = "/index.html";
        }

        Path filePath = Paths.get(System.getProperty("user.dir"),  "static", resource.substring(1)).normalize();

        if(Files.exists(filePath) && !Files.isDirectory(filePath)){
            String ext = getFileExtension(filePath.getFileName().toString());
            String contentType = MimeTypeMap.get(ext);
            long contentLength = Files.size(filePath);

            PrintWriter out = new PrintWriter(rawOut, true);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + contentLength);
            out.println();
            out.flush();
        }else{
            sendNotFound();
        }
    }
    private void handleFetchRequest(String resource, PrintWriter out) {
        String url = resource.substring("fetch?url=".length());
        String title;
        String desc = "N/A";

        try (CloseableHttpClient newClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse ourResponse = newClient.execute(request)) {
                String html = EntityUtils.toString(ourResponse.getEntity());
                Document doc = Jsoup.parse(html);

                title = doc.title();
                if (doc.select("meta[name=description]").first() != null) {
                    desc = Objects.requireNonNull(doc.select("meta[name=description]").first()).attr("content");
                }
            }
        } catch (Exception e) {
            title = "Error fetching " + url;
            desc = e.getMessage();
        }
        out.println("HTTP/1.1 200 OK\r\n");
        out.println("Content-Type: text/html; charset=UTF-8\r\n\r\n");
        out.println("<html><body>");
        out.println("<h1>Title: " + title + "</h1>");
        out.println("<p>Description: " + desc + "</p>");
        out.println("</body></html>");
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? "" : fileName.substring(index + 1).toLowerCase();
    }
    public OutputStream getOutputStream() {
        return rawOut;
    }

    public void sendNotFound() {
        PrintWriter out = new PrintWriter(rawOut, true);
        out.println("HTTP/1.1 404 NOT FOUND");
        out.println("Content-Type: " + "text/html");
        out.println();
        out.println("<html><body><h1>404 NOT FOUND</h1></body></html>");
        out.flush();
    }

    public void sendOptionsResponse() throws IOException {
        PrintWriter out = new PrintWriter(rawOut, true);
        out.println("HTTP/1.1 204 No Content");
        out.println("Allow: GET, HEAD, OPTIONS");
        out.println();
        out.flush();
    }

    public void sendMethodNotAllowed() throws IOException {
        PrintWriter out = new PrintWriter(rawOut, true);
        out.println("HTTP/1.1 405 Method Not Allowed");
        out.println("Allow: GET, HEAD, OPTIONS");
        out.println();
        out.flush();
    }
}

