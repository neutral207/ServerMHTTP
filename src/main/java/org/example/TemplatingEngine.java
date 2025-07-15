package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


public class TemplatingEngine {
    public String renderTemplate(Map<String, String> data) throws IOException {
        String tempContent = Files.readString(Path.of("src/main/resources/template.html"));
        for(Map.Entry<String, String> entry : data.entrySet()){
            String placeHolder = "{{" + entry.getKey() + "}}";
            tempContent = tempContent.replace(placeHolder, entry.getValue());
        }
        return tempContent;
    }
}
