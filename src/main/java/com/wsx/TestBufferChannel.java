package com.wsx;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class TestBufferChannel {
    public static void main(String[] args) throws IOException {

//        ServerSocketChannel channel = new ServerSocket().getChannel();
//        SocketChannel channel1 = new Socket().getChannel();
        //        DatagramChannel datagramChannel = new DatagramSocket().getChannel();


        String str="hello world !";
        FileChannel fileChannel = new FileOutputStream("d:\\file01.txt").getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(str.getBytes());
        byteBuffer.flip();
        fileChannel.write(byteBuffer);
    }

    @Test
//    测试channel、buffer的工作机制
    public void test1() throws IOException {
        File file = new File("d:\\file01.txt");
        FileChannel fileChannel = new FileInputStream(file).getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate((int)file.length());
        fileChannel.read(byteBuffer);
        byteBuffer.flip();
        System.out.println(new String(byteBuffer.array()));
    }
    @Test
//    测试用nio的channel方法，进行数据的复制
    public void test2() throws Exception {
        File file = new File("file01");
        FileChannel channel = new FileInputStream(file).getChannel();
        FileChannel channel1 = new FileOutputStream("file02").getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());
        channel.read(byteBuffer);
        byteBuffer.flip();
        channel1.write(byteBuffer);
    }
    @Test
//    测试nio中channel的transferfrom方法
    public void test3() throws Exception {
        File file = new File("file01");
        FileChannel channel = new FileInputStream(file).getChannel();
        FileChannel channel1 = new FileOutputStream("file02").getChannel();
        channel1.transferFrom(channel,0,channel.size());

    }

//    测试buffer的分散与聚集
    @Test
    public void test4() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(5050);
        serverSocketChannel.socket().bind(inetSocketAddress);

        ByteBuffer[] byteBuffers = new ByteBuffer[2];
        byteBuffers[0]=ByteBuffer.allocate(10);
        byteBuffers[1]=ByteBuffer.allocate(5);

        SocketChannel socketChannel = serverSocketChannel.accept();

        socketChannel.read(byteBuffers);

        Arrays.asList(byteBuffers).stream().forEach(byteBuffer -> {
            byteBuffer.flip();
        });
        Arrays.asList(byteBuffers).stream().forEach(byteBuffer -> {
            System.out.println("**"+new String(byteBuffer.array())+"**");
        });


    }
//    测试nio执行流程
//    这是nio模型的服务端
    @Test
    public void test5() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        Selector selector = Selector.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(5050);
        serverSocketChannel.socket().bind(inetSocketAddress);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        while (true){
            int i = selector.selectNow();
            if (i==0){
                System.out.println("没有连接，我做其他的事情去!");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }else{
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()){
                        System.out.println("有人来连接了诶");
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        System.out.println(socketChannel.hashCode()+"--->"+Thread.currentThread().getName());
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector,SelectionKey.OP_READ,ByteBuffer.allocate(1024));
                    }
                    if (selectionKey.isReadable()){
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        channel.read(byteBuffer);
                        System.out.println(new String(byteBuffer.array()));
                        byteBuffer.clear();
                    }
                    iterator.remove();
                }
            }


        }


    }
    @Test
//    这是nio模型的客户端
    public void test6() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 5050);
        socketChannel.configureBlocking(false);
        if (!socketChannel.connect(inetSocketAddress)){
            while (!socketChannel.finishConnect()){
                System.out.println("客户端做自己的事情");
            }
        }
        if (socketChannel.isConnected()){
            ByteBuffer byteBuffer = ByteBuffer.wrap("hello world".getBytes());
            socketChannel.write(byteBuffer);
        }
        System.in.read();
    }
    @Test
    public void test7() throws IOException, InterruptedException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(5050);
        serverSocketChannel.socket().bind(inetSocketAddress);
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        while (true){
            int i = selector.selectNow();
            if (i<=0){
                System.out.println("服务器做自己的事情");
                Thread.sleep(3000);
                 continue;
            }
            if (i>0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()){
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector,SelectionKey.OP_READ,ByteBuffer.allocate(1024));
                    }
                    if (selectionKey.isReadable()){
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        channel.read(byteBuffer);
                        System.out.println(new String(byteBuffer.array()));
                        byteBuffer.clear();
                    }
                    iterator.remove();
                }
            }


        }



    }






}
