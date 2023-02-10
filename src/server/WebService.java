package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class WebService implements Runnable{

    Socket client;
    Scanner readFromClient;
    PrintWriter sendToClient;

    /**
     * Constructor sets the client Socket
     * @param client Socket
     */
    public WebService(Socket client){

        this.client = client;

    }

    /**
     * creates a Scanner and PrinterWriter from the client
     * Sends status headers and reads request headers
     * Currently only handles GET requests
     * Returns the lines of the file specified in the GET request
     */
    @Override
    public void run() {
        System.out.println("Client connected from: " + client.getInetAddress() + "  on thread: " + Thread.currentThread().getName());
        try {
            this.readFromClient = new Scanner(client.getInputStream());
            this.sendToClient = new PrintWriter(client.getOutputStream());

            sendToClient.println("HTTP/1.1 200");
            sendToClient.println("Content-Type: text/html");
            sendToClient.println("Connection: close");
            sendToClient.println("");

            List<String> requestHeaders = new ArrayList<>();
            while(readFromClient.hasNextLine()){
                String nextLine = readFromClient.nextLine();
                System.out.println(nextLine);
                if(nextLine.length() == 0){
                    break;
                }else{
                    requestHeaders.add(nextLine);
                }
            }

            String[] argArray = requestHeaders.get(0).split(" ");
            if(argArray[0].compareTo("GET") == 0){
                performService("GET", argArray[1]);
            }else{
                System.out.println("Did not receive GET request.");
                System.exit(-1);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Reads the file with the base path set by the SimpleWebServer WEB_ROOT
    //and the full path created by adding the requestPath per the GET request
    //returns the lines of the file as it reads them
    //if no file is specified, then the lines of index.html (found in the WEB_ROOT) are returned
    private void performService(String command, String requestPath) throws IOException {
        //sendToClient.println("Thank you, come again.");
        //String encodedString = Base64.getEncoder().encodeToString("My test string".getBytes());
        //sendToClient.println(encodedString);
        //String encodedString = "PCFET0NUWVBFIGh0bWw+CjxodG1sIGxhbmc9ImVuIj4KPGhlYWQ+CiAgICA8bWV0YSBodHRwLWVxdWl2PSJDb250ZW50LVR5cGUiIGNvbnRlbnQ9InRleHQvaHRtbDsgY2hhcnNldD11dGYtOCI+ICAKICAgIDxtZXRhIG5hbWU9InZpZXdwb3J0IiBjb250ZW50PSJ3aWR0aD1kZXZpY2Utd2lkdGgsIGluaXRpYWwtc2NhbGU9MS4wIj4gIAogICAgPHRpdGxlPldlbGNvbWU8L3RpdGxlPgo8L2hlYWQ+Cjxib2R5PgogICAgPGRpdj4KICAgICAgICA8aDE+V2VsY29tZSE8L2gxPgogICAgICAgIDxoMT5CaWVudmVuaWRvcyE8L2gxPgogICAgPC9kaXY+CiAgICA8aDE+VGhhbmtzIGZvciB2aXNpdGluZyE8L2gxPgo8L2JvZHk+CjwvaHRtbD4=";
        //System.out.println(encodedString);
        //System.out.println(new String(Base64.getDecoder().decode(encodedString)));
        //sendToClient.println(new String(Base64.getDecoder().decode(encodedString)));

        String filePath = SimpleWebServer.WEB_ROOT;
        if(requestPath.compareTo("/") == 0){
            filePath = filePath + "\\index.html";
        }else{
            filePath = filePath + requestPath;
        }

        try {
            Scanner fileIn = new Scanner(new File(filePath));
            //fileIn.useDelimiter("");
            while(fileIn.hasNext()){
                sendToClient.println(fileIn.nextLine());
            }//end while
        } catch (FileNotFoundException e) {
            System.out.println("File not found. ");
            sendToClient.println("404 File Not Found: "+filePath);
        }

        sendToClient.flush();
        sendToClient.close();
        readFromClient.close();
        client.close();
    }
}

