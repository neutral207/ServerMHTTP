package org.example;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.*;

public class StaticFileHandler {
    private final Path staticDir;

    public StaticFileHandler(String root) {
        this.staticDir = Paths.get(System.getProperty("user.dir"), root);
        System.out.println("Static file root: " + staticDir);
    }
    public StaticFileHandler() {
        this.staticDir = Paths.get("src", "main", "resources").toAbsolutePath().normalize();
    }
    public boolean serve(String resource, OutputStream rawOut) throws IOException {
        System.out.println("Serving static file: " + resource);
        if(resource.equals("/")){
            resource = "/index.html";
        }

        String cleanPath = resource.startsWith("/") ? resource.substring(1) : resource;
        System.out.println("Resolved path: " + cleanPath);
        System.out.println("Current working directory: " + System.getProperty("user.dir"));


        try (InputStream input = getClass().getClassLoader().getResourceAsStream("static/" + cleanPath)) {
            if(input == null){
                System.out.println("Resource not found: " + resource);
                try(InputStream notFoundStream = getClass().getClassLoader().getResourceAsStream("static/404.html")) {
                    if(notFoundStream != null){
                        byte[] notFoundData = notFoundStream.readAllBytes();
                        String contentType = "text/html";

                        PrintWriter out = new PrintWriter(new OutputStreamWriter(rawOut));
                        out.println("HTTP/1.1 404 Not Found");
                        out.println("Content-Type: " + contentType);
                        out.println("Content-Length: " + notFoundData.length);
                        out.println();
                        out.flush();

                        rawOut.write(notFoundData);
                        rawOut.flush();
                        System.out.println("404 page served");
                    }else{
                        write404(rawOut);
                    }
                }catch(Exception e){
                    System.err.println("Error serving 404 page: " + e.getMessage());
                    write500(rawOut);
                }
                return true;
            }

            byte[] fileData = input.readAllBytes();
            String contentType = URLConnection.guessContentTypeFromName(cleanPath);
            if (contentType == null){ contentType = "application/octet-stream";}


                PrintWriter out = new PrintWriter(rawOut, true);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: " + contentType);
                out.println("Content-Length: " + fileData.length);
                out.println();
                out.flush();

                rawOut.write(fileData);
                rawOut.flush();
                System.out.println("File served successfully.");
                return true;

        }   catch(IOException e){
            System.err.println("Failed to serve: " + cleanPath + " - " + e.getMessage());
            return false;
        }


    }


    private void write404(OutputStream out) throws IOException {
        String body = "<h1>404 Not Found</h1>";
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write("HTTP/1.1 404 Not Found\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("Content-Length: " + body.length() + "\r\n");
        writer.write("\r\n");
        writer.write(body);
        writer.flush();
    }

    private void write500(OutputStream out) throws IOException {
        String body = "<h1>500 Internal Server Error</h1>";
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write("HTTP/1.1 500 Internal Server Error\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("Content-Length: " + body.length() + "\r\n");
        writer.write("\r\n");
        writer.write(body);
        writer.flush();
    }
    private String getFileExtension(String fileName){
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }
}

