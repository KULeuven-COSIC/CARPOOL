package com.example.CARPOOL.CommunicationUtils;

import android.util.Log;

import com.example.CARPOOL.RoomAndPolicy.ReadRP;
import com.example.CARPOOL.RoomAndPolicy.Room;

import static com.example.CARPOOL.CommunicationUtils.CreateMessage.createLoginMessage;

/**
 * Used in MainActivity (= login activity) (as a thread) to send a login message and receive the tokens
 */
public class LoginProcedure implements Runnable {
    private static final String TAG = "LoginProcedure";

    private String username;
    private String password;
    private String room;
    private int policyNumber;

    private boolean sentLogin = false;

    public LoginProcedure(String username, String password, String room, String policy) {
        this.username = username;
        this.password = password;
        this.room = room;
        Room roomInstance = ReadRP.getRoom(room);
        policyNumber = roomInstance.getPolicyNumber(policy);


    }

    private volatile String encrypted_access_token;
    private volatile String encrypted_id_token;
    private volatile boolean login_succes = true;

    /**
     * Sends loginmessage
     * Receives encrypted tokens in 2 parts (because larger messages were not possible, said Volkan)
     */
    @Override
    public void run() {
        if (sentLogin == false) {
            UDPClient.assignSocket();
            String message = createLoginMessage(username, password, room, policyNumber);
            UDPClient.Send(message);
            String confirmationmsg = UDPClient.Receive();
            if (confirmationmsg.equals("AUTH_FAIL")) {
                Log.d(TAG,"Authentication failed");
                login_succes = false;
            }
            Log.d(TAG, "Login Message sent: " + message);
            sentLogin = true;
        } else if (sentLogin == true){
            try {
                // Receive in 2 parts per token
                String encrypted_access_token_part1 = UDPClient.Receive();
                if (encrypted_access_token_part1 == null)
                    throw new IllegalStateException("Received token is null");
                String encrypted_access_token_part2 = UDPClient.Receive();
                encrypted_access_token = encrypted_access_token_part1 + encrypted_access_token_part2;


                String encrypted_id_token_part1 = UDPClient.Receive();
                String encrypted_id_token_part2 = UDPClient.Receive();
                encrypted_id_token = encrypted_id_token_part1 + encrypted_id_token_part2;
            } catch (Exception e) {
                Log.e(TAG,"Tokens not received properly",e);
                encrypted_access_token = null;
                encrypted_id_token = null;

            }

            sentLogin = false; // just in case
        }

    }

    public boolean isLogin_succes() {
        return login_succes;
    }

    public String getEncrypted_access_token() {
        return encrypted_access_token;
    }

    public String getEncrypted_id_token() {
        return encrypted_id_token;
    }

}
