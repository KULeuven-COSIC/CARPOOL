package com.example.CARPOOL.CommunicationUtils;

public class CreateMessage {

    /**
     * creates login message for server in the right format
     */
    public static String createLoginMessage(String username, String password, String room, int policyNumber) {
        return 1 + "&" + username + "&" + password + "&" + room + "&" + policyNumber;
    }

    /**
     * creates register message for server in the right format
     */
    public static String createRegisterMessage(String username, String password,String email) {
        return 2 + "&" + username + "&" + password + "&" + email;
    }

    /**
     * creates message containing j values for each offline device in the right format
     */
    public static String createJIDMessage(int[] j_values,String[] deviceIDs) {
        String result = new String();
        for (int i=0; i < j_values.length; i++) {
            if (i == 0) {
                result += j_values[i];
                result += deviceIDs[i];
            } else {
                result += '&';
                result += j_values[i];
                result += deviceIDs[i];
            }
        }
        return result;
    }


}



