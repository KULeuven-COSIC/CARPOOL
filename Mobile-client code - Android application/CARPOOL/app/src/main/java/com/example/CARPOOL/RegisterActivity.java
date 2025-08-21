package com.example.CARPOOL;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CARPOOL.CommunicationUtils.CreateMessage;
import com.example.CARPOOL.CommunicationUtils.RegisterProcedure;
import com.example.CARPOOL.CommunicationUtils.UDPClient;

/**
 * Activity to Register
 * After sending registermessage, confirmation with code sent to email is needed
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText REGUsername;
    private EditText REGPassword;
    private EditText REGemail;
    private Button REGButton;

    private RegisterProcedure regprocedure = new RegisterProcedure();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register");

        REGUsername = findViewById(R.id.REG_username);
        REGPassword = findViewById(R.id.REG_password);
        REGemail = findViewById(R.id.REG_email);
        REGButton = findViewById(R.id.REG_button);

        configureREGButton();




    }

    private void configureREGButton() {
        REGButton.setText("REGISTER");

        REGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputName = REGUsername.getText().toString().trim();
                String inputPassword = REGPassword.getText().toString().trim();
                String inputEmail = REGemail.getText().toString().trim();

                // Continue if no field is empty
                if (inputName.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Details not filled in correctly", Toast.LENGTH_SHORT).show();
                } else {
                    register(inputName,inputPassword,inputEmail);
                }
            }
        });
    }

    /**
     * Send register message to redbox, then continue
     */
    private void register(String username, String password,String email) {
        String message = CreateMessage.createRegisterMessage(username,password,email);
        regprocedure.setRegmessage(message);
        Thread thread = new Thread(regprocedure);

        try {
            thread.start();
            thread.join();
        } catch (Exception e) {
            Log.e(TAG,"regprocedure for login credentials thread join didn't work", e);
        }

        reconfigure();
    }

    /**
     * Change layout after register message is sent. New layout is to fill in confirmation code which the user should have received
     * on the email he put in
     */
    private void reconfigure() {
        setContentView(R.layout.sendconfirmation);
        EditText Ccode = findViewById(R.id.confirmcode);
        Button sendbutton = findViewById(R.id.CC_send);
        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = Ccode.getText().toString().trim();

                //If field is not filled in, do nothing (except message). Otherwise, continue.
                if (code.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "not filled in", Toast.LENGTH_SHORT).show();
                } else {
                    confirmation(code);
                    finish();
                }
            }
        });

    }

    /**
     * Send confirmation code to redbox
     */
    private void confirmation(String Ccode) {
        regprocedure.setRegmessage(Ccode);
        Thread thread = new Thread(regprocedure);

        try {
            thread.start();
            thread.join();
        } catch (Exception e) {
            Log.e(TAG,"Thread join to give confirm code join exception",e);
        }

        String confirmation = regprocedure.getConfirmation();

        if (confirmation == null) {
            Toast.makeText(RegisterActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegisterActivity.this, confirmation, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finish() {
        super.finish();
     }

}