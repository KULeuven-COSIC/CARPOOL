package com.example.CARPOOL.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.example.CARPOOL.DeviceHandlers.Online.BluetoothPairedActivity.MESSAGE_READ;

/**
 * Class to connect, send & receive via Bluetooth (pairing already done).
 * - UUID hardcoded
 * - connecting & receiving are threads
 */
public class BluetoothProcedure {

    private static final String TAG = "BluetoothProcedure";

    private static final UUID MY_UUID = UUID.fromString("c8888344-9443-43ae-a628-25f9787a7657");    // needs to be the same on the bluetooth device

    private final BluetoothAdapter adapter;
    private final Handler handler;              // message handler
    private ConnectThread connectThread;        // thread to connect
    private ConnectedThread connectedThread;    // thread to read/write while connected
    private Context context;                    // context (activity) where this procedure is called from

    /**
     * Constructor
     */
    public BluetoothProcedure(Context context, Handler handler) {
        adapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
        this.context = context;
    }

    /**
     * starts a connectthread to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectedThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();

    }

    /**
     * Stops all threads in this class from running
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }


    }

    /**
     * Write a message (String) to device via bluetooth. Only works if connection has been established.
     */
    public void writeMessage(String message) {
        connectedThread.write(message.getBytes());

    }

    /**
     * Write a message (byte[]) to device via bluetooth. Only works if connection has been established.
     */
    public void writeMessage(byte[] message) {
        connectedThread.write(message);

    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;


        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            socket = tmp;


        }

        public void run() {
            Log.i(TAG, "BEGIN connectThread");

            // Always cancel discovery because it will slow down a connection
            adapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    socket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close()" +
                            " socket during connection failure", e2);
                }
                ((Activity) context).finish();
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothProcedure.this) {
                connectThread = null;
            }

            // Start the connected thread
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + " socket failed", e);
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread: ");
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN connectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            //write("write example".getBytes());

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) { //while loop stops if socket closes
                    Log.e(TAG, "disconnected", e);
                    ((Activity) context).finish();
                    break;
                }
            }

        }

        /**
         * Write to the connected OutStream.
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


}
