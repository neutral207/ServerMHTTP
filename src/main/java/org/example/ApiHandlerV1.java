package org.example;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class ApiHandlerV1 {

    public void handle(String path, ClientHandler handler) {
        HttpServer.incrementRequestCount();
        if (path.equals("/api/v1/status")) {
            handleStatus(handler.getOutputStream());
        }
        try {
            String urlP = extractQueryParam(path, "url");
            String formatP = extractQueryParam(path, "format");

            if (urlP == null || urlP.isEmpty()) {
                sendJSONError(handler.getOutputStream(), 400,"Missing 'url' query parameter");
                return;
            }

            String title;
            String desc = "N/A";

            try (CloseableHttpClient hClient = HttpClients.createDefault()) {
                HttpGet req = new HttpGet(urlP);
                try (CloseableHttpResponse response = hClient.execute(req)) {
                    String html = EntityUtils.toString(response.getEntity());
                    Document docu = Jsoup.parse(html);
                    title = docu.title();
                    try {
                        var metaTag = docu.select("meta[name=description").first();
                        if (metaTag != null) {
                            desc = metaTag.attr("content");
                        }
                    } catch (Exception e) {
                        desc = "N/A";
                    }
                    sendJSON(handler.getOutputStream(), title, desc);
                }
            }catch (Exception e) {
                sendJSONError(handler.getOutputStream(), 500,"Failed to fetch or parse: " + e.getMessage());
                return;
            }

            if("html".equals(formatP)){
                renderHtmlResponse(handler.getOutputStream(), title, desc);
            }
        } catch (Exception e) {
            sendJSONError(handler.getOutputStream(),500, "Server Error: " + e.getMessage());
        }
    }


    public void handleStatus(OutputStream out) {
        long uptimeMillis = System.currentTimeMillis() - HttpServer.START_TIME;
        long requestCount = HttpServer.getRequestCount();

        Runtime runtime = Runtime.getRuntime();
        long memoryUsed =  runtime.totalMemory() - runtime.freeMemory();
        long memoryTotal =  runtime.totalMemory();

        String json = String.format("""
                {
                "uptimeMillis": %d,
                "uptimeSeconds": %.2f,
                "totalRequests": %d,
                "memoryUsedBytes": %d,
                "memoryTotalBytes": %d,
                }
                """, uptimeMillis, uptimeMillis / 1000.0,  requestCount, memoryUsed, memoryTotal);

        PrintWriter writer = new PrintWriter(out);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json; charset=utf-8");
        writer.println("Content-Length: " + json.getBytes().length);
        writer.println();
        writer.println(json);
        writer.flush();

    }

    private String extractQueryParam(String path, String key){
        try{
            int index =   path.indexOf("?");
            if(index == -1)return null;

            String query = path.substring(index+1);
            String[] params = query.split("&");
            for(String param : params){
                String[] kv = param.split("=");
                if(kv.length == 2 && kv[0].equals(key)){
                    return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e){
            return null;
        }
            return null;    }

    private void sendJSON(OutputStream out, String title, String desc) {
        PrintWriter writer = new PrintWriter(out);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println();
        writer.println(JsonUtil.formatSuccess(Map.of("title", title, "desc", desc)));
        writer.flush();
    }

    private void sendJSONError(OutputStream out, int code, String message)  {
        String json = String.format("{\"error\": \"%s\", \"code\": %d}", message, code);
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 " + code + " " + getStatusText(code));
        writer.println("Content-Type: application/json; charset=UTF-8");
        writer.println("Content-Length: " + json.getBytes().length);
        writer.println();
        writer.print(json);
        writer.flush();
    }
    private void renderHtmlResponse(OutputStream out, String title, String desc){
        try{
            Map<String, String> data = Map.of(
                    "title", title,
                    "description", desc
            );
           // String rendered = TemplatingEngine.renderTemplate("template.html", data);

            PrintWriter writer = new PrintWriter(out, true);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println();
         //   writer.println(rendered);
            writer.flush();
        } catch (Exception e){
            sendJSONError(out,400, "Template Error: " + e.getMessage());
        }
    }
    private static String getStatusText(int code) {
        return switch (code) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 204 -> "No Content";
            default -> "Internal Server Error";
        };
    }
}

