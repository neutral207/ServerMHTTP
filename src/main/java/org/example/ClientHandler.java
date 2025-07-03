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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.net.URLDecoder;
import java.io.*;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             OutputStream rawOut = clientSocket.getOutputStream())
        {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            System.out.println("Client Request: " + requestLine);

            // Extract path
            String[] tokens = requestLine.split(" ");
            if (tokens.length < 3) return;
            String method = tokens[0];
            String resource = URLDecoder.decode(tokens[1], StandardCharsets.UTF_8);

            if(!method.equals("GET")){
                sendResponse(out);
            }

            if (resource.startsWith("/fetch?url=")) {
                handleFetchRequest(resource, out);
            } else {
               serveStaticFile(resource, rawOut);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    private void serveStaticFile(String resource, OutputStream rawOut) throws IOException {
        if (resource.equals("/")){
            resource = "/index.html";
        }
        String webRoot = "static";
        Path staticDir = Paths.get(System.getProperty("user.dir"), webRoot);
        Path filePath = staticDir.resolve(resource.substring(1)).normalize();
        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            String contentType = Files.probeContentType(filePath);
            byte[] fileData = Files.readAllBytes(filePath);

            PrintWriter out = new PrintWriter(rawOut, true);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + fileData.length);
            out.println();
            out.flush();

            rawOut.write(fileData);
            rawOut.flush();
        } else{
            PrintWriter out = new PrintWriter(rawOut, true);
            out.println("HTTP/1.1 404 NOT FOUND");
            out.println("Content-Type: text/html");
            out.println("<html><body><h1>404 NOT FOUND</h1></body></html>");
            out.println();
            out.flush();
        }
    }
    private void sendResponse(PrintWriter out){
        out.println("HTTP/1.1 " + "501 not implemented");
        out.println("Content-Type: " + "text/plain");
        out.println("Content-Length: " + "Method not supported.".length());
        out.println();
        out.println("Method not supported.");
    }

}

