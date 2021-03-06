package com.tzSocket.server;

import java.io.IOException;
import java.net.*;

/**
 * create by tz on 2018-08-02
 */
public class ToUpperUDPServer {

    //服务器IP
    public static final String SERVER_IP = "localhost";

    //服务器端口号
    public static final int SERVER_PORT = 10005;

    //最多处理1024个字符
    public static final  int MAX_BYTES = 1024;

    //UDP使用DatagramSocket发送数据包
    private DatagramSocket serverSocket;

    /**
     * 启动服务器
     * @param serverIp 服务器指定ip
     * @param serverPort 服务器监听IP
     * */
    public void startServer(String serverIp, int serverPort) {

        try {
            //创建DatagramSocket
            InetAddress serverAddr = InetAddress.getByName(serverIp);
            serverSocket = new DatagramSocket(serverPort, serverAddr);

            //创建接收数据的对象
            byte[] recvBuf = new byte[MAX_BYTES];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);

            //死循环，一直运行服务器
            while (true) {
                //接收数据，会在这里阻塞，直到数据到来
                try {
                    serverSocket.receive(recvPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());

                //拿到对端的Ip和端口
                InetAddress clientAddr = recvPacket.getAddress();
                int clientPort = recvPacket.getPort();

                //回传数据
                String upperString = recvStr.toUpperCase();
                byte[] sendBuf = upperString.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, clientAddr, clientPort);
                try {
                    serverSocket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }  finally {
            //记得关闭Socket
            if (null != serverSocket) {
                serverSocket.close();
                serverSocket = null;
            }
        }
    }

    public static void main(String[] args) {
        ToUpperUDPServer server = new ToUpperUDPServer();
        server.startServer(SERVER_IP, SERVER_PORT);
    }


}
