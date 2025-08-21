package com.example.CARPOOL.CommunicationUtils;

/**
 * Used in the PolicyStart class (as a thread) to send & receive after reconstruction of secret and decryption of tokens
 * Sends decrypted tokens
 * Receives endmessage & records if succesfull
 */
public class EndProcedure implements Runnable {

    private String decrypted_access_token;
    private String decrypted_id_token;

    public void setDecrypted_access_token(String decrypted_access_token) {
        this.decrypted_access_token = decrypted_access_token;
    }

    public void setDecrypted_id_token(String decrypted_id_token) {
        this.decrypted_id_token = decrypted_id_token;
    }

    private volatile String endmessage;
    private volatile String records;

    @Override
    public void run() {
        UDPClient.Send(decrypted_access_token);
        UDPClient.Send(decrypted_id_token);

        endmessage = UDPClient.Receive();
        records = UDPClient.Receive();

        UDPClient.closeSocket();

    }

    public String getEndmessage() {
        return endmessage;
    }

    public String getRecords() {
        return records;
    }
}
