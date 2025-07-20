package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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

        }else if (path.equals("/") || path.equals("/index.html")){
            System.out.println("Routing to static file handler");
           boolean served =  staticFileHandler.serve(path, out);
            System.out.println("Serve success: " + served);
           if(!served){
               write404(out);
           }
        }else{
            System.out.println("Unknown route. Sending 404.");
            write404(out);
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
}
