package com.tsalachsnow.chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private static ServerSocket server;
    private static Socket client;
    private static ExecutorService pool;
    private static boolean isRunning = false;

    private static List<ConnectionHandler> connections = new ArrayList<>();

    public void Server(){
        connections = new ArrayList<>();
        isRunning = false;
    }
    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!isRunning) {
                client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public static void shutdown(){
        try {
            isRunning = true;
            pool.shutdown();
            if(!server.isClosed()){
                server.close();
            }
            for(ConnectionHandler handler : connections){
                if(handler != null && !handler.client.isClosed()){
                    handler.shutdown1();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcast(String message){
        for(ConnectionHandler handler : connections){
            if(handler != null && !handler.client.isClosed()){
                handler.sendMessage(message);
            }
        }
    }

    static class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;


        private String request;
        public ConnectionHandler(Socket client){
            this.client = client;
        }
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                out.println("Please enter nickname");
                request = in.readLine();
                System.out.println("Nickname connected:: " + request);
                broadcast(request + " has joined the chatroom");
                String message;
                while((message = in.readLine()) != null){
                    if(message.startsWith("/nick")){
                        String[] newNick = message.split("",2);
                        if(newNick.length == 2){
                            broadcast(request + " is now known as " + newNick[1]);
                            System.out.println(request + " is now known as " + newNick[1]);
                            request = newNick[1];
                           out.println("You are now known as " + request);
                        }else{
                            out.println("Invalid nickname");
                        }
                    }else if(message.startsWith("/quit")){
                        System.out.println(request + " has left the chatroom");
                        broadcast(request + " has left the chatroom");
                        shutdown();
                    }else{
                        broadcast(request + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }


        public void shutdown1() throws IOException {
            try {
                in.close();
                out.close();
            if(!client.isClosed()){

                    client.close();
                }
            } catch (IOException e) {
                    throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
