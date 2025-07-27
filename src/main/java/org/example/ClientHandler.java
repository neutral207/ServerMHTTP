package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.*;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Router router;
    private final OutputStream rawOut;
    private final StaticFileHandler staticFileHandler;


    public ClientHandler(Socket clientSocket, Router router) throws IOException {
        this.clientSocket = clientSocket;
        this.router = router;
        this.rawOut = clientSocket.getOutputStream();
        this.staticFileHandler = new StaticFileHandler("static");
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String requests = in.readLine();
            if (requests == null || requests.isEmpty()) {
                return;
            }
            String[] tokens = requests.split(" ");
            String method = tokens[0];
            String path = tokens[1];

            String line;
            int contentLength = 0;
            while (!(line = in.readLine()).isEmpty()) {
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }


            StringBuilder bodyBUILDER = new StringBuilder();
            if (method.equals("POST") && contentLength > 0) {
                for (int i = 0; i < contentLength; i++) {
                    bodyBUILDER.append((char) in.read());
                }
            }

            String requestBody = bodyBUILDER.toString();
            Logger.info("Client Request: " + requests);


            if (method.equals("GET") || method.equals("POST")) {
                router.handle(path, method, requestBody, this);
            } else if (method.equals("HEAD")) {
                handleHead(path);
            } else if (method.equals("OPTIONS")) {
                sendOptionsResponse();
            } else {
                staticFileHandler.writeError(405, "Requested Method is not allowed", rawOut);
            }

        } catch (IOException e) {
            Logger.error("Error Handling client request: " + e.getMessage());
            Logger.error("[" + java.time.LocalDateTime.now() + "] Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                Logger.error("Error closing client socket: " + ex.getMessage());
                Logger.error("[" + java.time.LocalDateTime.now() + "] Error: " + ex.getMessage());
            }
        }
    }

    private void handleHead(String resource) throws IOException {
        if (resource.equals("/")) {
            resource = "/index.html";
        }

        Path filePath = Paths.get(System.getProperty("user.dir"), "static", resource.substring(1)).normalize();

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            String ext = getFileExtension(filePath.getFileName().toString());
            String contentType = MimeTypeMap.get(ext);
            long contentLength = Files.size(filePath);

            PrintWriter out = new PrintWriter(rawOut, true);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + contentLength);
            out.println();
            out.flush();
        } else {
            staticFileHandler.writeError(404, "File Not Found", rawOut);
        }
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index == -1) ? "" : fileName.substring(index + 1).toLowerCase();
    }

    public OutputStream getOutputStream() {
        return rawOut;
    }

    public void sendOptionsResponse() throws IOException {
        PrintWriter out = new PrintWriter(rawOut, true);
        out.println("HTTP/1.1 204 No Content");
        out.println("Allow: GET, HEAD, OPTIONS");
        out.println();
        out.flush();
    }

    public String getPostParam(String body, String key) {
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyVal = pair.split("=");
            if (keyVal.length == 2 && keyVal[0].equals(key)) {
                return java.net.URLDecoder.decode(keyVal[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}

