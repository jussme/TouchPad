package com.example.touchpad.communication;

import android.content.res.AssetFileDescriptor;

import com.example.touchpad.TouchPadNotConnectedActivity;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class LogInServer extends Thread{
  public static final String CLIENT_INET_SOCKET_ADDRESS = "com.example.touchpad.SOCK_ADDRESS";
  private final static int SERVER_PORT = 50000;
  private ServerSocket serverSocket;
  private TouchPadNotConnectedActivity context;

  public LogInServer(TouchPadNotConnectedActivity context) {
    this.context = context;
    try{
      serverSocket = new ServerSocket(SERVER_PORT);
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
      sendClientJarHttp(clientConnection);
      clientConnection.shutdownOutput();
      clientConnection.close();

      //to RST connections with pesky browsers sending keep-alive PDUs to the serverSocket(?).
      //A four-way FIN handshake with Chrome prevents keep-alives but that requires
      //a sleep between shutdownOutput() and close() it seems; otherwise there's no time to
      //handshake before close()'s RST is sent.
      serverSocket.close();
      serverSocket = new ServerSocket(SERVER_PORT);

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

  private void sendClientJarHttp(Socket socket) throws IOException{
    String httpHeader = "HTTP/1.1 200\nContent-Type: application/octet-stream\nContent-Length: ";
    String charsetName = "UTF-8";

    BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    AssetFileDescriptor jarFileDescriptor = context.getAssets().openFd("Mouse.jar");

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

  private InetSocketAddress readClientsUDPInetSocketAddress(Socket socket) throws IOException{
    DataInputStream clientDataInputStream = new DataInputStream(socket.getInputStream());
    int clientUDPRemotePort = clientDataInputStream.readShort();
    clientDataInputStream.close();

    return new InetSocketAddress(socket.getInetAddress(), clientUDPRemotePort);
  }

  public int getServerLocalPort() {
    return this.serverSocket.getLocalPort();
  }
}
