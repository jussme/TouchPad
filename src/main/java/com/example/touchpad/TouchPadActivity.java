package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class TouchPadActivity extends AppCompatActivity {
    public static final String CLIENT_INET_SOCKET_ADDRESS = "com.example.touchpad.SOCK_ADDRESS";
    private MovementSender movementSender = new MovementSender();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad);

        


    }
}