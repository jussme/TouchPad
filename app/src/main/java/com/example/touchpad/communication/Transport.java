package com.example.touchpad.communication;

public enum Transport {
    BLUETOOTH(2, "Bluetooth"),
    CELLULAR(0, "Cellular"),
    ETHERNET(3, "Ethernet"),
    LOWPAN(6, "LoWPAN"),
    USB(8,"USB"),
    VPN(4, "VPN"),
    WIFI(1, "WiFi"),
    WIFIAWARE(5,"WiFi-aware");

    private int c;
    public String name;

    private Transport(int c, String name){
        this.c = c;
        this.name = name;
    }

    public static Transport valueOf(int c){
        for(Transport value : Transport.values()){
            if(c == value.c){
                return value;
            }
        }
        return null;
    }
}
