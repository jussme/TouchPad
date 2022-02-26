package base.web;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class InputReceiver extends Thread{
  private static final int MAX_PAYLOAD_LENGTH = 5;
  DatagramSocket datagramSocket;
  DatagramPacket packet;
  byte[] payload;
  
  Robot inputExecutor;
  
  public InputReceiver(DatagramSocket inputSocket) {
    try {
      this.datagramSocket = inputSocket;
      this.inputExecutor = new Robot();
      
      this.payload = new byte[MAX_PAYLOAD_LENGTH];
      this.packet = new DatagramPacket(payload, payload.length);
      
      this.setPriority(MAX_PRIORITY);
    }catch(AWTException e) {
      e.printStackTrace();
      System.exit(1);
    }
    
    this.start();
  }
  
  @Override
  public void run() {
    try {
      int x = 0, y = 0;
      Point pointerLocation;
      InputType inputType;
      do {
        datagramSocket.receive(packet);
        inputType = InputType.valueOf(payload[0]);
        x = Byte.toUnsignedInt(payload[1]) + (Byte.toUnsignedInt(payload[2]) << 8);
        switch(inputType) {
          case MOVEMENT:
            y = Byte.toUnsignedInt(payload[3]) + (Byte.toUnsignedInt(payload[4]) << 8);
            pointerLocation = MouseInfo.getPointerInfo().getLocation();
            System.out.println(System.currentTimeMillis() + "\n\t" + pointerLocation.x + ":" + pointerLocation.y + "\n\t" + x + ":" + y);
            inputExecutor.mouseMove(pointerLocation.x + x, pointerLocation.y + y);
            break;
          case PRESS:
            inputExecutor.mousePress(InputEvent.getMaskForButton(x));
            break;
          case RELEASE:
            inputExecutor.mouseRelease(InputEvent.getMaskForButton(x));
            break;
          default:
            throw new IllegalArgumentException();
        }
      }while(true);
    }catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public void shutdown() {
    
  }
}
