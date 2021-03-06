package com.s_k.devsec.positiondevice010;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class UDPObjectTransfer {

    /**
     * オブジェクトを送信する。
     *
     * @param map     オブジェクト
     * @param address 宛先アドレス。192.168.1.255のようにネットワークアドレスを指定するとブロードキャスト送信。
     * @param port    宛先ポート。受信側と揃える。
     * @throws IOException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void send(Map<String, String> map, String address, int port) throws IOException {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            InetAddress IPAddress = InetAddress.getByName(address);
            byte[] sendData = convertToBytes(map);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            clientSocket.send(sendPacket);
        }
    }

    /**
     * 指定ポートでUDPソケットを開いてオブジェクトの受信を待機する。
     *
     * @param port       受信待機ポート。送信側と揃える。
     * @param bufferSize 受信時のバッファーサイズ。小さすぎると受信に失敗するので、適当に大きめな値(1024～8192くらい？)を指定する。
     * @return オブジェクト
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Object receive(int port, int bufferSize)
            throws IOException, ClassNotFoundException {
        try (DatagramSocket clientSocket = new DatagramSocket(port)) {
            return receive(clientSocket, bufferSize);
        }
    }

    /**
     * 指定UDPソケットを使ってオブジェクトの受信を待機する。
     *
     * @param clientSocket 事前作成したUDPソケット
     * @param bufferSize   受信時のバッファーサイズ。小さすぎると受信に失敗するので、適当に大きめな値(1024～8192くらい？)を指定する。
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Object receive(DatagramSocket clientSocket, int bufferSize)
            throws IOException, ClassNotFoundException {
        byte[] buffer = new byte[bufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, bufferSize);
        clientSocket.receive(packet);
        return convertFromBytes(buffer, 0, packet.getLength());
    }

    /**
     * オブジェクトをバイト配列に変換する。
     *
     * @param  object Serializableを実装していなければいけない。
     * @return バイト配列
     * @throws IOException シリアライズに失敗した時に発生する
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    /**
     * バイト配列をオブジェクトに変換する。
     *
     * @param bytes  バイト配列
     * @param offset 読み込み開始位置
     * @param length 読み込むデータの長さ
     * @return 復元されたオブジェクト
     * @throws IOException デシリアライズに失敗した時に発生する
     * @throws ClassNotFoundException デシリアライズに失敗した時に発生する
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static Object convertFromBytes(byte[] bytes, int offset, int length)
            throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes, offset, length);
             ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }
}
