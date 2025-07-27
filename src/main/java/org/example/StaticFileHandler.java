package org.example;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.*;

public class StaticFileHandler {
    private final Path staticDir;

    public StaticFileHandler(String root) {
        this.staticDir = Paths.get(System.getProperty("user.dir"), root);
        Logger.debug("Static file root: " + staticDir);
    }
    public StaticFileHandler() {
        this.staticDir = Paths.get("src", "main", "resources").toAbsolutePath().normalize();
    }
    public boolean serve(String resource, OutputStream rawOut) throws IOException {
        Logger.debug("Serving static file: " + resource);
        if(resource.equals("/")){
            resource = "/index.html";
        }

        String cleanPath = resource.startsWith("/") ? resource.substring(1) : resource;
        Logger.debug("Resolved path: " + cleanPath);
        Logger.debug("Current working directory: " + System.getProperty("user.dir"));


        try (InputStream input = getClass().getClassLoader().getResourceAsStream("static/" + cleanPath)) {
            if(input == null){
                Logger.error("Resource not found: " + resource);
                writeError(404, "The file requested could not be found.", rawOut);
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
                        Logger.info("404 page served");
                    }else{
                        writeError(404, "An error occurred trying to locate 404 page",rawOut);
                    }
                }catch(Exception e){
                    Logger.error("Error serving 404 page: " + e.getMessage());
                    writeError(500, "An error occurred when trying to serve the 404 page.", rawOut);
                }
                return true;
            }

            byte[] fileData = input.readAllBytes();
            String contentType = URLConnection.guessContentTypeFromName(cleanPath);
            if (contentType == null){
                String ext = getFileExtension(cleanPath);
                contentType = MimeTypeMap.mimeTypes.getOrDefault(ext, "application/octet-stream");
            }


                PrintWriter out = new PrintWriter(rawOut, true);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: " + contentType);
                out.println("Content-Length: " + fileData.length);
                out.println();
                out.flush();

                rawOut.write(fileData);
                rawOut.flush();
                Logger.info("File served successfully.");
                return true;

        }   catch(IOException e){
            Logger.error("Failed to serve: " + cleanPath + " - " + e.getMessage());
            writeError(500, "An error occurred while trying to serve this file. ", rawOut);
            return true;
        }


    }

    public void writeError(int status, String message, OutputStream out) throws IOException {
        String statusText = switch (status) {
            case 404 -> "404 Not Found";
            case 405 -> "405 Method Not Allowed";
            case 400 -> "400 Bad Request";
            case 204 -> "204 No Content";
            default -> "Internal Server Error";
        };

        String body = "<html><body><h1>Error " + status + "</h1><p> " + statusText + "</p></body></html>";

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write("HTTP/1.1 " +  status + " " + statusText + "\r\n");
        writer.write("Content-Type: text/html\r\n");
        writer.write("Content-Length: " + body.getBytes().length + "\r\n");
        writer.write("\r\n");
        writer.write(body);
        writer.flush();
    }

    private String getFileExtension(String fileName){
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }
}

