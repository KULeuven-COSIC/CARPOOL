package com.example.CARPOOL;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.CARPOOL.CommunicationUtils.NetworkUtils;

/**
 * Appears when on application start there is no network connection.
 * Can’t return to login screen except by hitting “try again ( = check again)” (or closing application)
 */
public class TryForInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_for_internet);
        setTitle("NOT CONNECTED TO INTERNET");

        Button button = findViewById(R.id.NWreturnbutton);
        TextView text = findViewById(R.id.NWtextView);

        text.setText("Not connected to internet. Cannot proceed");
        button.setText("Try again");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isConnected(TryForInternetActivity.this)) {
                    Intent intent = new Intent(TryForInternetActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(TryForInternetActivity.this, "Still not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    /**
     * Makes sure we don't go back to home screen when clicking back button (= closes application)
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}