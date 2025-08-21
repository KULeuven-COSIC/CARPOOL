package com.example.CARPOOL.CommunicationUtils;

/**
 * Used in RegisterActivity (as a thread) to send a registermessage.
 *
 * Works in 2 phases
 * First: registermessage from CreateMessage
 * second: Confirmationcode sent to email
 */
public class RegisterProcedure implements Runnable{

    private String regmessage;
    private int phase;
    private String confirmation;

    public void setRegmessage(String regmessage) {
        this.regmessage = regmessage;
    }

    @Override
    public void run() {

        // send registermessage
        if (phase == 0) {
            UDPClient.assignSocket();
            UDPClient.Send(regmessage);
            phase++;
        }

        // send confirmation code sent to email
        else if (phase ==1) {
            UDPClient.Send(regmessage);
            confirmation = UDPClient.Receive();
        }
    }

    public String getConfirmation() {
        return confirmation;
    }
}
