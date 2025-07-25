package org.example;

import java.io.*;

public class Router {
    private final StaticFileHandler staticFileHandler;

    public Router(StaticFileHandler staticFileHandler) {
        this.staticFileHandler = staticFileHandler;

    }
    public void handle(String path, ClientHandler handler) throws IOException {
        System.out.println("Received request for path: " + path);
        OutputStream out = handler.getOutputStream();
        if(path.startsWith("/api/fetch")) {
            System.out.println("Routing to API handler");
            ApiHandler apiHandler = new ApiHandler();
            apiHandler.handle(path, handler);

        }else if (path.equals("/")){
            staticFileHandler.serve("/index.html", handler.getOutputStream());
        }
        else if (path.equals("/form.html")){
            System.out.println("Routing to static file handler");
           boolean served = staticFileHandler.serve(path, out);
            System.out.println("Serve success: " + served);
           if(!served){
               write404(out);
           }
        }else if(path.equals("/submit")) {
           if(method.equalsIgnoreCase("POST")){
            handlePost(request, out);
         }else if(method.equalsIgnoreCase("GET")){
                handleGet(request, out);
            }else{
                write405(out);
            }

        }else{
            if (!staticFileHandler.serve(path, handler.getOutputStream())) {
              //   Send 404 only if file wasn't served
                System.out.println("Static file not found: " + path);
                System.out.println("Unknown route. Sending 404.");
                write404(out);
            }

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

    private void handlePost(ClientHandler request, OutputStream out) throws IOException {
        String name = request.getParameter("name");
        String message = request.getParameter("message");

        String body = "<h1>Hello " + escapeHtml(name) + "!</h1><p>You said: '" + escapeHtml(message) + "'</p>";
        sendHtmlResponse(out, "200 OK", body);
    }

    private void handleGet(ClientHandler request, OutputStream out) throws IOException {
        String name = request.getQueryParam("name");
        String message = request.getQueryParam("message");

        String body = "<h1>Hello " + escapeHtml(name) + "!</h1><p>You said: '" + escapeHtml(message) + "'</p>";
        sendHtmlResponse(out, "200 OK", body);
    }

    private void sendHtmlResponse(OutputStream out, String status, String body) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 " + status);
        writer.println("Content-Type: text/html; charset=UTF-8");
        writer.println("Content-Length: " + body.getBytes().length);
        writer.println();
        writer.print(body);
        writer.flush();
    }

    private String escapeHtml(String input) {
        return input == null ? "" : input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
