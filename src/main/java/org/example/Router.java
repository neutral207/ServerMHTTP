package org.example;

import java.io.IOException;

public class Router {
    private final StaticFileHandler staticFileHandler;

    public Router(StaticFileHandler staticFileHandler) {
        this.staticFileHandler = staticFileHandler;
    }
    public  boolean handle(String path, ClientHandler handler) throws IOException {
        if(path.startsWith("/api")) {
            handler.sendNotFound();
            return true;
        }else{
            return staticFileHandler.serve(path, handler.getOutputStream());
        }
    }
}
