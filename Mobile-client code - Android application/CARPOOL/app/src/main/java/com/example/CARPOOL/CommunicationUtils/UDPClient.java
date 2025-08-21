package com.example.CARPOOL.CommunicationUtils;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Class contains static methods needed for interaction with the back-end server
 */
public class UDPClient {

    private static final String TAG = "UDPClient";

    private static DatagramSocket clientSocket;             // static!
    private final static int PORT = 6666;
    private final static String ADDRESS = "35.179.36.122";   //redbox IP and old IP: 35.176.23.80
    private final static int timeout = 5000;                // in milliseconds

    /**
     * Open a new datagramsocket and set the timeout time
     */
    public static void assignSocket() {
        try {
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(timeout);

        } catch (IOException e) {
            Log.e(TAG,"Something went wrong making datagram socket",e);
        }
        ;
    }

    /**
     * Close the opened socket
     */
    public static void closeSocket() {
        clientSocket.close();
    }

    /**
     * Send the message string to {ADDRESS, PORT} (redbox IP and port)
     */
    public static void Send(String message) {
        try {
            byte[] sendingDataBuffer = new byte[2048];
            InetAddress IPAddress = InetAddress.getByName(ADDRESS);

            sendingDataBuffer = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, IPAddress, PORT);
            clientSocket.send(sendingPacket);


        } catch (IOException e) {
            Log.e(TAG,"Something went wrong when trying to send a UDP message",e);
        }
    }

    /**
     * Return the received data (on the opened datagram socket) as a string
     */
    public static String Receive() {
        try {
            byte[] receivingDataBuffer = new byte[2048];
            DatagramPacket receivingPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
            clientSocket.receive(receivingPacket);
            String receivedData = new String(receivingPacket.getData(), StandardCharsets.UTF_8);
            return receivedData.trim();

        } catch (IOException e) {
            Log.e(TAG,"Something went wrong when trying to receive a UDP message",e);
            return null;
        }
    }


}
