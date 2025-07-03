package org.example;

import java.util.HashMap;
import java.util.Map;


public class MimeTypeMap {
    public static final Map<String, String> mimeTypes = new HashMap<>();

        static
        {
            mimeTypes.put("html", "text/html");
            mimeTypes.put("htm", "text/html");
            mimeTypes.put("css", "text/css");
            mimeTypes.put("js", "application/javascript");
            mimeTypes.put("json", "application/json");
            mimeTypes.put("png", "image/png");
            mimeTypes.put("jpg", "image/jpeg");
            mimeTypes.put("jpeg", "image/jpeg");
            mimeTypes.put("gif", "image/gif");
            mimeTypes.put("svg", "image/svg+xml");
            mimeTypes.put("ico", "image/x-icon");
        }


        public static String get(String extension){
            return mimeTypes.getOrDefault(extension, "application/octet-stream");
        }

}
