package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.touchpad.communication.LogInServer;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.TreeSet;

public class ConnectServerActivity extends AppCompatActivity {
  private Set<LogInServer> logInServers = new TreeSet<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    //TODO shutdown when the user leaves the activity
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_touch_pad_not_connected);
  }

  @Override
  protected void onStart() {
    super.onStart();

    LogInServer server = new LogInServer(this);
    logInServers.add(server);
    server.startServer();
  }

  @Override
  protected void onStop() {
    super.onStop();
    for(LogInServer server : logInServers){
      server.shutdownServer();
    }
  }

  public void setServerAddressPrompt(InetSocketAddress serverISA){
    TextView textView = findViewById(R.id.notConnectedMessage);
    String message = getString(R.string.serverUp, serverISA.getAddress().getHostAddress(),
            serverISA.getPort());
    textView.setText(message);
  }

  public void launchTouchpadding(InetSocketAddress clientUDPInetSocketAddress) {
    Intent intent = new Intent(this, TouchPadActivity.class);
    intent.putExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS, clientUDPInetSocketAddress);
    startActivity(intent);
  }
}