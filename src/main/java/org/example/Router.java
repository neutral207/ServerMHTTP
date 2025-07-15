package org.example;

import java.io.IOException;

public class Router {
    private final StaticFileHandler staticFileHandler;

    public Router(StaticFileHandler staticFileHandler) {
        this.staticFileHandler = staticFileHandler;
    }
    public void handle(String path, ClientHandler handler) throws IOException {
        if(path.startsWith("/api/fetch")) {
            ApiHandler apiHandler = new ApiHandler();
            apiHandler.handle(path, handler);
        }else{
            staticFileHandler.serve(path, handler.getOutputStream());
        }
        StaticFileHandler staticFormHandler = new StaticFileHandler("src/main/resources");
        if(path.equals("/") || path.equals("/index.html")){

            staticFormHandler.serve("/form.html", handler.getOutputStream());
        }
    }
}
