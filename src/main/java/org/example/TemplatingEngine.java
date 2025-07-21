package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


public class TemplatingEngine {
    public String renderTemplate(Map<String, String> data) throws IOException {
        System.out.println("Attempting to load template from classpath: templates/template.html");
        InputStream in = getClass().getClassLoader().getResourceAsStream("templates/template.html");
        if(in == null) {
            System.err.println("Template NOT FOUND in classpath!");
            throw new IOException("Template not found in classpath");
        }
        System.out.println("Template loaded successfully.");
        String tempContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        for(Map.Entry<String, String> entry : data.entrySet()){
            String placeHolder = "{{" + entry.getKey() + "}}";
            tempContent = tempContent.replace(placeHolder, entry.getValue());
        }
        return tempContent;
    }
}
