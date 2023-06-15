package com.tsalachsnow.chatroom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(9999);
            Socket client = server.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class ConnectionHandler implements Runnable{
        private Socket client;
        public ConnectionHandler(Socket client){
            this.client = client;
        }
        @Override
        public void run() {

        }
    }
}
