package com.app.poke.poke;

import java.io.OutputStream;
import java.net.Socket;

class ConnectToSocket implements Runnable {

    MainActivityPhone main;

    ConnectToSocket(MainActivityPhone main){
        this.main = main;
    }

    public void run(){
        try{

            // Setup socket and share its output stream
            // share this with the UI thread and bind to click
            // for sending.
            System.out.println("Connecting to "+PokeConfig.MACBOOK_IP);
            this.main.socket = new Socket(PokeConfig.MACBOOK_IP, 1338);
            if( this.main.socket.isConnected() ){
                System.out.println("Is Connected");
            }
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
}
