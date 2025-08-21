package com.example.CARPOOL.IoTDevices;

import android.content.Intent;

import com.example.CARPOOL.DeviceHandlers.Online.AudioActivity;
import com.example.CARPOOL.PolicyStart;

public class AudioDevice extends Device {
    // constructor
    public AudioDevice(String status, String deviceID) {
        super(status, deviceID);
    }

    /**
     * method to link an activity to a device.class
     */
    @Override
    public Intent getIntent(PolicyStart context) {
        return new Intent(context, AudioActivity.class);
    }
}
