package com.example.touchpad.communication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

public class NetworkInterfaceMaster {
    private ConnectivityManager.NetworkCallback callback;
    private ConnectivityManager manager;

    NetworkInterfaceMaster(LogInServer.Refresher refresher, Context context){
        callback = new ConnectivityManager.NetworkCallback() {
            private Network currentBestNetwork;
            @Override
            public void onAvailable(Network network){
                super.onAvailable(network);
                boolean found = false;
                for(LinkAddress linkAddress : manager.getLinkProperties(network).getLinkAddresses()){
                    if(linkAddress.getAddress().getClass() == Inet4Address.class){
                        found = true;
                        refresher.refreshServer(linkAddress.getAddress());
                        break;
                    }
                }
                if(!found){//TODO DRY
                    for(LinkAddress linkAddress : manager.getLinkProperties(network).getLinkAddresses()){
                        if(linkAddress.getAddress().getClass() == Inet6Address.class &&
                                linkAddress.getAddress().isLinkLocalAddress()){
                            refresher.refreshServer(linkAddress.getAddress());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities){
                super.onCapabilitiesChanged(network, networkCapabilities);
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties){
                super.onLinkPropertiesChanged(network, linkProperties);
            }

            @Override
            public  void onLosing(Network network, int maxMsToLive){
                super.onLosing(network, maxMsToLive);
                System.err.println("LOSING NETWORK" + network.toString() + ", maxMsToLive: " +
                        maxMsToLive);
            }

            @Override
            public void onLost(Network network){
                super.onLost(network);
                System.err.println("LOSING NETWORK" + network.toString());
            }
        };

        findLocalWifiAddress(context);
    }

    public InetAddress findLocalWifiAddress(Context context) {
        manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        //ethernet - usbc card or virtual eth over usb  should have higher priority
        NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
        requestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET);
        manager.requestNetwork(requestBuilder.build(), callback);

        //wifi, dont know if new builder is needed
        requestBuilder = new NetworkRequest.Builder();
        requestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        manager.requestNetwork(requestBuilder.build(), callback);

        return null;
    }
}
