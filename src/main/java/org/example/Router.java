package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Router {
    private final StaticFileHandler staticFileHandler;
    public Router(StaticFileHandler staticFileHandler) {
        this.staticFileHandler = staticFileHandler;

    }
    public void handle(String path, String method, String body,ClientHandler handler) throws IOException {
        HttpServer.incrementRequestCount();
        Logger.debug("Received request: " + method + " " + path);
        Logger.info("Received request for path: " + path);
        String newPath = path.split("\\?")[0];
        Map<String, String> queryParams = new HashMap<>();
        if(path.contains("?")) {
            String query = path.substring(path.indexOf("?")+1);
                for(String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if(pair.length == 2) {
                        queryParams.put(
                                java.net.URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                                java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                        );
                    }
                }
        }
        OutputStream out = handler.getOutputStream();
        if(path.startsWith("/api/v1/fetch") || path.startsWith("/api/v1/metadata")) {
            Logger.debug("Routing to API handler");
            new ApiHandlerV1().handle(path, handler);

        }else if (path.equals("/api/v1/status")) {
            Logger.debug("Routing to API status endpoint");
            new ApiHandlerV1().handleStatus(handler.getOutputStream());
        }
        else if (path.equals("/")){
            staticFileHandler.serve("/index.html", out);
        }
        else if (path.equals("/form.html")){
            Logger.debug("Routing to static file handler");
           boolean served = staticFileHandler.serve(path, out);
            Logger.debug("Serve success: " + served);
           if(!served){
               staticFileHandler.writeError(404, "Error serving static File: " + path, out);
           }
        }else if(path.equals("/submit")) {
           if(method.equalsIgnoreCase("POST")){
            handlePost(handler, out, body);
         }else if(method.equalsIgnoreCase("GET")){
                handleGet(queryParams, out);
            }else if (!method.equals("GET") && !method.equals("POST")) {
               staticFileHandler.writeError(405, "Method Not Allowed", out);
           }else{
                staticFileHandler.writeError(405, "Requested method is not allowed.", out);
            }

        }else{
            if (!staticFileHandler.serve(path, handler.getOutputStream())) {
              //   Send 404 only if file wasn't served
                Logger.error("Static file not found: " + path);
                Logger.error("Unknown route. Sending 404.");
                staticFileHandler.writeError(404, "Error: Unknown Route: " + path, out);
            }

        }
    }

    private void handlePost(ClientHandler request, OutputStream out, String body) {
        String name = request.getPostParam(body,"name");
        String message = request.getPostParam(body,"message");

        String responseBody = "<h1>Hello " + escapeHtml(name) + "!</h1><p>You said: '" + escapeHtml(message) + "'</p>";
        sendHtmlResponse(out, body);
    }

    private void handleGet(Map<String,String> requestPath, OutputStream out) {

        String name = requestPath.getOrDefault("name", "Guest");
        String message = getQueryParam("message", "No message provided");

        String body = "<h1>Hello " + escapeHtml(name) + "!</h1><p>You said: '" + escapeHtml(message) + "'</p>";
        sendHtmlResponse(out, body);
    }

    private void sendHtmlResponse(OutputStream out, String body) {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 " + "200 OK");
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Content-Length: " + body.getBytes().length);
        writer.println();
        writer.print(body);
        writer.flush();
    }

    private String getQueryParam(String query, String name) {
        for (String param : query.split("&")) {
            String[] parts = param.split("=");
            if(parts.length == 2 && parts[0].equals(name)) {
                return java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }
    private String escapeHtml(String input) {
        return input == null ? "" : input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
