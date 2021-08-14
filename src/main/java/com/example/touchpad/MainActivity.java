package com.example.touchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.refreshButton);
        button.setOnClickListener(onClickListener);

        displayNetworkInterfacesInTextView();
    }

    private void displayNetworkInterfacesInTextView() {
        TextView textView = (TextView) findViewById(R.id.textView);

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            textView.setText(networkInterfacesToString(networkInterfaces));
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private String networkInterfacesToString(Enumeration<NetworkInterface> netInterfaces) {
        StringBuilder stringBuilder = new StringBuilder();
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = netInterfaces.nextElement();
            if(networkInterface.getInetAddresses().hasMoreElements()){
                stringBuilder.append(networkInterfaceToString(networkInterface) + "\n\n");
            }
        }

        return stringBuilder.toString();
    }

    private String networkInterfaceToString(NetworkInterface networkInterface) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Display name: " + networkInterface.getDisplayName() + "\n");
        stringBuilder.append("Name: " + networkInterface.getName() + "\n");
        stringBuilder.append("Parent(null for physical): " + networkInterface.getParent() + "\n");

        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        stringBuilder.append("Bound addresses: \n");
        while (inetAddresses.hasMoreElements()) {
            stringBuilder.append("\t" + inetAddresses.nextElement().toString());
        }

        return stringBuilder.toString();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refreshNetworkInterfacesInfo(v);
        }

        public void refreshNetworkInterfacesInfo(View view) {
            displayNetworkInterfacesInTextView();
        }
    };


}