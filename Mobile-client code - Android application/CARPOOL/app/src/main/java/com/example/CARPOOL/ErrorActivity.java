package com.example.CARPOOL;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity when somethings goes wrong in the secret-reconstruction procedure.
 * Cannot return to procedure from this activity.
 */
public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        Intent intent = getIntent();  
        String errormsg = intent.getStringExtra("errormsg");


        TextView errormessageView = findViewById(R.id.errorMessage);
        if (errormsg != null)
            errormessageView.setText("Something went wrong: " + errormsg );
        else
            errormessageView.setText("Something went wrong, no error message given");

        Button button = findViewById(R.id.ReturnHomeB);
        button.setText("Return to start");
        button.setEnabled(true); // false because i am not sure if this was a source for errors for the next time you want access.



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ErrorActivity.this,MainActivity.class);
                startActivity(intent);
                finish(); // to make sure you can't return to the error screen
            }
        });




    }
}