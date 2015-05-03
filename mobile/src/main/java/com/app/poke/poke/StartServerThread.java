package com.app.poke.poke;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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


            while(true){
                try{
                   while(this.in.available() >= 0){
                    readFile(this.in);
                   }

                }catch(Exception e){
                    System.out.println(e.toString());
                }
            }
        }

        public void readFile(InputStream inputStream){

            OutputStream outputStream = null;

            try {

                // write the inputStream to a FileOutputStream
                outputStream = new FileOutputStream(new File("audio.mp3"));

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                System.out.println("Done!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
