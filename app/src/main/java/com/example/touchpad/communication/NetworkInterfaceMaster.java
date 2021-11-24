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
            @Override
            public void onAvailable(Network network){
                super.onAvailable(network);
                boolean found = false;
                for(LinkAddress linkAddress : manager.getLinkProperties(network).getLinkAddresses()){
                    if(linkAddress.getAddress().getAddress().length == 4){
                        found = true;
                        refresher.refreshServer(linkAddress.getAddress());
                        break;
                    }
                }
                if(!found){//TODO DRY
                    for(LinkAddress linkAddress : manager.getLinkProperties(network).getLinkAddresses()){
                        if(linkAddress.getAddress().getAddress().length != 4 &&
                                linkAddress.getAddress().isLinkLocalAddress()){
                            refresher.refreshServer(linkAddress.getAddress());
                            break;
                        }
                    }
                }
                /*InetAddress[] networkAddresses = manager
                            .getLinkProperties(network)
                            .getLinkAddresses()
                            .stream()
                            .map(LinkAddress::getAddress)
                            .toArray();*/
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
        NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
        requestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        requestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        //TODO ethernet? no either
        manager.requestNetwork(requestBuilder.build(), callback);

        return null;
    }

    public static InetAddress getPhysicalIPv4Address() throws SocketException, UnknownHostException {
        DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getByName("8.8.8.8"), 12);
        InetAddress usedAddress = socket.getLocalAddress();

        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        while(netInterfaces.hasMoreElements()){
            NetworkInterface netInterface = netInterfaces.nextElement();
            if(interfaceContainsAddress(netInterface, usedAddress)){
                return findIPv4Address(netInterface);
            }
        }
        return null;
    }

    public static boolean interfaceContainsAddress(NetworkInterface netInterface, InetAddress address) {
        Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
        while(addresses.hasMoreElements()){
            if(addresses.nextElement().getHostAddress().equals(address.getHostAddress())){
                return true;
            }
        }
        return false;
    }

    public static InetAddress findIPv4Address(NetworkInterface netInterface){
        Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
        while(addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            byte[] addressBytes = address.getAddress();
            if (addressBytes.length == 4 && addressBytes[0] != 127) {
                return address;
            }
        }
        return null;
    }
}
