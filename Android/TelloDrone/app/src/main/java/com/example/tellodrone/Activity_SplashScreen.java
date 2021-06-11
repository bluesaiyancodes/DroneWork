package com.example.tellodrone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Activity_SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity_SplashScreen.this.startActivity(new Intent(Activity_SplashScreen.this, MainActivity.class));
                Activity_SplashScreen.this.finish();
            }
        },2000); //2 Secs


    }
}
