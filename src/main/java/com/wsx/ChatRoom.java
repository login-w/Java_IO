package com.wsx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class ChatRoom {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private InetSocketAddress inetSocketAddress;

    public static void main(String[] args) {
        ChatRoom chatRoom = new ChatRoom();
        try {
            chatRoom.rec();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void rec() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        inetSocketAddress=new InetSocketAddress(5050);
        serverSocketChannel.socket().bind(inetSocketAddress);
        selector=Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true){
            int i = selector.selectNow();
            if (i<=0){
                System.out.println("服务器处理其他事情....");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
                        socketChannel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                        System.out.println(socketChannel.hashCode()+"进入了聊天室...");
                    }
                    if (selectionKey.isReadable()){
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        channel.read(byteBuffer);
                        send(new String(byteBuffer.array()),channel,selectionKeys);
                        byteBuffer.clear();
                    }
                    iterator.remove();
                }
            }
        }
    }

public void send(String msg,Channel src,Set<SelectionKey> selectionKeys) throws IOException {
    Iterator<SelectionKey> iterator = selectionKeys.iterator();
    ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
    while (iterator.hasNext()){
        SelectionKey selectionKey = iterator.next();
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        if (src instanceof SocketChannel &&src!=channel){
            channel.write(byteBuffer);
        }

    }
}

}
