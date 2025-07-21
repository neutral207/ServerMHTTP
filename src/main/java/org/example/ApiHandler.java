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


public class ApiHandler {

    public void handle(String path, ClientHandler handler) throws IOException {

        try {
            String urlP = extractQueryParam(path, "url");
            String formatP = extractQueryParam(path, "format");

            if (urlP == null || urlP.isEmpty()) {
                sendError(handler.getOutputStream(), "Missing 'url' query parameter");
                return;
            }

            String title = "N/A";
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
                sendError(handler.getOutputStream(), "Failed to fetch or parse: " + e.getMessage());
                return;
            }

            if("html".equals(formatP)){
                renderHtmlResponse(handler.getOutputStream(), title, desc);
            }else{
                sendJSON(handler.getOutputStream(), title, desc);
            }
        } catch (Exception e) {
            sendError(handler.getOutputStream(), "Server Error: " + e.getMessage());
        }
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
        writer.println("{\"title\":\"" + escapeJSON(title) + "\",\"description\":\"" + escapeJSON(desc) + "\"}");
        writer.flush();
    }

    private void sendError(OutputStream out, String message)  {
        PrintWriter pw = new PrintWriter(out, true);
        pw.println("HTTP/1.1 400 Bad Request");
        pw.println("Content-Type: application/json");
        pw.println();
        pw.println("{");
        pw.println("  \"error\": \"" + escapeJSON(message) + "\"");
        pw.println("}");
        pw.flush();
    }
    private void renderHtmlResponse(OutputStream out, String title, String desc){
        try{
            TemplatingEngine engine = new TemplatingEngine();
            Map<String, String> data = Map.of(
                    "title", title,
                    "description", desc
            );
            String rendered = engine.renderTemplate(data);

            PrintWriter writer = new PrintWriter(out, true);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println();
            writer.println(rendered);
            writer.flush();
        } catch (Exception e){
            sendError(out, "Template Error: " + e.getMessage());
        }
    }
    private String escapeJSON(String str){
        return str.replace("\"", "\\\"");
    }

}
