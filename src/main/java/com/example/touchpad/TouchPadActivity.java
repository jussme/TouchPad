package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.net.InetSocketAddress;

public class TouchPadActivity extends AppCompatActivity {
    private MovementSender movementSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad);

        InetSocketAddress address = (InetSocketAddress) getIntent()
                .getSerializableExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS);
        movementSender = new MovementSender(address);
    }
}