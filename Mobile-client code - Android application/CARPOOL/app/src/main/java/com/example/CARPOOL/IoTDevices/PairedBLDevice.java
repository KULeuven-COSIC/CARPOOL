package com.example.CARPOOL.IoTDevices;

import android.content.Intent;

import com.example.CARPOOL.DeviceHandlers.Offline.OfflineBluetoothPairedActivity;
import com.example.CARPOOL.DeviceHandlers.Online.BluetoothPairedActivity;
import com.example.CARPOOL.PolicyStart;

public class PairedBLDevice extends Device {
    //constructor
    public PairedBLDevice(String status,String deviceID) {
        super(status,deviceID);
    }

    /**
     * method to link an activity to a device.class
     */
    @Override
    public Intent getIntent(PolicyStart context) {
        if (this.isOffline()) {
            Intent intent = new Intent(context, OfflineBluetoothPairedActivity.class);
            intent.putExtra("device_id", deviceID);
            return intent;
        }
        else
            return new Intent(context, BluetoothPairedActivity.class);
    }
}
