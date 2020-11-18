package com.wsx;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaIO {
    public static void main(String[] args)  throws  Exception{
        ServerSocket serverSocket = new ServerSocket(5050);
        ExecutorService pool = Executors.newCachedThreadPool();
        while (true){
            System.out.println("有人连接我吗？");
            Socket socket = serverSocket.accept();
            System.out.println("客宾一位楼上请.....");

            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Scanner input = new Scanner(System.in);

                        InputStream inputStream = socket.getInputStream();
                        byte[] bytes = new byte[1024];
                        int len=inputStream.read(bytes);
                        while(len!=-1){
                            System.out.println(Thread.currentThread().getName()+"----->"+new String(bytes,0,len));
                            socket.getOutputStream().write("收到 over！".getBytes());
                            len=inputStream.read(bytes);
                        }

                        System.out.println(Thread.currentThread().getName()+"撤离了.....");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


    }
}
