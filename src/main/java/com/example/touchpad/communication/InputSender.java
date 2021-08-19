package com.example.touchpad.communication;

import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import static com.example.touchpad.communication.InputType.MOVEMENT;

public class InputSender {
  public static final int MAX_PAYLOAD_LENGTH = 5;
  private DatagramSocket datagramSocket;
  private final DatagramPacket packet;
  private final byte[] payload;

  public InputSender(InetSocketAddress clientUDPAddress) throws IOException {
    this.datagramSocket = new DatagramSocket();

    this.payload = new byte[MAX_PAYLOAD_LENGTH];
    this.packet = new DatagramPacket(payload, payload.length, clientUDPAddress);
  }

  public boolean sendInput(View v, MotionEvent motionEvent) {
    System.err.println(motionEvent.toString());
    return true;
  }

  private void sendMovement(int x, int y) {
    try {
      synchronized(datagramSocket) {
        payload[0] = (byte) MOVEMENT.getIntType();
        payload[1] = (byte) x;
        payload[2] = (byte) (x >> 8);
        payload[3] = (byte) y;
        payload[4] = (byte) (y >> 8);
        datagramSocket.send(packet);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void sendClick(int code, InputType inputType) {
    try {
      synchronized(datagramSocket) {
        payload[0] = (byte) inputType.getIntType();
        payload[1] = (byte) code;
        payload[2] = (byte) (code >> 8);
        datagramSocket.send(packet);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
