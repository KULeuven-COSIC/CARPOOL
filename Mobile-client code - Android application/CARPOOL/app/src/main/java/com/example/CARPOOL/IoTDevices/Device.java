package com.example.CARPOOL.IoTDevices;

import android.content.Intent;

import com.example.CARPOOL.PolicyStart;
import com.example.CARPOOL.RoomAndPolicy.ReadRP;
import com.example.CARPOOL.RoomAndPolicy.Room;

import java.util.Map;

public abstract class Device {

    private String status;
    protected String deviceID;

    //Constructor
    public Device(String status,String deviceID) {
        this.status = status;
        this.deviceID = deviceID;
    }

    /**
     * true if device is 'offline'
     * false if 'online'
     */
    public boolean isOffline() {
        return status.equals("offline");
    }



    /**
     * Turns the policy of a certain room (both input parameters) to return an array of
     * devices of the right type, in the order of the policy with exception of offline devices:
     * those are first.
     * Based on the read JSON file.
     */
    public static Device[] Decode(String policy, String inputroom) {
        Room room = ReadRP.getRoom(inputroom);
        Map<String, String> Types = room.getDeviceTypes();
        Map<String, String> Statuses = room.getDeviceStatuses();
        Map<String,String> deviceIDs = room.getDeviceIDs();


        /*
        code can probably be shorter/ better:
        first loop through policy to get all offline devices first and get the remaining devices (= online) of the policy which need to be behind offline in the array
        then loop again through the remaining policy to get all online devices

        Suggestion:
        - have 2 lists/arrays: online & offline
        - do 1 loop
        - append online list to offline list
         */
        Device[] devices = new Device[policy.length()];
        String remainingPolicy = new String();
        int index = 0;

        for (char deviceChar : policy.toCharArray()) {
            String status = Statuses.get(Character.toString(deviceChar));
            if (status.equals("offline")) {
                String type = Types.get(Character.toString(deviceChar));
                String device_id = deviceIDs.get(String.valueOf(deviceChar));
                switch (type) {
                    case "Visual":
                        devices[index++] = new VisualDevice(status,device_id);
                        break;
                    case "BLE":
                        devices[index++] = new BLEDevice(status,device_id);
                        break;
                    case "PairedBL":
                        devices[index++] = new PairedBLDevice(status,device_id);
                        break;
                    case "Audio":
                        devices[index++] = new AudioDevice(status,device_id);
                }
            } else {
                remainingPolicy += deviceChar;
            }
        }
        //Online
        for (char deviceChar : remainingPolicy.toCharArray()) {
            String type = Types.get(Character.toString(deviceChar));
            String status = Statuses.get(Character.toString(deviceChar));

            switch (type) {
                case "Visual":
                    devices[index++] = new VisualDevice(status,null);
                    break;
                case "BLE":
                    String device_id = deviceIDs.get(String.valueOf(deviceChar));
                    devices[index++] = new BLEDevice(status,device_id);
                    break;
                case "PairedBL":
                    devices[index++] = new PairedBLDevice(status,null);
                    break;
                case "Audio":
                    devices[index++] = new AudioDevice(status,null);
            }
        }
        return devices;
    }


    /**
     * abstract method to link an activity to a device.class
     */
    public abstract Intent getIntent(PolicyStart context);

}
