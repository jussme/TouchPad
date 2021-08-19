package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.example.touchpad.communication.LogInServer;
import com.example.touchpad.communication.InputSender;

import java.io.IOException;
import java.net.InetSocketAddress;

public class TouchPadActivity extends AppCompatActivity {
    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return inputSender.sendInput(v, event);
        }
    };

    private InputSender inputSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad);

        InetSocketAddress address = (InetSocketAddress) getIntent()
                .getSerializableExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS);
        try {
            inputSender = new InputSender(address);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        View pane = (View) findViewById(R.id.touchpadPane);
        pane.setOnTouchListener(onTouchListener);
    }
}