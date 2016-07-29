package com.kevin.miniagv2.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by Administrator
 * on 2016/7/15.
 */
public class UdpHelper {
    private static final String TAG = "UdpHelper";
    public static final int ONE_KB = 1024;
    public static final int HALF_KB = 512;
    public static final int RECEIVED_DATA = 0x123;
    private DatagramSocket udpSocket;
    private DatagramPacket udpReceivePacket;
    private DatagramPacket udpSendPacket;
    private InetAddress inetAddress;
    private static byte[] udpReceiveBytes;
    private OnReceiveListen onReceiveListen;//接收监听
    private Thread udpReceiveThread;
    private static UdpHelper UdpInstance;
    private Myhandler myhandler;
    private SpHelper spHelper;
    private boolean isReSend = false;


//    //私有构造器
    private UdpHelper(Context context) {
        init(context);
    }

    //单例

    /**
     * 取得实例
     * @param context 最好用Application的context
     * @return 单例
     */
    public static synchronized UdpHelper getUdpInstance(Context context) {

        if (UdpInstance == null) {
            UdpInstance = new UdpHelper(context);
        }
        return UdpInstance;
    }


    //初始化
    private void init(Context context) {

        try {
            spHelper = new SpHelper(context);
            udpReceiveBytes = new byte[HALF_KB];
            udpReceivePacket = new DatagramPacket(udpReceiveBytes, HALF_KB);
            inetAddress = InetAddress.getByName(spHelper.getSpAgvIp());
            udpSocket = new DatagramSocket();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        myhandler = new Myhandler();
        receiveUdp();
    }

    //接收线程
    private void receiveUdp() {

        udpReceiveThread = new Thread() {
            public void run() {
                while (!udpReceiveThread.isInterrupted()) {
                    try {
                        //初始化赋值 (byte)0x00

                        //会阻塞
                        udpSocket.receive(udpReceivePacket);
                        int len = udpReceivePacket.getLength();

                        if (len > 0) {
                            if (onReceiveListen != null) {

                                onReceiveListen.onReceiveData(udpReceiveBytes,len,null);
                                myhandler.sendEmptyMessage(RECEIVED_DATA);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };
        udpReceiveThread.start();

    }


    //关闭udp
    public void stop() {
        udpReceiveThread.interrupt();
        if (udpSocket != null) {
            udpSocket.close();
        }
        UdpInstance = null;
    }

    //发送
    public void send(byte[] data){

        Log.e(TAG, "发送的数据=" + Util.bytes2HexString(data, data.length));
        if(udpSendPacket==null){
            udpSendPacket = new DatagramPacket(data, data.length, inetAddress, Constant.REMOTE_PORT);
        }else{
            udpSendPacket.setData(data);
            udpSendPacket.setLength(data.length);
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    if (udpSocket != null) {
                        udpSocket.send(udpSendPacket);
                        Log.e(TAG, "udp发送成功！");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "udp发送失败！"+e.toString());
                }
            }
        }.start();
    }


    //发送
    public void send(byte[] data,int times){

        if(times == 0){
            //不作重发
        }else if(times > 0){
            //作重发times次


        }

        Log.e(TAG, "发送的数据=" + Util.bytes2HexString(data, data.length));
        if(udpSendPacket==null){
            udpSendPacket = new DatagramPacket(data, data.length, inetAddress, Constant.REMOTE_PORT);
        }else{
            udpSendPacket.setData(data);
            udpSendPacket.setLength(data.length);
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    if (udpSocket != null) {
                        udpSocket.send(udpSendPacket);
                        Log.e(TAG, "udp发送成功！");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "udp发送失败！"+e.toString());
                }
            }
        }.start();
    }




    public void setOnReceiveListen(OnReceiveListen onReceiveListen){
        this.onReceiveListen = onReceiveListen;
    }

    static class Myhandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == RECEIVED_DATA){
                Arrays.fill(udpReceiveBytes, (byte) 0x00);
            }
        }
    }

}
