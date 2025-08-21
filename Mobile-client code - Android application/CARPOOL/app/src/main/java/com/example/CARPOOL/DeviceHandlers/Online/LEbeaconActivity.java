package com.example.CARPOOL.DeviceHandlers.Online;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CARPOOL.Bluetooth.EddystoneActivity;
import com.example.CARPOOL.R;

/**
 * IMPORTANT: ONLINE BEACONS ARE NOT SUPPORTED BY REDBOX YET. This is because it doesn't yet support sending
 * different size online shares, which is needed bcause of the restriction on advertisement size.
 *
 * Activity to get the (online) share from bluetooth advertisement (without pairing)
 *
 * EddystoneActivity is more general and just gets an advertisement. This class decodes the right way.
 */
public class LEbeaconActivity extends AppCompatActivity {

    private static final String TAG = "LEbeaconActivity";
    public static final int REQUEST_BEACON_MESSAGE = 1; // request code for callback from EddystoneActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lebeacon);

        setTitle("Bluetooth interaction (online)");

        // get device_id
        Intent intent = getIntent();
        String device_id = intent.getStringExtra("device_id");

        // Call eddystone activity
        Intent eddy_intent = new Intent(this, EddystoneActivity.class);
        eddy_intent.putExtra("device_id",device_id);
        startActivityForResult(eddy_intent, REQUEST_BEACON_MESSAGE);
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
            Log.d(TAG, "Failed to get beacon message");
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
        resultIntent.putExtra("share", decoded[1]);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Decode an advertisement message
     */
    public static String[] decode(String hexString) {
        //0x0201689762059950D18A7AA381741E7BC07 for example
        String[] decoded = new String[2];
        decoded[0] = hexString.substring(4, 6); // ID
        decoded[1] = hexString.substring(6, 36); // share
        Log.d(TAG, "Decoded message from (online) beacon: " + decoded[0] + " " + decoded[1]);

        return decoded;
    }
}