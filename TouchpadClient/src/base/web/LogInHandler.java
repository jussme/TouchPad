package base.web;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

import base.ui.TouchpadWindow;

public class LogInHandler {
  //changes when the web flow changes
  private static final int VERSION = 1;
  
  private static final String VALID_IP_ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]).){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
  private static final String VALID_HOSTNAME_REGEX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]).)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9-]*[A-Za-z0-9])$";
  private static final String VALID_PORT_NUMBER_REGEX = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

  private static final int TIMEOUT_MS = 5000;
  
  private final TouchpadWindow touchpadWindow;
  
  @FunctionalInterface
  public static interface UserServerAddressConsumer {
    //dont know whether have InetSocketAddress
    //or Strings as arguments, but should UI classes
    //validate arguments and turn them into InetSocketAddresses?
    //probably not
    public abstract boolean consume(String hostname, String portNumber) throws IllegalArgumentException;
  }
  
  public LogInHandler(TouchpadWindow touchpadWindow) {
    this.touchpadWindow = touchpadWindow;
  }
  
  public void logIn(int udpPort) throws IOException {
    touchpadWindow.promptServerAddress((hostname, serverPortString) -> {
      if (!validUserInput(hostname, serverPortString)) {
        throw new IllegalArgumentException();
      }
      
      var serverAddress = new InetSocketAddress(hostname,
          Integer.valueOf(serverPortString));
      
      try {
        Socket connection = new Socket();
        connection.connect(serverAddress, TIMEOUT_MS);
        authenticateAsClient(connection);
        writeUDPPortToServer(connection, udpPort);
        
        connection.shutdownOutput();
        connection.close();
      } catch (IOException e) {
        return false;
      }
      
      return true;
    });
  }
  
  private boolean validUserInput(String hostname, String portString) {
    return (hostname.matches(VALID_IP_ADDRESS_REGEX)
        || hostname.matches(VALID_HOSTNAME_REGEX))
        && portString.matches(VALID_PORT_NUMBER_REGEX);
  }
  
  private static void authenticateAsClient(Socket connection) throws IOException {
    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
    outputStream.writeShort(Short.MAX_VALUE);
    writeClientVersionToServer(connection);
    outputStream.flush();
  }
  
  private static void writeClientVersionToServer(Socket connection) throws IOException{
    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
    outputStream.writeInt(VERSION);
    outputStream.flush();
  }
  
  private static void writeUDPPortToServer(Socket connection, int localUDPPort) throws IOException{
    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
    outputStream.writeInt(localUDPPort);
    outputStream.flush();
  }
}
