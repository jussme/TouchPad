package com.example.touchpad.communication;

import android.view.MotionEvent;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintSet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.example.touchpad.communication.InputType.MOVEMENT;

public class InputSender {
  private static final int MAX_PAYLOAD_LENGTH = 5;
  private static final int CLICK_TIME_MS = 150;
  private final int MAIN_MOUSE_BUTTON = 1;
  private final DatagramSocket datagramSocket;
  private final DatagramPacket packet;
  private final byte[] payload;

  private class InputParser {
      int mainPointerID = -1;
      int lastMainPointerDown_x;
      int lastMainPointerDown_y;
      long lastACTION_DOWNtime;

      public class ParsedInput {
        InputType inputType;
        int x;
        int y;

        @Override
        public String toString() {
          return inputType.toString() + ", " + x + ":" + y;
        }
      }

      public ParsedInput[] parseInput(View v, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
          case MotionEvent.ACTION_UP:
            //all pointers lifted/no pointers on the screen
            mainPointerID = -1;
            if (System.currentTimeMillis() - lastACTION_DOWNtime <= CLICK_TIME_MS) {
              ParsedInput[] parsedInputs = {new ParsedInput(), new ParsedInput()};
              parsedInputs[0].inputType = InputType.PRESS;
              parsedInputs[0].x = MAIN_MOUSE_BUTTON;
              parsedInputs[1].inputType = InputType.RELEASE;
              parsedInputs[1].x = MAIN_MOUSE_BUTTON;

              return parsedInputs;
            }
            break;
          case MotionEvent.ACTION_POINTER_UP:
            //TODO when the main pointer goes up switch to another one(random will suffice?)
            break;
          case MotionEvent.ACTION_DOWN:
            lastACTION_DOWNtime  = System.currentTimeMillis();
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
            for (int h = 0; h < historySize; ++h) {
              ParsedInput buff = new ParsedInput();
              parsedInput[h] = buff;
              buff.inputType = MOVEMENT;
              buff.x = (int) motionEvent.getX() - lastMainPointerDown_x;
              buff.y = (int) motionEvent.getY() - lastMainPointerDown_y;
              lastMainPointerDown_x = (int) motionEvent.getX();
              lastMainPointerDown_y = (int) motionEvent.getY();
              System.err.println(buff.x + ":" + buff.y);
            }

            return parsedInput;
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

  private InputParser inputParser;
  public InputSender(InetSocketAddress clientUDPAddress) throws IOException {
    this.datagramSocket = new DatagramSocket();
    this.inputParser = new InputParser();
    this.payload = new byte[MAX_PAYLOAD_LENGTH];
    this.packet = new DatagramPacket(payload, payload.length, clientUDPAddress);
  }

  public boolean sendInput(View v, MotionEvent motionEvent) {
    InputParser.ParsedInput[] parsedInputs = inputParser.parseInput(v, motionEvent);
    if (parsedInputs != null) {
      sendParsedInput(parsedInputs);
    }
    //if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
    //System.err.println(motionEvent.getActionMasked() + ": " + System.currentTimeMillis());
    return true;
  }

  private void sendParsedInput(InputParser.ParsedInput[] parsedInputs) {
    for (InputParser.ParsedInput parsedInput : parsedInputs) {
      try {
        switch (parsedInput.inputType) {
          case MOVEMENT:
            sendMovement(parsedInput.x, parsedInput.y);
            break;
          case RELEASE:
          case PRESS:
            sendClick(parsedInput.x, parsedInput.inputType);
            break;
          default:
            break;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void sendMovement(int x, int y) throws IOException {
    synchronized(datagramSocket) {
      payload[0] = (byte) MOVEMENT.getIntType();
      payload[1] = (byte) x;
      payload[2] = (byte) (x >> 8);
      payload[3] = (byte) y;
      payload[4] = (byte) (y >> 8);
      datagramSocket.send(packet);
    }
  }

  private void sendClick(int code, InputType inputType) throws IOException{
    synchronized(datagramSocket) {
      payload[0] = (byte) inputType.getIntType();
      payload[1] = (byte) code;
      payload[2] = (byte) (code >> 8);
      datagramSocket.send(packet);
    }
  }
}
