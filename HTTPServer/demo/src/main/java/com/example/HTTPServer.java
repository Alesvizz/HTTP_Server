package com.example;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

public class HTTPServer {

    private ServerSocket serverSocket;
    private DataOutputStream outVersoClinet;
    private BufferedReader inDalClient;
    private Socket clientSocket;
    private File searchFile;
    private String request;
    private byte[] content;
    private URI uri;
    private URL url;

    public void start() throws IOException, URISyntaxException {

        this.serverSocket = new ServerSocket(8000);
        System.out.println("Server in ascolto sulla porta 8000");

        while (true) {

            // Richiesta
            this.clientSocket = serverSocket.accept();
            System.out.println("Client Connesso");
            this.inDalClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.outVersoClinet = new DataOutputStream(clientSocket.getOutputStream());

            // Legge richiesta e stampa in console
            request = inDalClient.readLine();
            System.out.println(request + " --> Richiesta");

            // Se richiesta è vuota esce
            if (request.isEmpty()) {
                break;

            } else {

                // Splitta la richiesta per trovare URI
                String[] arrayStrings = request.split(" ");
                System.out.println(arrayStrings[1] + " --> Directory");

                //Salva path completo e controlla se presente

                String fileName = arrayStrings[1];
                if (fileName.endsWith("/")) {
                    fileName += "index.html"; //default file
                }
                if (fileName.startsWith("/")) {
                    fileName = fileName.substring(1); // rimuovo / iniziale
                }

                url = getClass().getClassLoader().getResource(fileName);

                if(url != null) searchFile = new File(url.getPath());
                
                if (url != null && searchFile.exists()) {
                    // IL FILE ESISTE

                    // TUTTI ALTRI CASI
                    String extension = fileName.substring((fileName.lastIndexOf(".") + 1));

                    //String directory = arrayStrings[1].replaceFirst("/", "");
                    content = this.readFileAsByte(fileName);
                    this.sendResponse(outVersoClinet, content, extension);
                    
                } else {

                    // FILE NON ESISTE, ERRORE 404
                    content = this.readFile("errore404.html", "html");
                    sendResponseNotFound(outVersoClinet, content);
                    System.out.println("Error 404: Resource not found");
                }

            }

            this.inDalClient.close();
            this.outVersoClinet.close();

        }

        try {
            this.close();
        } catch (Exception e) {
            System.out.println("Errore nella chiusura del socket");
        }
    }

    public void sendResponse(DataOutputStream out, byte[] content, String extension) {
        try {
            int contentLenght = content.length;
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            switch (extension) {
                case "html":
                case "htm":
                    out.write("Content-Type: text/html\r\n".getBytes());
                    break;
                case "png":
                    out.write("Content-Type: image/png\r\n".getBytes());
                    break;
                case "jpeg":
                    out.write("Content-Type: image/jpeg\r\n".getBytes());
                    break;
                case "css":
                    out.write("Content-Type: text/css\r\n".getBytes());
                    break;
                case "js":
                    out.write("Content-Type: text/javascript\r\n".getBytes());
                    break;
                case "json":
                    out.write("Content-Type: application/json\r\n".getBytes());
                    break;
                case "xml":
                    out.write("Content-Type: application/xml\r\n".getBytes());
                    break;
                }
            out.write(("Content-Length: " + contentLenght + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            out.write(content);
            out.flush();
        } catch (IOException e) {
            System.out.println("Errore invio risposta " + e.getMessage());
        }
    }


    public void sendResponseNotFound(DataOutputStream out, byte[] content) {
        try {
            out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
            out.write("Content-Type: text/html\r\n".getBytes());
            out.write(("Content-Length: " + content.length + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            out.write(content);
            //out.write("ERROR 404: Not Found".getBytes());
        } catch (IOException e) {
            System.out.println("Errore invio risposta " + e.getMessage());
        }
    }

    public void close() {
        try {
            this.inDalClient.close();
            this.outVersoClinet.close();
            this.clientSocket.close();
        } catch (IOException e) {
            System.out.println("Errore chiusura server " + e.getMessage());
        }
    }

    public byte[] readFileAsByte(String fileName) throws IOException, URISyntaxException {

        URL url = getClass().getClassLoader().getResource(fileName);

        File file = new File(url.toURI());

        byte[] bytes = Files.readAllBytes((file.toPath()));

        return bytes;
    } 


    public byte[] readFile(String fileName, String extension) throws IOException, URISyntaxException {

        if (extension.equals("html") || extension.equals("css") || extension.equals("js")) {
            InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
            StringBuilder resultStringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultStringBuilder.toString().getBytes();
        } else if (extension.equals("png") || extension.equals("jpeg")) {
            
            return readFileAsByte(fileName);
        } else {
            return null;
        }
    }
}
