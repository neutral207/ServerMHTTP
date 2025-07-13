package org.example;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class ApiHandler {

    public void handle(String path, ClientHandler handler) throws IOException {

        try{
            String query = extractQueryParam(path, "url");
            if(query == null || query.isEmpty()){
                sendError(handler.getOutputStream(), "Missing 'url' query parameter");
                return;
            }

            String title = "N/A";
            String desc = "N/A";

            try(CloseableHttpClient hClient = HttpClients.createDefault()){
                HttpGet req = new HttpGet(query);
                try(CloseableHttpResponse response = hClient.execute(req)){
                    String html = EntityUtils.toString(response.getEntity());
                    Document docu = Jsoup.parse(html);
                    title = docu.title();
                    if(docu.select("meta[name=description").first() != null){
                        desc = Objects.requireNonNull(docu.select("meta[name=description").first()).attr("content");
                    }
                } catch (Exception e) {
                    sendError(handler.getOutputStream(), "Failed to fetch or parse: " + e.getMessage());
                    return;
                }

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
        pw.println("HTTP1.1 400 Bad Request");
        pw.println("Content-Type: application/json");
        pw.println();
        pw.println("{");
        pw.println("  \"error\": \"" + escapeJSON(message) + "\"");
        pw.println("}");
        pw.flush();
    }

    private String escapeJSON(String str){
        return str.replace("\"", "\\\"");
    }

}
