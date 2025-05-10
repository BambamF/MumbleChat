package com.mumble.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer implements Runnable{

    private List<ClientHandler> clients = new ArrayList<>();
    public static final int PORT = 8080;
    
    @Override
    public void run(){
        System.out.println("Starting server on port: " + PORT);
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected to server.");
                ClientHandler handler = new ClientHandler(clientSocket, clients);
                clients.add(handler);
                new Thread(handler).start();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}