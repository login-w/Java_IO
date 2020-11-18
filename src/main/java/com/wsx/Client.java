package com.wsx;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 5050);
            Scanner input = new Scanner(System.in);
            byte[] bytes = new byte[1024];
            while (true){
                System.out.println("请输入待发送信息:");
                String next = input.next();
                if (next!=null){
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(next.getBytes());
                    int read = socket.getInputStream().read(bytes);
                    System.out.println(new String(bytes,0,read));
                }else{
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
