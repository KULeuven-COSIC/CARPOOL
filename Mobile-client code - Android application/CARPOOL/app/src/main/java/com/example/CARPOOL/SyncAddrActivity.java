package com.example.CARPOOL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.CARPOOL.CommunicationUtils.UDPClient;
import com.example.CARPOOL.RoomAndPolicy.ReadRP;
import com.example.CARPOOL.RoomAndPolicy.Room;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

/**
 * Appears after loginmessage has been sent if there are online devices.
 * For each online device in the policy, it reminds you to ‘initialise’ the IoT devices by sending a message to the redbox.
 * On redbox side, in the same order, the messages and thus the addresses are received.
 */
public class SyncAddrActivity extends AppCompatActivity {

    private Button syncButton;  // button to continue to next device (or go to next activity = last click)
    private TextView syncText;  // text to help with which device needs to be send when
    private String room;
    private String policy;

    private Map<String,String> statuses;
    private Map<String,String> types;
    private int index;
    private int onNr;           // amount of online devices in policy
    private String[] tips;      // array of messages, one for each online device

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_addr);
        setTitle("SYNC");

        syncButton = findViewById(R.id.syncContinue);
        syncText = findViewById(R.id.syncTextView);

        Intent intent = getIntent();                    // get data from previous activity
        room = intent.getStringExtra("roomname");
        policy = intent.getStringExtra("policy");

        setUp();

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index < onNr)
                    syncText.setText(tips[index++]);
                else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UDPClient.Send("ADDRCOMMS_DONE"); //Sending confirmation message to sync the end of this client part with the end of the redbox part
                        }
                    });

                    thread.start();

                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    /**
     * get device data from room instance to create array containing 1 message for each device
     * also initialize button and help/tip text
     */
    public void setUp() {
        Room roominstance = ReadRP.getRoom(this.room);
        statuses = roominstance.getDeviceStatuses();
        types = roominstance.getDeviceTypes();
        onNr = roominstance.getOnNrPolicy(policy);

        tips = new String[onNr];

        int i = 0;
        for (char deviceChar : policy.toCharArray()) {
            String charStr = String.valueOf(deviceChar);
            String status = statuses.get(charStr);
            String type = types.get(charStr);

            if (status.equals("online")) {
                tips[i] = createTip(type,charStr);
                i++;
            }
        }

        syncText.setText(tips[index++]);
        syncButton.setText("continue");
    }

    private String createTip(String type, String name) {
        return "Please make sure the initialize step is done for " + type + " device " + name;
    }

    /**
     * Makes sure we don't go back to home screen when clicking back button (= closes application)
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}