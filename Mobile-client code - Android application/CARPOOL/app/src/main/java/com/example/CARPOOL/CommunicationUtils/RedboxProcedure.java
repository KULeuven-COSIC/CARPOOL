package com.example.CARPOOL.CommunicationUtils;

/**
 * Used to get the redboxshare in PolicyStart if there are only online devices in the policy.
 *
 * Not called if there are offline devices: in this case JnIDProcedure is called, which also gets the redboxshare
 */
public class RedboxProcedure implements Runnable {

    private volatile String RBshare;

    @Override
    public void run() {
        RBshare = UDPClient.Receive();
    }

    public String getRBshare() {
        return RBshare;
    }
}
