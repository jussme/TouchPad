package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.touchpad.communication.LogInServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class MainActivity extends AppCompatActivity {
    private ImageButton networkInterfacesInfoButton;
    private ImageButton touchPadButton;

    private View.OnClickListener modeButtonsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(networkInterfacesInfoButton)) {
                launchNetInterfacesActivity();
            } else{
                if (v.equals(touchPadButton)) {//touchPadButton
                    launchNotConnectedActivity();
                } else {
                    launchTouchpadActivity();
                }
            }
        }

        private void launchTouchpadActivity() {
            Intent intent = new Intent(MainActivity.this, TouchPadActivity.class);
            new Thread(() -> {
                InetSocketAddress address = new InetSocketAddress("localhost", 50000);
                intent.putExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS, address);
                startActivity(intent);
            }).start();
        }

        private void launchNetInterfacesActivity() {
            Intent intent = new Intent(MainActivity.this, NetworkInterfacesBriefActivity.class);
            startActivity(intent);
        }

        private void launchNotConnectedActivity() {
            Intent intent = new Intent(MainActivity.this, TouchPadNotConnectedActivity.class);
            startActivity(intent);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkInterfacesInfoButton = (ImageButton) findViewById(R.id.networkInterfacesInfo);
        touchPadButton = (ImageButton) findViewById(R.id.touchPad);
        Button testPanelButton = (Button) findViewById(R.id.button);
        testPanelButton.setOnClickListener(modeButtonsOnClickListener);
        networkInterfacesInfoButton.setOnClickListener(modeButtonsOnClickListener);
        touchPadButton.setOnClickListener(modeButtonsOnClickListener);
    }
}