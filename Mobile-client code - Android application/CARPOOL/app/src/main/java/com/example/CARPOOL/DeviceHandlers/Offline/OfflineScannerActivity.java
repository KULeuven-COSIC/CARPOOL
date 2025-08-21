//package com.example.CARPOOL.DeviceHandlers.Offline;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.budiyev.android.codescanner.CodeScanner;
//import com.budiyev.android.codescanner.CodeScannerView;
//import com.budiyev.android.codescanner.DecodeCallback;
//import com.example.CARPOOL.R;
//import com.google.zxing.Result;
//
///** THIS SHOULD NO LONGER BE USED. THIS IS A SUBOPTIMAL VERSION. /////////////////////////////////
// * Activity to get the offline share, j_value and device_id by scanning their QR codes one by one.
// *
// * IMPORTANT: THIS IS VERSION 1
// * VERSION 1:
// *  FIRST SCAN ID
// *  THEN J_VAL
// *  THEN OFF_SHARE
// *
// * VERSION 2 (BETTER):
// *  SCAN 1 THING
// *  DECODE AND GET ALL INFORMATION
// */
//public class OfflineScannerActivity extends AppCompatActivity {
//
//    private static final String TAG = "OfflineScannerActivity";
//
//    private TextView HelperView;        // textView to instruct the user what to do
//    private CodeScanner codeScanner;    // Object type for scanning QR codes
//    private CodeScannerView scannView;  // View for scanner (see activity_(offline_)scanner.xml
//
//    private int phase = 0;              // index to keep track of the 'phase' the scanner is in
//
//    private int j_value;
//    private String rcvd_device_ID;
//    private String off_share;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_offline_scanner);
//        setTitle("SCAN QR (offline)");
//
//
//        scannView = findViewById(R.id.scannerViewOffline);
//        codeScanner = new CodeScanner(this, scannView);
//        HelperView = findViewById(R.id.textScanType);
//        HelperView.setText("Scan device id");
//
//        //Sets what happens when scanner decoded QR code
//        codeScanner.setDecodeCallback(new DecodeCallback() {
//            @Override
//            public void onDecoded(@NonNull Result result) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!onlyDigits(result.getText())) {
//                            setResult(RESULT_CANCELED);
//                            finish();
//                        } else {
//                            PhasedReader(result.getText()); // see function at bottom
//                            HelperView.setText("Tap to scan next");
//
//                        }
//
//
//                    }
//                });
//            }
//        });
//        // Does something when you tap on ScannerView
//        scannView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PhasedHelper();             // see function at bottom
//                codeScanner.startPreview(); // turn on camerapreview (turns off, on decode)
//            }
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        codeScanner.startPreview();
//    }
//
//    @Override
//    protected void onPause() {
//        codeScanner.releaseResources();
//        super.onPause();
//    }
//
//    /**
//     * Function to make sure the given string only contains digits
//     */
//    public static boolean onlyDigits(String str) {
//        int n = str.length();
//        for (int i = 0; i < n; i++) {
//            if (!Character.isDigit(str.charAt(i)))
//                return false;
//        }
//        return true;
//    }
//
//    /**
//     * Assigns result to correct object depending on phase
//     */
//    private void PhasedReader(String result) {
//
//        switch (phase) {
//            case 0:
//                rcvd_device_ID = result;
//                break;
//            case 1:
//                j_value = Integer.parseInt(result);
//                break;
//            case 2:
//                off_share = result;
//                sendBack();
//        }
//        phase++;
//    }
//
//    /**
//     * Sets text to the right instruction based on the phase
//     */
//    private void PhasedHelper() {
//        switch (phase) {
//            case 1:
//                HelperView.setText("Scan j_value");
//                break;
//            case 2:
//                HelperView.setText("Scan offline share");
//        }
//    }
//
//    /**
//     * Ends the activity and returns the essential data
//     */
//    private void sendBack() {
//        Intent resultIntent = new Intent();
//        resultIntent.putExtra("device_id", rcvd_device_ID);
//        resultIntent.putExtra("j", j_value);
//        resultIntent.putExtra("share", off_share);
//        setResult(RESULT_OK,resultIntent);
//        finish();
//    }
//}
