package com.example.CARPOOL.IoTDevices;

import android.content.Intent;

import com.example.CARPOOL.DeviceHandlers.Offline.OfflineLEbeaconActivity;
import com.example.CARPOOL.DeviceHandlers.Online.LEbeaconActivity;
import com.example.CARPOOL.PolicyStart;

public class BLEDevice extends Device {

    // constructor
    public BLEDevice(String status, String deviceID) {
        super(status,deviceID);
    }

    /**
     * method to link an activity to a device.class
     */
    @Override
    public Intent getIntent(PolicyStart context) {
        Intent intent = null;
        if (this.isOffline())
            intent = new Intent(context, OfflineLEbeaconActivity.class);
        else
            intent = new Intent(context, LEbeaconActivity.class);

        intent.putExtra("device_id",deviceID);
        return intent;

    }
}
