package com.example.touchpad.communication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

public class NetworkInterfaceMaster {
    NetworkInterfaceMaster(TransportMapInetAddressConsumer addressConsumer, Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        for(Transport t : Transport.values()){
            addresses.put(t, new ArrayList<InetAddress>());
        }
        registerCallbacksByTransportType(addressConsumer, manager);
    }

    //an interface and functions finding representative addresses for interfaces/networks
    private InetAddress findIPv4Address(ConnectivityManager manager, Network network) {
        return findAddress(manager, network,
                (inetAddress) -> inetAddress.getClass() == Inet4Address.class);
    }

    private InetAddress findLinkLocalIPv6Address(ConnectivityManager manager, Network network) {
        return findAddress(manager,
                network,
                (inetAddress) -> inetAddress.getClass() == Inet6Address.class &&
                        inetAddress.isLinkLocalAddress());
    }

    private InetAddress findAddress(ConnectivityManager manager, Network network,
                                    PrimitivePredicate<InetAddress> predicate){
        LinkProperties linkProperties = manager.getLinkProperties(network);
        if(linkProperties == null){
            return null;
        }
        for(LinkAddress linkAddress : linkProperties.getLinkAddresses()){
            if(predicate.test(linkAddress.getAddress())){
                System.err.println("found address4: " + linkAddress.getAddress().getHostAddress());
                return linkAddress.getAddress();
            }
        }
        return null;
    }

    private interface PrimitivePredicate<T> {
        public boolean test(T input);
    }


    private Map<Transport, List<InetAddress>> addresses = new TreeMap<Transport, List<InetAddress>>();
    {
        for(Transport t : Transport.values()){
            addresses.put(t, new ArrayList<InetAddress>());
        }
    }


    /**
     *  Functions as a callback when the address "book"(map) is updated,
     *  passed to the NetworkInterfaceMaster
     */
    public interface TransportMapInetAddressConsumer {
        public void consumeAddresses(Map<Transport, List<InetAddress>> addresses);
    }

    //to extend from, once there is a need for different onAvailable()s
    private class BaseCallback extends ConnectivityManager.NetworkCallback {
        private TransportMapInetAddressConsumer addressConsumer;
        private ConnectivityManager manager;
        private Transport transport;

        public BaseCallback(TransportMapInetAddressConsumer addressConsumer,
                            ConnectivityManager manager,
                            Transport transport){
            this.manager = manager;
            this.addressConsumer = addressConsumer;
            this.transport = transport;
        }

        @Override
        public void onAvailable(Network network){
            super.onAvailable(network);
            InetAddress ia = findIPv4Address(manager, network);
            if (ia != null){
                addresses.get(transport).add(ia);
            }
            ia = findLinkLocalIPv6Address(manager, network);
            if (ia != null){
                addresses.get(transport).add(ia);
            }
            addressConsumer.consumeAddresses(addresses);
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities){
            super.onCapabilitiesChanged(network, networkCapabilities);
            //onLost(network);
            //onAvailable(network);
        }

        @Override
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties){
            super.onLinkPropertiesChanged(network, linkProperties);
            //onLost(network);
            //onAvailable(network);
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
            LinkProperties linkProperties = manager.getLinkProperties(network);
            if(linkProperties == null){
                addresses.get(transport).clear();
                return;
            }
            for(LinkAddress la : linkProperties.getLinkAddresses()){
                addresses.get(transport).remove(la.getAddress());
            }
            System.err.println("LOST NETWORK" + network.toString());
            addressConsumer.consumeAddresses(addresses);
        }
    }

    private void registerTransportCallback(ConnectivityManager manager,
                                           ConnectivityManager.NetworkCallback callback,
                                           int transportType) {
        NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
        requestBuilder.addTransportType(transportType);
        manager.requestNetwork(requestBuilder.build(), callback);
    }
    private void registerCallbacksByTransportType(TransportMapInetAddressConsumer addressConsumer,
                                                         ConnectivityManager manager) {
        registerTransportCallback(manager,
                new BaseCallback(addressConsumer, manager, Transport.valueOf(NetworkCapabilities.TRANSPORT_WIFI)),
                NetworkCapabilities.TRANSPORT_WIFI);
        registerTransportCallback(manager,
                new BaseCallback(addressConsumer, manager, Transport.valueOf(NetworkCapabilities.TRANSPORT_ETHERNET)),
                NetworkCapabilities.TRANSPORT_ETHERNET);
    }
}
