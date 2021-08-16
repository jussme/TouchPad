package com.example.touchpad;

import android.content.res.AssetFileDescriptor;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class LogInServer extends Thread{
  private final static int PORT_LOWER_BOUND = 49152;
  private final static int PORT_UPPER_BOUND = 65535;
  private ServerSocket serverSocket;
  private TouchPadNotConnectedActivity context;

  public LogInServer(TouchPadNotConnectedActivity context) {
    this.context = context;
    try{
      serverSocket = new ServerSocket(findFreePort());
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    this.start();
  }

  @Override
  public void run() {
    try {
      Socket clientConnection = serverSocket.accept();
      sendClientJar(clientConnection);

      try {
        Thread.sleep(10000);
      } catch(InterruptedException e){
        e.printStackTrace();
        System.exit(1);
      }clientConnection.close();
      //client jar connection
      clientConnection = serverSocket.accept();
      serverSocket.close();
      InetSocketAddress remoteUDPInetSocketAddress = readClientsUDPInetSocketAddress(clientConnection);
      clientConnection.close();

      context.launchTouchpadding(remoteUDPInetSocketAddress);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void sendClientJar(Socket socket) throws IOException{
    BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    String httpHeader = "HTTP/1.1 200\nContent-Type: application/octet-stream\nContent-Length: ";
    String charsetName = "UTF-8";

    AssetFileDescriptor jarFileDescriptor = context.getAssets().openFd("Mouse.jar");
    httpHeader += jarFileDescriptor.getLength() + "\n\n";
    outputStream.write(httpHeader.getBytes(charsetName));

    FileInputStream jarFileStream = jarFileDescriptor.createInputStream();
    int readBuff; int counter = 0;
    while ((readBuff = jarFileStream.read()) != -1) {System.err.println(++counter + " " + readBuff);
      outputStream.write(readBuff);
    }
    outputStream.flush();

    try {
      Thread.sleep(10000);
    } catch(InterruptedException e){
      e.printStackTrace();
      System.exit(1);
    }

    jarFileDescriptor.close();
    jarFileStream.close();//prolly closed by the upper method
    outputStream.close();
  }

  private InetSocketAddress readClientsUDPInetSocketAddress(Socket socket) throws IOException{
    DataInputStream clientDataInputStream = new DataInputStream(socket.getInputStream());
    int clientUDPRemotePort = clientDataInputStream.readShort();
    clientDataInputStream.close();

    return new InetSocketAddress(socket.getInetAddress(), clientUDPRemotePort);
  }

  private int findFreePort() throws IOException{
    int currentPort = PORT_LOWER_BOUND;
    while(currentPort <= PORT_UPPER_BOUND) {
      try (ServerSocket serverSocket = new ServerSocket(currentPort)) {
        if (serverSocket.isBound() && serverSocket.getLocalPort() == currentPort) {
          return currentPort;
        }
      } catch (IOException e) {
        ++currentPort;
      }
    }

    throw new IOException("No free port in the <" + PORT_LOWER_BOUND + ", " + PORT_UPPER_BOUND + "> range");
  }

  public boolean isOpen() {
    return serverSocket.isClosed();
  }

  public int getServerLocalPort() {
    return this.serverSocket.getLocalPort();
  }
}
