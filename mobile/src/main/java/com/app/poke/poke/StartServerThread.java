package com.app.poke.poke;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Sets up server to accept incomming messages
 * prints out the data incomming on that socket
 */
class StartServerThread implements Runnable {

    final MainActivityPhone main;
    ServerSocket socket;
    Socket incommingSocket;
    String ip;

    StartServerThread(MainActivityPhone main, String ip){
        this.ip = ip;
        this.main = main;
    }

    /**
     * Reads from the socket and populates the view
     */
    public void run(){
        try{
            socket = new ServerSocket(1337);

            while(true){

                System.out.println("Server: listening for connection...");
                incommingSocket = socket.accept();
                System.out.println("Server: Connection found");
                InputStream in = incommingSocket.getInputStream();

                //main.uiThreadHandler.post(new UpdateUIThread(line));

                new Thread(new ReadInput(in)).start();
            }

        }catch(Exception e){
            System.out.print(e.toString());
        }
    }

    class UpdateUIThread implements Runnable{

        String message;

        UpdateUIThread(String message){
            this.message = message;
        }

        public void run(){
            main.textView.setText(this.message);
        }
    }

    class ReadInput implements Runnable{

        InputStream in;

        ReadInput(InputStream in){
            this.in = in;
        }

        public void run(){
            BufferedReader br = new BufferedReader(new InputStreamReader(this.in));

            while(true){
                try{
                    String line = br.readLine();
                    System.out.println("Server: Response from client: "+line);
                }catch(Exception e){
                    System.out.println(e.toString());
                }
            }
        }

    }
}
