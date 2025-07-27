package org.example;

import java.io.*;

public class Router {
    private final StaticFileHandler staticFileHandler;

    public Router(StaticFileHandler staticFileHandler) {
        this.staticFileHandler = staticFileHandler;

    }
    public void handle(String path, String method, String body,ClientHandler handler) throws IOException {
        Logger.debug("Received request: " + method + " " + path);
        Logger.info("Received request for path: " + path);
        OutputStream out = handler.getOutputStream();
        if(path.startsWith("/api/v1/fetch")) {
            Logger.debug("Routing to API handler");
            new ApiHandlerV1().handle(path, handler);

        }else if (path.equals("/")){
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
                handleGet(handler.toString(), out);
            }else if (!method.equals("GET") && !method.equals("POST")) {
               staticFileHandler.writeError(405, "Method Not Allowed", out);
               return;
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

    private void handleGet(String requestPath, OutputStream out) {
        String[] parts = requestPath.split("\\?", 2);
        String query = parts.length > 1 ? parts[1] : "";
        String name = getQueryParam(query, "name");
        String message = getQueryParam(query, "message");

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
