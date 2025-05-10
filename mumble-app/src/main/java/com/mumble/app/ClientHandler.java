package com.mumble.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable{

    private Socket socket;
    private List<ClientHandler> clients;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket s, List<ClientHandler> c){
        this.socket = s;
        this.clients = c;
    }
    
    @Override
    public void run(){

        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String msg;
            while((msg = in.readLine()) != null){
                System.out.println("Message received: " + msg);
                broadcast(msg);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        finally{
            try{
                clients.remove(this);
                socket.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

    }

    private void broadcast(String msg){
        for(ClientHandler client : clients){
            client.out.println(msg);
            client.out.flush();
        }
    }
}
