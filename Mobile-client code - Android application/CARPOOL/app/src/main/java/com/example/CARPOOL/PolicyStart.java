package com.example.CARPOOL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CARPOOL.CommunicationUtils.CryptoUtils;
import com.example.CARPOOL.CommunicationUtils.EndProcedure;
import com.example.CARPOOL.CommunicationUtils.JnIDProcedure;
import com.example.CARPOOL.CommunicationUtils.RedboxProcedure;
import com.example.CARPOOL.IoTDevices.BLEDevice;
import com.example.CARPOOL.IoTDevices.Device;
import com.example.CARPOOL.RoomAndPolicy.ReadRP;

import java.math.BigInteger;

/**
 * Starts procedure based on chosen room and policy. Gives “device handler activities” (DeviceHandlers package) in order of the policy.
 * If there are offline devices, those are done first.
 * Gets the redbox share before interacting the first online device.
 * Sums the shares received from the device handler and adds the red box share (% fieldSize).
 * Decrypts tokens, sends them to redbox and receives a termination message and some records from redbox.
 */
public class PolicyStart extends AppCompatActivity {
    private static final String TAG = "PolicyStart";

    private BigInteger secret = BigInteger.ZERO;    // Secret that is built during activity lifetime by calling device handler activities

    //Data from previous (MainActivity) activity
    private String room;    // Chosen room
    private String policy;  // Chosen policy
    private String encrypted_access_token;
    private String encrypted_id_token;

    private String RedboxShare; //RedboxShare received after offline devices, before online devices


    private Device[] devices;   //Array with DeviceHandlers in order of the policy, except for offline devices, those appear first
    private int device_index;   //Index to keep track of which deviceHandlers have been/have to be called.

    private int offNr;          //Amount of offline devices in chosen policy
    private int[] j_values;     //Array of j_values: one for every offline device
    private String[] deviceIDs; //Array of device_IDs: one for every offline device

    private TextView endText;    //TextView to show end message from redbox
    private TextView resultText; //TextView to show records

    //Needed in onActivityResult
    private final int ONLINE_REQUEST = 1000;    //RequestCode for callbacks from 'online' device handlers
    private final int OFFLINE_REQUEST = 1100;   //RequestCode for callbacks from 'offline' device handlers


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policystart);

        // Get passed data from acces(Main)Activity
        Intent intent = getIntent();
        String username = intent.getStringExtra("SESSION_USERNAME");
        room = intent.getStringExtra("ROOM");
        policy = intent.getStringExtra("POLICY");
        encrypted_access_token = intent.getStringExtra("encrypted_access_token");
        encrypted_id_token = intent.getStringExtra("encrypted_id_token");

        // Show some information of the current session (only visible at the end of the whole procedure)
        TextView currentparameters = findViewById(R.id.SessionText);
        currentparameters.setText("Username: " + username + '\n' + "Room: " + room + "\n" + "Policy: " + policy);

        // endText is a message, resultText are some records from database
        endText = findViewById(R.id.endText);
        resultText = findViewById(R.id.resultText);

        // Start the procedure with goal of getting access to records in database
        StartProcedure();


    }
    // starts procedure
    private void StartProcedure() {
        offNr = ReadRP.getRoom(room).getOffNrPolicy(policy); // amount of offline devices in chosen policy
        devices = Device.Decode(policy, room);

        Device current_device = devices[device_index++];
        Intent intent = current_device.getIntent(this);
        int requestCode = ONLINE_REQUEST;
        // If there are offline devices, initialise j & id arrays
        if (current_device.isOffline()) {
            j_values = new int[offNr];
            deviceIDs = new String[offNr];
            requestCode = OFFLINE_REQUEST;
        } else {
            // get redbox share in case of only online devices (offline -> after offline devices)
            RedboxProcedure redboxProcedure = new RedboxProcedure();
            Thread thread = new Thread(redboxProcedure);
            thread.start();
            try {
                thread.join();
            } catch (Exception e) {
                String errmsg = "Error @ Getting redbox share in case of only online devices:";
                Log.e(TAG,errmsg,e);
                goErrorActivity(errmsg);
            }
            RedboxShare = redboxProcedure.getRBshare();
            if (RedboxShare == null)
                goErrorActivity("Did not receive RedboxShare");
        }


        // Start first device handler -> OnActivityResult
        startActivityForResult(intent, requestCode);
    }

    // ends procedure
    private void EndProcedure() {
        // Do operation to get final secret value
        secret = secret.add(new BigInteger(RedboxShare));
        secret = secret.remainder(BigInteger.TEN.pow(77));

        // Decrypt tokens. If decryption fails, strings should be null and then erroractivity is called
        String decrypted_access_token = CryptoUtils.decrypt(encrypted_access_token, secret.toString());
        String decrypted_id_token = CryptoUtils.decrypt(encrypted_id_token, secret.toString());
        if (decrypted_access_token == null || decrypted_id_token == null) {
            goErrorActivity("Token decryption failed");
        } else {
            // If decryption went through, do final procedure
            // EndProcedure: sends decrypted tokens and receives a termination message (= success or not) and if succesful also some records from database
            EndProcedure endProcedure = new EndProcedure();
            endProcedure.setDecrypted_access_token(decrypted_access_token);
            endProcedure.setDecrypted_id_token(decrypted_id_token);
            Thread thread = new Thread(endProcedure);

            thread.start();
            try {
                thread.join();
            } catch (Exception e) {
                String errmsg = "Trying to join end thread didn't work";
                Log.e(TAG, errmsg, e);
                goErrorActivity(errmsg);
            }

            String endmessage = endProcedure.getEndmessage();
            if (endmessage != null) {
                endText.setText(endmessage);
                String result = endProcedure.getRecords();
                resultText.setText(result);
                resultText.setEnabled(true);
            } else {
                endText.setText("Not successful");
            }
        }


    }

    /**
     * Communicate with redbox to send j, id values and receive redbox share.
     * Only called if there are offline devices
     */
    private void communicateJnID() {
        JnIDProcedure jprocedure = new JnIDProcedure();
        jprocedure.setJ_values(j_values);
        jprocedure.setDeviceIDs(deviceIDs);
        Thread thread = new Thread(jprocedure);
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            String errmsg = "Joining J and ID communicate thread failed";
            Log.e(TAG,errmsg,e);
            goErrorActivity(errmsg);
        }
        RedboxShare = jprocedure.getRedboxshare();
        Log.d(TAG,"Redbox share: " + RedboxShare);
        if (RedboxShare == null)
            goErrorActivity("Did not receive RedboxShare");

        // if there are online devices then continue, otherwise end procedure
        else if (device_index < devices.length) {
            Device current_device = devices[device_index++];
            Intent intent = current_device.getIntent(this);
            startActivityForResult(intent, ONLINE_REQUEST);
        } else {
            EndProcedure();

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //If something went wrong during device handler activity
        if (resultCode == RESULT_CANCELED) {
            goErrorActivity("Something went wrong during device handler activity. (Or pressed back)");
        }

        if (requestCode == ONLINE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String resultShareString = data.getStringExtra("share");
                Log.d(TAG,"Online share: " + resultShareString);
                BigInteger resultShare = new BigInteger(resultShareString);
                Log.d(TAG,resultShare.toString());
                secret = secret.add(resultShare);
                Log.d(TAG,"Added, result now: "+secret.toString());
                // If there are no more devices, end procedure. Otherwise, go to next device handler activity
                if (device_index < devices.length) {
                    Device current_device = devices[device_index++];
                    Intent intent = current_device.getIntent(this);
                    startActivityForResult(intent, ONLINE_REQUEST);

                } else {
                    EndProcedure(); // Decrypt & Authorization
                }
            }
        }

        if (requestCode == OFFLINE_REQUEST) {
            if (resultCode == RESULT_OK) {

                int resultJ = data.getIntExtra("j",0);
                j_values[device_index -1] = resultJ;

                String resultShareString = data.getStringExtra("share");
                Log.d(TAG,"Offline share: " + resultShareString);
                BigInteger resultShare = new BigInteger(resultShareString);
                secret = secret.add(resultShare);

                deviceIDs[device_index-1] = data.getStringExtra("device_id");

                //If there are no more offline devices, send j_values, id values and receive redbox share
                //(We are here after offline device, that means redboxshare is not received yet, since there was at least 1 offline device)
                if (device_index < offNr) {
                    Device current_device = devices[device_index++];
                    Intent intent = current_device.getIntent(this);
                    startActivityForResult(intent, OFFLINE_REQUEST);

                } else {
                    communicateJnID();
                }


            }
        }

    }

    private void goErrorActivity(String errormsg) {
        Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra("errormsg",errormsg);
        startActivity(intent);
        finish();
    }
}