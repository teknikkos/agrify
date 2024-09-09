package com.app.agrify;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button button;
    Button buttonSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize the button
        button = (Button) findViewById(R.id.LogIn_Button);
        buttonSignin = (Button) findViewById(R.id.SignIn_button);

        // Set up the button click listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity(); // Call the method to open the new activity
            }
        });

        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUpActivity(); // Call the method to open the new activity
            }
        });

        // Handle window insets to adjust padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Method to open the LogIn activity
    public void openLoginActivity() {
        Intent intent = new Intent(this, LogIn.class);
        startActivity(intent); // Start the LogIn activity
    }

    public void openSignUpActivity() {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent); // Start the LogIn activity
    }
}