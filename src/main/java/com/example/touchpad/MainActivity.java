package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.net.NetworkInterface;

public class MainActivity extends AppCompatActivity {
    private ImageButton networkInterfacesInfoButton;
    private ImageButton touchPadButton;

    private View.OnClickListener modeButtonsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {//ternary operator doesn't work?
            //v.equals(networkInterfacesInfoButton)? launchNetInterfacesActivity() : launchTouchPadActivity();
            if (v.equals(networkInterfacesInfoButton)) {
                launchNetInterfacesActivity();
            } else {//touchPadButton
                launchTouchPadActivity();
            }
        }
    };

    private void launchNetInterfacesActivity() {
        Intent intent = new Intent(this, NetworkInterfacesBriefActivity.class);
        startActivity(intent);
    }

    private void launchTouchPadActivity() {
        Intent intent = new Intent(this, TouchPadActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkInterfacesInfoButton = (ImageButton) findViewById(R.id.networkInterfacesInfo);
        touchPadButton = (ImageButton) findViewById(R.id.touchPad);

        networkInterfacesInfoButton.setOnClickListener(modeButtonsOnClickListener);
        touchPadButton.setOnClickListener(modeButtonsOnClickListener);
    }
}