package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.touchpad.communication.LogInServer;

import java.net.InetSocketAddress;

public class ServerActivity extends AppCompatActivity implements LogInServer.Facilitator{
  private LogInServer logInServer;
  private TextView textViewPrompt;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_touch_pad_not_connected);
    textViewPrompt = findViewById(R.id.notConnectedMessage);
    textViewPrompt.setText(getString(R.string.serverDown));//doesnt happen?
  }

  @Override
  protected void onStart() {
    super.onStart();

    logInServer = new LogInServer(this, this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    System.err.println("stop!");
    logInServer.shutdownServer();
  }

  @Override
  public void noAvailableServerISA(){
    String message = getString(R.string.serverNoIP);
    this.runOnUiThread(() -> {
      textViewPrompt.setText(message);
    });
  }

  @Override
  public void communicateServerISA(InetSocketAddress serverISA){
    String message = getString(R.string.serverUp, serverISA.getAddress().getHostAddress(),
            serverISA.getPort());
    this.runOnUiThread(() -> {
      textViewPrompt.setText(message);
    });
  }

  @Override
  public void communicateClientUDP_ISA(InetSocketAddress clientUDPInetSocketAddress) {
    Intent intent = new Intent(this, TouchPadActivity.class);
    intent.putExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS, clientUDPInetSocketAddress);
    startActivity(intent);
  }
}