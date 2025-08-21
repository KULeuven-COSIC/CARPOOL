package com.example.CARPOOL.IoTDevices;

import android.content.Intent;

//import com.example.CARPOOL.DeviceHandlers.Offline.OfflineScannerActivity;
import com.example.CARPOOL.DeviceHandlers.Offline.OfflineScannerActivityV2;
import com.example.CARPOOL.DeviceHandlers.Online.ScannerActivity;
import com.example.CARPOOL.PolicyStart;

public class VisualDevice extends Device {
    //Constructor
    public VisualDevice(String status,String deviceID) {
        super(status,deviceID);
    }

    /**
     * method to link an activity to a device.class
     */
    @Override
    public Intent getIntent(PolicyStart context) {
        if (this.isOffline()) {
            Intent intent = new Intent(context, OfflineScannerActivityV2.class);
            intent.putExtra("device_id", deviceID);
            return intent;
        }
        else
            return new Intent(context, ScannerActivity.class);
    }
}

