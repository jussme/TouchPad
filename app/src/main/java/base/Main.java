package base;
 
import java.io.IOException;
import java.net.DatagramSocket;

import base.ui.TouchpadWindow;
import base.web.FreePortReservation;
import base.web.InputReceiver;
import base.web.LogInHandler;

public class Main {
  public static void main(String[] args) {
	  TouchpadWindow touchpadWindow = new TouchpadWindow();
	  FreePortReservation reservation = null;
    try {
      reservation = FreePortReservation.reserveFreePort();
      LogInHandler logInHandler = new LogInHandler(touchpadWindow);
      logInHandler.logIn(reservation.peekReservedPort());
      new InputReceiver(new DatagramSocket(reservation.redeemReservedPort()));
    } catch (IOException e1) {
      e1.printStackTrace();
      System.exit(1);
    }
  }
}