package com.example.CARPOOL;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.CARPOOL.CommunicationUtils.LoginProcedure;
import com.example.CARPOOL.CommunicationUtils.NetworkUtils;
import com.example.CARPOOL.RoomAndPolicy.ReadRP;

/**
 * Main/First activity. Meant to login, after which procedure starts based on selected room and policy.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";   // Tag for debugging

    private EditText eUsername; //View to fill in username
    private EditText ePassword; //View to fill in password
    private Button bSignIn;     //Button to sign in
    private TextView tRegister; //View to go to register activity

    private String username;
    private String policy;
    private String room;

    LoginProcedure loginProcedure;  // used to send login message and get encrypted tokens

    //Encrypted tokens received after succesful authentication
    private String encrypted_access_token;
    private String encrypted_id_token;

    public static final int MIC_REQUEST_CODE = 100;     //Request code for microphone permission callback
    public static final int CAMERA_REQUEST_CODE = 10;   //Request code for camera permission callback
    public static final int LOCATION_REQUEST_CODE = 1;  //Request code for location permissions callback
    private static final int SYNC_REQUEST = 3;          //Request code for phase after addresses have been sent


    /**
     * Goes to new activity "TryForInternetActivity" if not connected to internet.
     * Checked with method from CommunicationUtils/NetworkUtils
     */
    private void checkInternet() {
        boolean IsConnected = NetworkUtils.isConnected(this);

        if (!IsConnected) {
            Intent intent = new Intent(this, TryForInternetActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Asks for camera permission if it has not been granted yet
     */
    private void checkCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    /**
     * Ask for location permission if it has not been granted yet
     */
    private void checkLocation() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE); // callback to onRequestPermissionResult
        }
    }

    /**
     * Ask for microphone permission if it has not been granted yet
     */
    private void checkMicrophone() {
        if (this.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST_CODE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("ACCESS"); // Sets title of the activity (Shows up in top bar on screen)

        //Assign views to variables. ID's can be found/set in res/layout/...xml
        eUsername = findViewById(R.id.SI_username);
        ePassword = findViewById(R.id.SI_password);
        bSignIn = findViewById(R.id.SI_button);
        tRegister = findViewById(R.id.REG_go);

        checkInternet();    //check if there is a viable connection,
        checkCamera();
        checkLocation();
        checkMicrophone();
        getData();          //Read data from central HTTP or local .json(= /assets/roomspolicies.json)
        String[] rooms = ReadRP.getRooms(); // get Data from JSON File



        //Drop down menu for rooms
        Spinner RoomsSpinner = findViewById(R.id.spinner);
        ArrayAdapter<String> RoomsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, rooms);
        RoomsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        RoomsSpinner.setAdapter(RoomsAdapter);

        // spinner (= Drop down menu) for policies
        Spinner PolicySpinner = findViewById(R.id.spinner2);

        // Code to make sure the selectable policies correspond to the selected room
        RoomsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected_room = parent.getItemAtPosition(position).toString();
                String[] policies = ReadRP.getPoliciesOf(selected_room);
                ArrayAdapter<String> current_adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, policies);
                current_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                PolicySpinner.setAdapter(current_adapter);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // Access (Sign in) Button listener
        bSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String inputRoom = RoomsSpinner.getSelectedItem().toString();
                String inputPolicy = PolicySpinner.getSelectedItem().toString();
                String inputName = eUsername.getText().toString().trim();
                String inputPassword = ePassword.getText().toString().trim();

                //If field not empty, start procedure
                if (inputName.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Details not filled in correctly", Toast.LENGTH_SHORT).show();
                } else {
                    login(inputName, inputPassword, inputRoom, inputPolicy); // Start login procedure
                }
            }
        });
        // Configure "go to register" button
        configureRegister();
    }

    /**
     * Configure register button
     */
    private void configureRegister() {
        String text = "Register here";
        SpannableString content = new SpannableString(text);                // code to underline text
        content.setSpan(new UnderlineSpan(), 0, text.length(), 0);//
        tRegister.setText(content);

        tRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * reads data from either local JSON asset file (local == true) (assets/roomspolicies.json THIS IS FILE IS NOT UP2DATE) or
     * AWS S3 bucket (local == false).
     */
    private void getData() {
        boolean local = false;
        String JSONString;
        if (local == true) {
            //get data from JSON file in assets
            JSONString = ReadRP.readJSONFromAsset(this);
        } else {
            // get data from JSON from AWS bucket
            // can't be done on main thread
            ReadRP readrp = new ReadRP();
            Thread thread = new Thread(readrp);
            thread.start();

            try {
                thread.join();
            } catch (Exception e) {
                String errmsg = "Something went wrong when trying to join read thread";
                Log.e(TAG,errmsg, e);
                goErrorActivity(errmsg);
            }

            JSONString = readrp.getJSONString();
        }

        //Parsing (needed for both ways)
        ReadRP.readJSON(JSONString);


    }


    /**
     * Send username, password, room name and policy number to redbox
     * If authentication is succesful: Make sure redbox has all device adresses (SyncAddrActivity)
     */
    private void login(String username, String password, String room, String policy) {
        this.username = username;
        this.room = room;
        this.policy = policy;
        // UDP communication can't be done on main thread.
        loginProcedure = new LoginProcedure(username, password, room, policy);   //See CommunicationUtils package
        Thread thread = new Thread(loginProcedure);
        try {
            thread.start();
            thread.join();
        } catch (Exception e) {
            String errmsg = "Something went wrong with joining first phase loginprocedure thread";
            Log.e(TAG,errmsg,e);
            goErrorActivity(errmsg);
        }
        boolean login_succes = loginProcedure.isLogin_succes();
        if (login_succes) {

            //make sure redbox gets all addresses
            int onNr = ReadRP.getRoom(room).getOnNrPolicy(policy);
            if (onNr > 0) {
                Intent syncIntent = new Intent(MainActivity.this, SyncAddrActivity.class);
                syncIntent.putExtra("roomname", room);
                syncIntent.putExtra("policy", policy);
                MainActivity.this.startActivityForResult(syncIntent, SYNC_REQUEST);
            }
        } else {
            goErrorActivity("Authentication failed");
        }
    }

    /**
     * Receive encrypted access and id tokens from redbox
     */
    private void getTokens() {

        Thread thread = new Thread(loginProcedure);
        try {
            thread.start();
            thread.join();
        } catch (Exception e) {
            String errmsg = "Something went wrong with joining second phase loginprocedure thread";
            Log.e(TAG,errmsg,e);
            goErrorActivity(errmsg);
        }

        encrypted_access_token = loginProcedure.getEncrypted_access_token();
        encrypted_id_token = loginProcedure.getEncrypted_id_token();

    }

    /**
     * Goes to the next intent: the policyStart page and passes following data strings:
     * - the roomname
     * - the policy
     * - the username  (can be deleted if text in left upper conner is deleted in policyStart activity)
     * - the encrypted tokens
     */
    private void goNextIntent() {
        Intent intent = new Intent(this, PolicyStart.class);
        intent.putExtra("ROOM", room);
        intent.putExtra("POLICY", policy);
        intent.putExtra("SESSION_USERNAME", username);

        intent.putExtra("encrypted_access_token", encrypted_access_token);
        intent.putExtra("encrypted_id_token", encrypted_id_token);

        startActivity(intent);
        finish(); // To make sure there are no multiple instances of this activity
    }

    /**
     * Called after permission request activity.
     * (Nothing yet implemented for camera request)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                    break;
                }
            }
            case CAMERA_REQUEST_CODE:
                break;
            case MIC_REQUEST_CODE:
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SYNC_REQUEST) {
            if (resultCode == RESULT_OK) {
                getTokens();
                if (encrypted_id_token == null || encrypted_access_token == null) {
                    goErrorActivity("Tokens not received properly");
                } else {
                    goNextIntent();
                }
            }
        }
    }

    /**
     * Go to error activity and display {errormsg} there
     */
    private void goErrorActivity(String errormsg) {
        Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra("errormsg",errormsg);
        startActivity(intent);
        finish();
    }
}