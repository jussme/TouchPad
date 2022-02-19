package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.touchpad.communication.LogInServer;
import com.example.touchpad.communication.Transport;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

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
  public void communicateServerAddresses(Map<Transport, List<InetAddress>> addresses) {
    for(Map.Entry<Transport, List<InetAddress>> entry : addresses.entrySet()) {
      System.err.println(entry + "\n");
    }

    public void setServerAddressPrompt(InetSocketAddress serverISA){
      TextView textView = findViewById(R.id.notConnectedMessage);
      String message = getString(R.string.serverUp, serverISA.getAddress().getHostAddress(),
              serverISA.getPort());
      textView.setText(message);
    }
    */
  }
  @Override
  public void communicateClientUDP_ISA(InetSocketAddress clientUDPInetSocketAddress) {
    Intent intent = new Intent(this, TouchPadActivity.class);
    intent.putExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS, clientUDPInetSocketAddress);
    startActivity(intent);
  }
}