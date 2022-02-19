package com.example.touchpad.communication;

public enum Transport {
    BLUETOOTH(2),
    CELLULAR(0),
    ETHERNET(3),
    LOWPAN(6),
    USB(8),
    VPN(4),
    WIFI(1),
    WIFIAWARE(5);

    private int c;

    private Transport(int c){
        this.c = c;
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
