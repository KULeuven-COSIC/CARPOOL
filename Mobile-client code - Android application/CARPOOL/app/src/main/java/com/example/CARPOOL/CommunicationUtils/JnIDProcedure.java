package com.example.CARPOOL.CommunicationUtils;

/**
 * Invoked by PolicyStart after interaction with all offline devices:
 * Contains methods to send J & ID values and afterwards receive the redboxshare.
 *
 * Is not called in case of a policy with only online devices. In that case, redboxshare is received
 * via the RedBoxProcedure class
 */
public class JnIDProcedure implements Runnable {

    private int[] j_values;
    private String[] deviceIDs;
    private volatile String redboxshare;

    public void setDeviceIDs(String[] deviceIDs) {
        this.deviceIDs = deviceIDs;
    }

    public void setJ_values(int[] j_values) {
        this.j_values = j_values;
    }

    @Override
    public void run() {
        UDPClient.Send(CreateMessage.createJIDMessage(j_values,deviceIDs)); // send J values & IDs

        redboxshare = UDPClient.Receive();

    }

    public String getRedboxshare() {
        return redboxshare;
    }
}
