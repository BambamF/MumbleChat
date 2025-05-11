package com.mumble.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A ChatServer provides methods to create and start a server via a ServerSocket
 */
public class ChatServer implements Runnable{

    private List<ClientHandler> clients = new ArrayList<>();
    public static final int PORT = 8080;
    
    /**
     * Start the server via the declared port
     */
    @Override
    public void run(){
        System.out.println("Starting server on port: " + PORT);

        // Initialise a new socket server
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            
            while(true){

                // accept connection requests from client sockets
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected to server.");

                // add the new connected client to the list of connected clients and start a new thread
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