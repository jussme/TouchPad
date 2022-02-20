package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.touchpad.communication.LogInServer;
import com.example.touchpad.communication.Transport;

import org.w3c.dom.Text;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
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
  public void communicateServerAddresses(Map<Transport, List<InetAddress>> addresses, int serverSocketPort) {
    this.runOnUiThread(() -> {//TODO change the prompt when again no addresses are available
      TextView textView = findViewById(R.id.notConnectedMessage);
      String message = getString(R.string.serverUpPrompt);
      textView.setText(message);

      Map<Transport, TextView> map = new HashMap<>();
      map.put(Transport.WIFI, findViewById(R.id.textView_wifi));
      map.put(Transport.ETHERNET, findViewById(R.id.textView_ethernet));

      for(Map.Entry<Transport, List<InetAddress>> entry : addresses.entrySet()) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(entry.getKey().name + "\n");
        for(InetAddress inetAddress : entry.getValue()){
          stringBuilder.append("\t" + inetAddress.getHostAddress() + ":" + serverSocketPort + "\n");
        }
        textView = map.get(entry.getKey());
        if(textView != null){//all interfaces will have textviews and be included, makeshift solution
          textView.setText(stringBuilder.toString());
        }
      }
    });
  }
  @Override
  public void communicateClientUDP_ISA(InetSocketAddress clientUDPInetSocketAddress) {
    Intent intent = new Intent(this, TouchPadActivity.class);
    intent.putExtra(LogInServer.CLIENT_INET_SOCKET_ADDRESS, clientUDPInetSocketAddress);
    startActivity(intent);
  }
}