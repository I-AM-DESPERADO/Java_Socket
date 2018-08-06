package com.tzSocket.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * create by tz on 2018-08-02
 */
public class ToUpperTCPBlockServer {
    //服务器IP
    public static final String SERVER_IP = "127.0.0.1";

    //服务器端口号
    public static final int SERVER_PORT = 10016;

    //请求终结字符串
    public static final char REQUEST_END_CHAR = '#';

    /***
     * 启动服务器
     * @param serverPort 服务器监听的端口号，
     * @param serverIP  服务器ip无需指定，系统自动分配
     */
    public void startServer(String serverIP, int serverPort) {

        //创建服务器地址对象
        InetAddress serverAddr;
        try {
            serverAddr = InetAddress.getByName(serverIP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        //Java提供了ServerSocket作为服务器
        //这里使用了Java的自动关闭的语法，很好用
        try(ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 5, serverAddr)) {
            while (true) {
                StringBuilder recvStrBuilder = new StringBuilder();

                //有客户端向服务器发起tcp连接时，accept会返回一个Socket
                //该Socket的対端就是客户端的Socket
                //具体过程可以查看TCP三次握手过程
                try (Socket connection = serverSocket.accept()) {
                    InputStream in = connection.getInputStream();

                    //读取客户端的请求字符串，请求字符串以#终结
                    for (int c = in.read(); c != REQUEST_END_CHAR; c = in.read()) {
                        recvStrBuilder.append((char)c);
                    }
                    recvStrBuilder.append("_ok");
                    recvStrBuilder.append('#');
                    String recvStr = recvStrBuilder.toString();

                    //向客户端写出处理后的字符串
                    OutputStream out = connection.getOutputStream();
                    out.write(recvStr.toUpperCase().getBytes());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ToUpperTCPBlockServer server = new ToUpperTCPBlockServer();
        server.startServer(SERVER_IP,SERVER_PORT);
    }
}
