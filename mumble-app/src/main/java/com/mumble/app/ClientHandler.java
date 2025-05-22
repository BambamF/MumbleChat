package com.mumble.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * A ClientHandler provides a method for clients to broadcast messages
 */
public class ClientHandler implements Runnable{

    private Socket socket;
    private List<ClientHandler> clients;
    private BufferedReader in;
    private PrintWriter out;
    private User user;

    /**
     * Instantiates the client handler
     * @param s the Socket that provides the input and output streams for the connection
     * @param c the list of clients actively connected to the server via the socket
     */
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

                // associate login messages with a new user
                if(msg.split("::")[0].equals("LOGIN")){
                    setUser(new User(msg.split("::")[1], 0));
                }

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

    /**
     * Broadcasts the message to all clients in the client list via the socket output stream
     * @param msg the message to be broadcasted as a String
     */
    private void broadcast(String msg){
        for(ClientHandler client : clients){
            
            // send the message via the output stream
            client.out.println(msg);

            // flush the output stream after
            client.out.flush();
        }
    }

    /**
     * Returns the user object
     * @return the user object
     */
    public User getUser(){
        return this.user;
    }

    /**
     * Sets the user object
     * @param u the user object to be set
     */
    public void setUser(User u){
        this.user = u;
    }

}
