package com.example.CARPOOL.DeviceHandlers.Offline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CARPOOL.Bluetooth.EddystoneActivity;
import com.example.CARPOOL.R;

/**
 * Activity to get the offline share, j_value and device_id from a bluetooth advertisement (without pairing)
 *
 * EddystoneActivity is more general and just gets an advertisement. This class decodes the right way.
 *
 */
public class OfflineLEbeaconActivity extends AppCompatActivity {

    private static final String TAG = "OfflineLEbeaconActivity";

    private static final int REQUEST_BEACON_MESSAGE = 1; // request code for callback from EddystoneActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_lebeacon);
        setTitle("Bluetooth interaction (offline)");

        // get device_id
        Intent intent = getIntent();
        String device_id = intent.getStringExtra("device_id");

        // Call eddystone activity
        Intent eddy_intent = new Intent(this,EddystoneActivity.class);
        eddy_intent.putExtra("device_id",device_id);
        startActivityForResult(eddy_intent,REQUEST_BEACON_MESSAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_BEACON_MESSAGE) {
                String hexString = data.getStringExtra("Hexstring");
                DecodeAndReturn(hexString);
            }

        } else {
            Log.d(TAG,"Failed to get beacon message");
            finish();
        }
    }

    /**
     * Decode the message (hexstring) and return the information to calling activity (policyStart)
     */
    protected void DecodeAndReturn(String hexString) {
        String[] decoded = decode(hexString);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("device_id", decoded[0]);
        resultIntent.putExtra("j", Integer.parseInt(decoded[1]));
        resultIntent.putExtra("share", decoded[2]);
        setResult(RESULT_OK,resultIntent);
        finish();
    }

    /**
     * Decode an advertisement message
     */
    private static String[] decode(String hexString) {
        //0x0201689762059950D18A7AA381741E7BC07 for example
        String[] decoded = new String[3];
        decoded[0] = hexString.substring(4,6); // ID
        decoded[1] = hexString.substring(6,16); // j value
        decoded[2] = hexString.substring(16,36); // share
        Log.d(TAG,"Decoded message from beacon: "+ decoded[0]+ " " +decoded[1] + " " +  decoded[2]);

        return decoded;
    }
}