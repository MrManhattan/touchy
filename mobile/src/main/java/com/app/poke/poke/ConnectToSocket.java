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
            this.main.socket = new Socket(PokeConfig.MACBOOK_IP, 1337);

        }catch(Exception e){
            System.out.println(e.toString());
        }
    }
}
