package com.example.CARPOOL.DeviceHandlers.Offline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.CARPOOL.R;
import com.google.zxing.Result;

/**
 * Activity to get the offline share, j_value and device_id by scanning their QR codes one by one.
 *
 * IMPORTANT: THIS IS VERSION 2
 * VERSION 1:
 *  FIRST SCAN ID
 *  THEN J_VAL
 *  THEN OFF_SHARE
 *
 * VERSION 2 (BETTER):
 *  SCAN 1 THING
 *  DECODE AND GET ALL INFORMATION
 */
public class OfflineScannerActivityV2 extends AppCompatActivity {
    private static final String TAG = "OfflineScannerActivity2";

    private TextView resultView;        // TextView to show result on decode: this can and probably should be removed
    private CodeScanner codeScanner;    // Object type for scanning QR codes
    private CodeScannerView scannView;  // View for scanner (see activity_(offline_)scanner.xml

    private String device_id;           // device ID from JSON
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        setTitle("SCAN QR (offline)");

        Intent intent = getIntent();
        device_id = intent.getStringExtra("device_id");


        scannView = findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this, scannView);
        resultView = findViewById(R.id.result);
        resultView.setText("Result: ");

        // Sets what happens when scanner decodes QR code: decode message and send back essential data
        // Decode also stops camera preview
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultView.setText("Result: " + result.getText());
                        Intent resultIntent = new Intent();
                        String[] splitted = result.getText().split("&",3);
                        String rcvd_device_ID = splitted[0];
                        int j_value = Integer.parseInt(splitted[1]);
                        String off_share = splitted[2];
                        Log.d(TAG,"id " + rcvd_device_ID + ", j: " + j_value + ", Share: " + off_share );
                        if (device_id.equals(rcvd_device_ID)) {
                            resultIntent.putExtra("device_id", rcvd_device_ID);
                            resultIntent.putExtra("j", j_value);
                            resultIntent.putExtra("share", off_share);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            finish(); // go to error page because no RESULT_OK
                        }


                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }


}