package org.example;
import java.io.*;
import java.nio.file.*;

public class StaticFileHandler {
    private final Path staticDir;

    public StaticFileHandler(String root) {
        this.staticDir = Paths.get(System.getProperty("user.dir"), root);
    }

    public boolean serve(String resource, OutputStream rawOut) throws IOException {
        if(resource.equals("/")){
            resource = "/index.html";
        }

        Path filePath = staticDir.resolve(resource.substring(1)).normalize();

        if(Files.exists(filePath) && !Files.isDirectory(filePath)){
            String ext = getFileExtension(filePath.getFileName().toString());
            String contentType = MimeTypeMap.get(ext);
            byte[] fileData = Files.readAllBytes(filePath);

            PrintWriter out = new PrintWriter(rawOut, true);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + fileData.length);
            out.println();
            out.flush();

            rawOut.write(fileData);
            rawOut.flush();
            return true;
        }
        return false;
    }

    private String getFileExtension(String fileName){
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }
}

