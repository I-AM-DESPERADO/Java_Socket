package com.tzSocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * create by tz on 2018-08-03
 */
public class ToUpperTCPNonBlockServer {

    //服务器IP
    public static final String SERVER_IP = "127.0.0.1";

    //服务器端口号
    public static final int SERVER_PORT = 10005;

    //请求终结字符串
    public static final char REQUEST_END_CHAR = '#';

    public void startServer(String serverIP, int serverPort) throws IOException {
        //使用NIO需要用到ServerSocketChannel
        //其中包含一个ServerSocket对象
        ServerSocketChannel serviceChannel = ServerSocketChannel.open();

        //创建地址对象
        InetSocketAddress localAddr = new InetSocketAddress(serverIP, serverPort);

        //服务器绑定地址
        serviceChannel.bind(localAddr);

        //设置为非阻塞
        serviceChannel.configureBlocking(false);

        //注册到selector，会调用ServerSocket的accept
        //我们用selector监听accept能否返回
        //当调用accept可以返回时，会得到通知
        //注意，是可以返回，还需要调用accept
        Selector selector = Selector.open();
        serviceChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {

            //调用select，阻塞在这里，直到有注册的channel满足条件
            selector.select();

            //如果走到这里，有符合条件的channel
            //可以通过selector.selectedKeys().iterator()拿到符合条件的迭代器
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            //处理满足条件的keys
            while (keys.hasNext()) {
                //取出一个key并移除
                SelectionKey key = keys.next();
                keys.remove();
                try{
                    if (key.isAcceptable()) {
                        //有accept可以返回
                        //取得可以操作的channel
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();

                        //调用accept完成三次握手，返回与客户端可以通信的channel
                        SocketChannel channel = server.accept();

                        //将该channel置非阻塞
                        channel.configureBlocking(false);

                        //注册进selector，当可读或可写时将得到通知，select返回
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        //有channel可读,取出可读的channel
                        SocketChannel channel = (SocketChannel) key.channel();

                        //创建读取缓冲区,一次读取1024字节
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.read(buffer);

                        //锁住缓冲区，缓冲区使用的大小将固定
                        buffer.flip();

                        //附加上buffer，供写出使用
                        key.attach(buffer);
                        key.interestOps(SelectionKey.OP_WRITE);
                    } else if (key.isWritable()) {
                        //有channel可写,取出可写的channel
                        SocketChannel channel = (SocketChannel) key.channel();

                        //取出可读时设置的缓冲区
                        ByteBuffer buffer = (ByteBuffer) key.attachment();

                        //将缓冲区指针移动到缓冲区开始位置
                        buffer.rewind();

                        //读取为String
                        String recv = new String(buffer.array());

                        //清空缓冲区
                        buffer.clear();
                        buffer.flip();

                        //写回数据
                        byte[] sendBytes = recv.toUpperCase().getBytes();
                        channel.write(ByteBuffer.wrap(sendBytes));

                        //变为等待读
                        key.interestOps(SelectionKey.OP_READ);
                    }
                } catch (IOException e) {
                    key.cancel();
                    key.channel().close();
                }
            }
        }
    }

    public static void main(String[] args) {
        ToUpperTCPNonBlockServer server = new ToUpperTCPNonBlockServer();
        try {
            server.startServer(SERVER_IP, SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
