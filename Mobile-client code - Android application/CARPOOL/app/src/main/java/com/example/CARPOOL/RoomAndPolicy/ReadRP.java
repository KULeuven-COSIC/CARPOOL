package com.example.CARPOOL.RoomAndPolicy;


import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Is responsible for reading the central JSON file
 * saves all information under room objects which are saved in a room array.
 */
public class ReadRP implements Runnable {

    private static Room[] rooms;

    /**
     * Return array of room names
     */
    public static String[] getRooms() {
        String[] result = new String[rooms.length];
        for (int i = 0; i < rooms.length; i++) {
            Room current_room = rooms[i];
            result[i] = current_room.getName();
        }
        return result;
    }

    /**
     * Return array of all possible policies in a room
     */
    public static String[] getPoliciesOf(String room) {
        for (int i = 0; i < rooms.length; i++) {
            Room current_room = rooms[i];
            if (current_room.getName() == room) {
                return current_room.getPolicies();
            }
        }
        return null;
    }

    /**
     * Return Room object corresponding to given roomname
     */
    public static Room getRoom(String room) {
        for (int i = 0; i < rooms.length; i++) {
            Room current_room = rooms[i];
            if (room.equals(current_room.getName())) {
                return current_room;
            }
        }
        return null;
    }

    /**
     * reads the JSON from the AWS S3 bucket. Returns string in JSON format
     */
    public static String readJSONFromAWS() {
        try {
            URL url = new URL("https://locproof.s3.eu-west-2.amazonaws.com/index3.html");
            InputStream is = url.openStream();

            int bufferSize = 1024;
            char[] buffer = new char[bufferSize];
            StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                out.append(buffer, 0, numRead);
            }
            String output = out.toString();
            output = StringUtils.substringBetween(output, "<p>", "</p>"); //Takes the contents between the <p> tags. Probably better to use REST or something else instead of plain text.
            is.close();

            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Reads the JSON file in app/java/assets. Returns string in JSON format
     */
    public static String readJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("roomspolicies.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Parses the JSON string and saves all information under room objects which are saved in a room array.
     */
    public static void readJSON(String JSONStr) {

        try {
            JSONObject jsonObj = new JSONObject(JSONStr);
            JSONArray roomArray = jsonObj.getJSONArray("Rooms");
            rooms = new Room[roomArray.length()];

            for (int i = 0; i < roomArray.length(); i++) {
                JSONObject current_room = roomArray.getJSONObject(i);
                //name
                String current_name = current_room.getString("name");

                // policies
                JSONArray policyArray = current_room.getJSONArray("Policies");
                String[] current_policy_array = new String[policyArray.length()];
                int[] current_policy_numbers = new int[policyArray.length()];
                for (int j = 0; j < policyArray.length(); j++) {
                    JSONObject current_policy_object = policyArray.getJSONObject(j);
                    current_policy_array[j] = current_policy_object.getString("policy_devices");
                    current_policy_numbers[j] = Integer.parseInt(current_policy_object.getString("policy_number"));

                }

                // Devices
                JSONArray deviceArray = current_room.getJSONArray("Devices");
                String[] current_device_names = new String[deviceArray.length()];
                String[] current_device_stati = new String[deviceArray.length()];
                String[] current_device_types = new String[deviceArray.length()];
                Map<String,String> current_device_ids = new HashMap();
                for (int j = 0; j < deviceArray.length(); j++) {
                    JSONObject current_object = deviceArray.getJSONObject(j);
                    String current_device_name = current_object.getString("name");
                    String current_device_status = current_object.getString("status");
                    String current_type = current_object.getString("type");
                    current_device_names[j] = current_device_name;
                    current_device_stati[j] = current_device_status;
                    current_device_types[j] = current_type;

                    if (current_type.equals("BLE") || current_device_status.equals("offline")) {
                        current_device_ids.put(current_device_name, current_object.getString("device_id"));
                    };


                }

                Room room = new Room(current_name, current_policy_array, current_device_names, current_device_stati, current_device_types, current_policy_numbers, current_device_ids);
                rooms[i] = room;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private volatile String JSONString;

    @Override // because it can't be done on the main thread
    public void run() {
        JSONString = ReadRP.readJSONFromAWS();
    }

    public String getJSONString() {
        return JSONString;
    }
}
