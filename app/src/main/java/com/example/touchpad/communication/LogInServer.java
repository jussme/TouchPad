package com.example.touchpad.communication;

import android.content.res.AssetFileDescriptor;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.touchpad.TouchPadNotConnectedActivity;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class LogInServer{
  private static final int VERSION = 1;
  public static final String CLIENT_INET_SOCKET_ADDRESS = "com.example.touchpad.SOCK_ADDRESS";
  private final static int SERVER_PORT = 50000;
  private ServerSocket serverSocket;
  private TouchPadNotConnectedActivity context;
  private Thread serverThread;

  public LogInServer(TouchPadNotConnectedActivity context) {
    this.context = context;
    try{
      serverSocket = new ServerSocket();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    serverThread = new Thread(this::runImpl);
  }

  private void runImpl() {
    try {//TODO nie wykonuje sie?
      serverSocket.bind(new InetSocketAddress(pickServerAddress(), SERVER_PORT));
      Socket connection = serverSocket.accept();
      context.setServerAddressPrompt((InetSocketAddress) serverSocket.getLocalSocketAddress());
      System.err.println("setServerAddressPrompt juz po");
      if (!isClientConnection(connection)) {
        sendBrowserJarHttp(connection);
        connection.shutdownOutput();
        connection.close();

        //to RST connections with pesky browsers sending keep-alive PDUs to the serverSocket(?).
        //A four-way FIN handshake with Chrome prevents keep-alives but that requires
        //a sleep between shutdownOutput() and close() it seems; otherwise there's no time to
        //handshake before close()'s RST is sent.
        serverSocket.close();
        serverSocket = new ServerSocket(SERVER_PORT);

        //client jar connection
        connection = serverSocket.accept();
        serverSocket.close();
        //to drain
        isClientConnection(connection);
      }

      //universal client communication from now on
      if (readClientsVersion(connection) != VERSION) {
        //TODO invalid version
      }

      InetSocketAddress remoteUDPInetSocketAddress = readClientsUDPInetSocketAddress(connection);
      connection.close();

      context.launchTouchpadding(remoteUDPInetSocketAddress);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void sendBrowserJarHttp(Socket socket) throws IOException{
    String httpHeader = "HTTP/1.1 200\nContent-Type: application/octet-stream\nContent-Length: ";
    String charsetName = "UTF-8";

    BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    AssetFileDescriptor jarFileDescriptor = context.getAssets().openFd("TouchpadClient.jar");

    httpHeader += jarFileDescriptor.getLength() + "\n\n";
    outputStream.write(httpHeader.getBytes(charsetName));

    FileInputStream jarFileStream = jarFileDescriptor.createInputStream();
    socket.setSoLinger(true, 15);
    int readBuff;
    while ((readBuff = jarFileStream.read()) != -1) {
      outputStream.write(readBuff);
    }
    outputStream.flush();
  }

  private boolean isClientConnection(Socket connection) throws IOException{
    DataInputStream clientDataInputStream = new DataInputStream(connection.getInputStream());
    short checkValue = clientDataInputStream.readShort();
    if (checkValue == Short.MAX_VALUE) {
      return true;
    }
    return false;
  }

  private int readClientsVersion(Socket clientConnection) throws IOException{
    DataInputStream clientDataInputStream = new DataInputStream(clientConnection.getInputStream());
    int clientVersion = clientDataInputStream.readInt();

    return clientVersion;
  }

  private InetSocketAddress readClientsUDPInetSocketAddress(Socket socket) throws IOException{
    DataInputStream clientDataInputStream = new DataInputStream(socket.getInputStream());
    int clientUDPRemotePort = clientDataInputStream.readInt();

    return new InetSocketAddress(socket.getInetAddress(), clientUDPRemotePort);
  }

  private InetAddress pickServerAddress() throws SocketException, UnknownHostException{
    //TODO ipv6 if no ipv4
    return getPhysicalIPv4Address();
  }

  private InetAddress getPhysicalIPv4Address() throws SocketException, UnknownHostException {
    DatagramSocket socket = new DatagramSocket();
    socket.connect(InetAddress.getByName("8.8.8.8"), 12);
    InetAddress usedAddress = socket.getLocalAddress();

    Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
    while(netInterfaces.hasMoreElements()){
      NetworkInterface netInterface = netInterfaces.nextElement();
      if(interfaceContainsAddress(netInterface, usedAddress)){
        return findIPv4Address(netInterface);
      }
    }
    return null;
  }

  private boolean interfaceContainsAddress(NetworkInterface netInterface, InetAddress address) {
    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
    while(addresses.hasMoreElements()){
      if(addresses.nextElement().getHostAddress().equals(address.getHostAddress())){
        return true;
      }
    }
    return false;
  }

  private InetAddress findIPv4Address(NetworkInterface netInterface){
    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
    while(addresses.hasMoreElements()) {
      InetAddress address = addresses.nextElement();
      byte[] addressBytes = address.getAddress();
      if (addressBytes.length == 4 && addressBytes[0] != 127) {
        return address;
      }
    }
    return null;
  }

  public void startServer(){
    serverThread.start();
  }

  public void shutdownServer() {
    serverThread.interrupt();
  }
}
