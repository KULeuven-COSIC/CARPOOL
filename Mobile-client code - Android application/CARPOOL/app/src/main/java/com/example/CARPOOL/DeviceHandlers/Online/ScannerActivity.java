package com.example.CARPOOL.DeviceHandlers.Online;

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
 * Activity to get the (online) share from visual interaction with a QR code
 */
public class ScannerActivity extends AppCompatActivity {
    private static final String TAG = "ScannerActivity";

    private TextView resultView;        // TextView to show result on decode: this can and probably should be removed
    private CodeScanner codeScanner;    // Object type for scanning QR codes
    private CodeScannerView scannView;  // View for scanner (see activity_(offline_)scanner.xml


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        setTitle("SCAN QR");


        scannView = findViewById(R.id.scannerView);
        codeScanner = new CodeScanner(this, scannView);
        resultView = findViewById(R.id.result);
        resultView.setText("Result: ");

        // Sets what happens when scanner decodes QR code: send back data
        // Decode also stops camera preview
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultView.setText("Result: " + result.getText());
                        Log.d(TAG,result.getText());
                        // First check if the message only has digits (so it can be used as an integer)
                        if (!onlyDigits(result.getText())) {
                            setResult(RESULT_CANCELED);
                            finish();
                        } else {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("share", result.getText().trim());
                            setResult(RESULT_OK, resultIntent);
                            finish();
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

    /**
     * Function to make sure the given string only contains digits
     */
    public static boolean onlyDigits(String str) {
        int n = str.length();
        for (int i = 0; i < n; i++) {
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }
}