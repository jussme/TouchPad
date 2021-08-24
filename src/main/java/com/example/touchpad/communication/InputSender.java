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

  private class InputParser {
      int mainPointerID = -1;
      int lastMainPointerDown_x;
      int lastMainPointerDown_y;

      public class ParsedInput {
        InputType inputType;
        int x;
        int y;
      }

      public ParsedInput[] parseInput(View v, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
          case MotionEvent.ACTION_UP:
            //all pointers lifted/no pointers on the screen
            mainPointerID = -1;
            break;
          case MotionEvent.ACTION_POINTER_UP:
            //TODO when the main pointer goes up switch to another one(random will suffice?)
            break;
          case MotionEvent.ACTION_DOWN:
            //first pointer put on the screen
          case MotionEvent.ACTION_POINTER_DOWN:
            //another pointer put on the screen
            int actionIndex = motionEvent.getActionIndex();
            mainPointerID = motionEvent.getPointerId(actionIndex);
            lastMainPointerDown_x = (int) motionEvent.getX(actionIndex);
            lastMainPointerDown_y = (int) motionEvent.getY(actionIndex);
            break;
          case MotionEvent.ACTION_MOVE:
            //any one of the pointers moved
            int historySize = motionEvent.getHistorySize();
            ParsedInput[] parsedInput = new ParsedInput[historySize];
            int mainPointerIndex = motionEvent.findPointerIndex(mainPointerID);
            for (int h = 0; h < historySize; ++h) {
              ParsedInput buff = new ParsedInput();
              parsedInput[h] = buff;
              buff.inputType = MOVEMENT;
              //buff.x =
              //TODO what values to send?
            }
            break;
          default:
            break;
        }

        return null;
      }

      //returns -1 if no more pointers
      private int findNewMainPointer(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        int buff;
        int min = pointerCount + 1;
        for (int it = 0; it < pointerCount; ++it) {//is pointerId incremented when assinging?
          buff = motionEvent.getPointerId(it);
          min = buff < min? buff : min;
        }

        return (min == pointerCount + 1)? -1 : min;
      }
  }

  public InputSender(InetSocketAddress clientUDPAddress) throws IOException {
    this.datagramSocket = new DatagramSocket();

    this.payload = new byte[MAX_PAYLOAD_LENGTH];
    this.packet = new DatagramPacket(payload, payload.length, clientUDPAddress);
  }

  public boolean sendInput(View v, MotionEvent motionEvent) {
    if(motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE)
      System.err.println(motionEvent.getActionIndex());
    //System.err.println(motionEvent.getAction() + ";" + motionEvent.getActionMasked());
    return true;
  }

  private void sendParsedInput(InputParser.ParsedInput parsedInput) {
    System.err.println(parsedInput.inputType + "\n\t" + parsedInput.x + ":" + parsedInput.y);
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
