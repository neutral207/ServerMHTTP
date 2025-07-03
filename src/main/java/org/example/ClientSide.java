package org.example;

import java.io.IOException;
import java.io.*;
import java.net.*;


public class ClientSide {
    public static void main(String[] args){
        String hostname = "localhost";
        int port = 8080;


        try(Socket socket = new Socket(hostname, port)){
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter URL: ");
            String url = userInput.readLine();
            out.println(url);

            out.println("GET /fetch?url=" + url + "HTTP/1.1");
            out.println("Host: " + hostname);
            out.println();


            String info;

            System.out.println("\n Server's Response: ");
          while((info = in.readLine())  != null){
              System.out.println(info);
          }

        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}