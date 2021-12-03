package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.touchpad.communication.LogInServer;

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
                }
            }
        }

        private void launchNetInterfacesActivity() {
            Intent intent = new Intent(MainActivity.this, NetworkInterfacesBriefActivity.class);
            startActivity(intent);
        }

        private void launchNotConnectedActivity() {
            Intent intent = new Intent(MainActivity.this, ConnectServerActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //interfaces brief button
        networkInterfacesInfoButton = (ImageButton) findViewById(R.id.networkInterfacesInfo);
        touchPadButton = (ImageButton) findViewById(R.id.touchPad);

        //server button
        networkInterfacesInfoButton.setOnClickListener(modeButtonsOnClickListener);
        touchPadButton.setOnClickListener(modeButtonsOnClickListener);
    }
}