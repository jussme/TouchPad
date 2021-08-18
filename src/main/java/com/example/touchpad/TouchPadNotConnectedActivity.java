package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.net.InetSocketAddress;

public class TouchPadNotConnectedActivity extends AppCompatActivity {
  private LogInServer logInServer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    //TODO shutdown when the user leaves the activity
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_touch_pad_not_connected);

    logInServer = new LogInServer(this);

    TextView textView = (TextView) findViewById(R.id.notConnectedMessage);
    String message = getString(R.string.serverUp, logInServer.getServerLocalPort());
    textView.setText(message);
  }

  public void launchTouchpadding(InetSocketAddress clientUDPInetSocketAddress) {
    Intent intent = new Intent(this, TouchPadActivity.class);
    intent.putExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS, clientUDPInetSocketAddress);
    startActivity(intent);
  }
}