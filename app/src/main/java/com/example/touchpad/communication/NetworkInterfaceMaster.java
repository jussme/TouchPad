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
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class NetworkInterfaceMaster {
    private ConnectivityManager manager;

    NetworkInterfaceMaster(InetAddressConsumer addressConsumer, Context context){
        findLocalWifiAddress(addressConsumer, context);
    }

    /**
     * Finds an IP address in a Network object, preferring ipv4 over ipv6
     * and link-local over global ipv6
     *
     * @return found address
     */
    private InetAddress findSuitableAddress(Network network) {//TODO DRY
        for(LinkAddress linkAddress : manager.getLinkProperties(network).getLinkAddresses()){
            if(linkAddress.getAddress().getClass() == Inet4Address.class){
                System.err.println("found address4: " + linkAddress.getAddress().getHostAddress());
                return linkAddress.getAddress();
            }
        }
        for(LinkAddress linkAddress : manager.getLinkProperties(network).getLinkAddresses()) {
            if(linkAddress.getAddress().getClass() == Inet6Address.class &&
                    linkAddress.getAddress().isLinkLocalAddress()){
                System.err.println("found address6linklocal: " + linkAddress.getAddress().getHostAddress());
                return linkAddress.getAddress();
            }
        }
        /*for(LinkAddress linkAddress : manager.getLinkProperties(network).getLinkAddresses()){
            if(linkAddress.getAddress().getClass() == Inet6Address.class){
                System.err.println("found address6: " + linkAddress.getAddress().getHostAddress());
                return linkAddress.getAddress();
            }
        }*/
        System.err.println("address not found\n" + network.toString());
        return null;
    }

    /**
     *  Functions as a callback when an address is found, passed to the NetworkInterfaceMaster
     */
    public interface InetAddressConsumer {
        public void consumeAddress(InetAddress inetAddress);
    }

    //to extend from, once there is a need for different onAvailable()s
    private class BaseCallback extends ConnectivityManager.NetworkCallback {
        private Network currentBestNetwork;
        private InetAddressConsumer addressConsumer;

        public BaseCallback(InetAddressConsumer addressConsumer){

        }

        @Override
        public void onAvailable(Network network){
            super.onAvailable(network);
            InetAddress ia;
            if ((ia = findSuitableAddress(network)) != null){
                addressConsumer.consumeAddress(ia);
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
    }

    private InetAddress findLocalWifiAddress(InetAddressConsumer addressConsumer, Context context) {
        manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        //ethernet - usbc card or virtual eth over usb should have higher priority
        NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
        //requestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET);
        //requestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        //manager.requestNetwork(requestBuilder.build(), new BaseCallback(addressConsumer));

        //wifi, hostspot? dont know if new builder is needed
        requestBuilder = new NetworkRequest.Builder();
        requestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        requestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        manager.requestNetwork(requestBuilder.build(), new BaseCallback(addressConsumer));

        return null;
    }
}
