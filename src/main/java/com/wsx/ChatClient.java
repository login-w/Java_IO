package com.wsx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 5050);
        socketChannel.configureBlocking(false);
        socketChannel.connect(inetSocketAddress);
        while (socketChannel.isConnected()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int count=0;
                    try {
                        count = socketChannel.read(byteBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (count>0){
                        System.out.println("收到了信息："+new String(byteBuffer.array()));
                        try {
                            count=socketChannel.read(byteBuffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            Scanner input = new Scanner(System.in);
            while (true){
                System.out.println("请输入待发送的数据：");
                String nextLine = input.nextLine();
                socketChannel.write(ByteBuffer.wrap(nextLine.getBytes()));
            }
        }
    }

}
