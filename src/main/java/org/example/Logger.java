package org.example;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



public class Logger {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void info(String message) {
        log("INFO", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void debug(String message) {
        log("DEBUG", message);
    }

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] [" + level + "] " + message );
    }
}
