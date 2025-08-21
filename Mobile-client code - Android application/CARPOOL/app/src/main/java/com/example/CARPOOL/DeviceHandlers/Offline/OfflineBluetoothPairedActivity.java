package com.example.CARPOOL.DeviceHandlers.Offline;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CARPOOL.Bluetooth.BluetoothProcedure;
import com.example.CARPOOL.R;


/**
 * Activity to get the offline share, j_value and device_id from regular (paired) bluetooth interaction
 */
public class OfflineBluetoothPairedActivity extends AppCompatActivity {
    private static final String TAG = "OfflinePairedBLActivity";
    
    private String device_id;                           // deviceID from JSON

    private final int SELECT_DEVICE_REQUEST_CODE = 600; // requestCode for callback from selecting a bluetooth device
    private final int REQUEST_ENABLE_BT = 500;          // requestCode for callback from enabling bluetooth

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothProcedure bluetoothprocedure;      //see custom Bluetooth package

    public static final int MESSAGE_READ = 2;           // Code for message handler (see bottom)



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        setTitle("Interact with BL device (offline)");

        Intent intent = getIntent();
        device_id = intent.getStringExtra("device_id");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            finish();
        }


    }
    // after onCreate
    @Override
    protected void onStart() {
        super.onStart();

        // If bluetooth not enabled: ask to enable.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            pair(); // if there is an ask prompt, this will happen after (+/- line 110)
        }

    }

    /**
     * End activity and 'send back' share, j_value and device_id based on given message
     */
    private void sendBack(String message) {
        String[] splitted = message.split("&", 3);
        Log.d(TAG,"Share: " + splitted[0] + " J: " + splitted[1] + " ID: " + splitted[2] );

        if (device_id.equals(splitted[2])) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("share", splitted[0]);
            resultIntent.putExtra("j", Integer.parseInt(splitted[1]));
            resultIntent.putExtra("device_id", splitted[2]);
            setResult(RESULT_OK, resultIntent);
            finish(); // to ensure you can't hit back arrow to this activity
        } else {
            finish(); //device ID does not match id from JSON. Go to error page.
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                finish(); // finish which gives 'result canceled' to policy start -> go to error activity
            } else {
                // pair if bluetooth enabled after ask prompt
                pair();
            }
        } else if (requestCode == SELECT_DEVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                BluetoothDevice deviceToPair = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
                bluetoothprocedure = new BluetoothProcedure(this, handler); // bluetoothprocedure, see custom bluetooth package
                bluetoothprocedure.connect(deviceToPair);
            } else {
                finish();
            }
        }
    }

    /**
     * Pop-up screen to select device to be paired with.
     * Goes to onActivityResult after selecting
     */
    private void pair() {
        BluetoothDeviceFilter deviceFilter = new BluetoothDeviceFilter.Builder().build(); //.setNamePattern
        AssociationRequest pairingRequest = new AssociationRequest.Builder().addDeviceFilter(deviceFilter).setSingleDevice(false).build();
        CompanionDeviceManager deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
        deviceManager.associate(pairingRequest, new CompanionDeviceManager.Callback() {
            // Called when a device is found. Launch the IntentSender so the user can
            // select the device they want to pair with.
            @Override
            public void onDeviceFound(IntentSender chooserLauncher) {
                try {
                    startIntentSenderForResult(
                            chooserLauncher, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0
                    );
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG,"Fault at bringing up launcher to choose bluetooth device ", e);
                    finish(); // finish which gives 'result canceled' to policy start -> go to error activity
                }
            }

            @Override
            public void onFailure(CharSequence error) {
                finish(); // finish which gives 'result canceled' to policy start -> go to error activity
            }
        }, null);
    }


    /**
     * Message handler to pass message from bluetooth connected thread to this activity
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {


                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    sendBack(readMessage);
                    break; // Break not necessary (yet). But in case the message handler is to be expanded upon.
            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        bluetoothprocedure.stop();
    }
}