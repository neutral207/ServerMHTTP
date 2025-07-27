package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


public class TemplatingEngine {
    public static String renderTemplate(String templateName, Map<String, String> data) throws IOException {
        try {
            Logger.info("Attempting to load template from classpath: " +  templateName);
            Path path = Paths.get("templates", templateName);
            Logger.info("Template loaded successfully.");
            String tempContent = Files.readString(path, StandardCharsets.UTF_8);

            for (Map.Entry<String, String> entry : data.entrySet()) {
                tempContent = tempContent.replace("${" + entry.getKey() + "}", entry.getValue());
            }
            return tempContent;
        } catch (IOException e) {
            Logger.error(e.getMessage());
            return "<h1>Template not found: " + templateName + "</h1>";
        }
    }
}
