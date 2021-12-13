package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.touchpad.communication.LogInServer;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.TreeSet;

public class ConnectServerActivity extends AppCompatActivity implements LogInServer.Facilitator{
  private LogInServer logInServer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_touch_pad_not_connected);
  }

  @Override
  protected void onStart() {
    super.onStart();

    logInServer = new LogInServer(this, this);
    logInServer.startServer();
  }

  @Override
  protected void onStop() {
    super.onStop();
    logInServer.shutdownServer();
  }

  @Override
  public void setServerAddressPrompt(InetSocketAddress serverISA){
    TextView textView = findViewById(R.id.notConnectedMessage);
    String message = getString(R.string.serverUp, serverISA.getAddress().getHostAddress(),
            serverISA.getPort());
    textView.setText(message);
  }

  @Override
  public void launchTouchpadding(InetSocketAddress clientUDPInetSocketAddress) {
    Intent intent = new Intent(this, TouchPadActivity.class);
    intent.putExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS, clientUDPInetSocketAddress);
    startActivity(intent);
  }
}