package com.example.CARPOOL.RoomAndPolicy;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a room
 */
public class Room {

    private String name;        // room name
    private String[] policies;  // policies in this room
    private String[] devices;   // available devices in this room (names)

    private Map<String,String> deviceStatuses; // (devicename, status) ; "offline" or "online"
    private Map<String,String> deviceTypes;    // (devicename, type)   ; "Visual","Audio", ...
    private Map<String,Integer> policyNumbers;  // (policy,policynumber) ; policy is string, policynumber is int
    private Map<String,String> deviceIDs;       // (devicename, deviceid) ; only

    private int offlineNumber;  // amount of offline devices in room, not in policy! (maybe for later use)

    //Constructor
    public Room(String name, String[] policies, String[] devices, String[] stati, String[] types,int[] policynumbers, Map<String,String> current_device_ids) {
        this.name = name;
        this.policies = policies;
        this.devices = devices;
        this.deviceIDs = current_device_ids;

        deviceStatuses = new HashMap();
        deviceTypes = new HashMap();
        for (int i=0; i<devices.length;i++) {
            deviceStatuses.put(devices[i],stati[i]);
            deviceTypes.put(devices[i],types[i]);

            if (stati[i].equals("offline"))
                offlineNumber++;
        }

        policyNumbers = new HashMap();
        for (int i=0; i<policies.length;i++) {
            policyNumbers.put(policies[i],policynumbers[i]);
        }

    }


    public int getPolicyNumber(String policy) {
        return (int)policyNumbers.get(policy);
    }

    public String getName() {
        return name;
    }

    public String[] getPolicies() {
        return policies.clone();
    }

    public String[] getDevices() {
        return devices.clone();
    }

    public Map getDeviceStatuses() {
        return deviceStatuses;
    }

    public Map getDeviceTypes() {
        return deviceTypes;
    }

    public Map<String, String> getDeviceIDs() { return deviceIDs; }

    /**
     * Returns amount of offline devices in given policy
     */
    public int getOffNrPolicy(String policy) {
        int count = 0;
        for (int i=0; i<devices.length;i++) {
            if (policy.contains(devices[i]) && deviceStatuses.get(devices[i]).equals("offline")) {
                count++;
            }
        }
        return count;
    }

    public int getOnNrPolicy(String policy) {
        int amount_devices = policy.toCharArray().length;
        int offline_nr = getOffNrPolicy(policy);
        int online_nr = amount_devices-offline_nr;

        return online_nr;
    }



}
