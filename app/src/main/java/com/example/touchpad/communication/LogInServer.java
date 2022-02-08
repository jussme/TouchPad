package com.example.touchpad.communication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;


import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class LogInServer{
  private static final int VERSION = 1;
  public static final String CLIENT_INET_SOCKET_ADDRESS = "com.example.touchpad.SOCK_ADDRESS";

  private Context context;
  private Facilitator facilitator;

  private Thread serverThread;
  private InetAddress serverAddress;
  private ServerSocket serverSocket;

  public interface Facilitator {
    public void communicateServerISA(InetSocketAddress serverISA);
    public void communicateClientUDP_ISA(InetSocketAddress clientUDPInetSocketAddress);
  }

  /**
   * The server is started when created
   * @param context
   * @param facilitator an interface between the server and the one using it
   */
  public LogInServer(Context context, Facilitator facilitator) {
    this.context = context;
    this.facilitator = facilitator;
    new NetworkInterfaceMaster(inetAddress -> {
      restartServer(inetAddress);
    }, context);
  }

  private void runServer() {
    try {
      serverSocket = new ServerSocket();
      serverSocket.bind(new InetSocketAddress(serverAddress, getServerPort()));
      Socket connection = serverSocket.accept();
      serviceConnection(connection);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void serviceConnection(Socket connection) throws IOException{
    if (!isClientConnection(connection)) {
      serviceBrowserConnection(connection);
    }

    //universal client communication from now on
    if (readClientsVersion(connection) != VERSION) {
      //TODO invalid version
    }

    InetSocketAddress remoteUDPInetSocketAddress = readClientsUDPInetSocketAddress(connection);
    connection.close();

    facilitator.communicateClientUDP_ISA(remoteUDPInetSocketAddress);
  }

  private void serviceBrowserConnection(Socket connection) throws IOException{
    sendJarToBrowserOverHttp(connection);
    connection.shutdownOutput();
    connection.close();

    //to RST connections with pesky browsers sending keep-alive PDUs to the serverSocket(?).
    //A four-way FIN handshake with Chrome prevents keep-alives but that requires
    //a sleep between shutdownOutput() and close() it seems; otherwise there's no time to
    //handshake before close()'s RST is sent.
    serverSocket.close();
    serverSocket = new ServerSocket(getServerPort());

    //client jar connection
    connection = serverSocket.accept();
    serverSocket.close();
    //to drain
    isClientConnection(connection);
  }

  private void sendJarToBrowserOverHttp(Socket socket) throws IOException{
    BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    AssetFileDescriptor jarFileDescriptor = context.getAssets().openFd("TouchpadClient.jar");

    String httpHeader = "HTTP/1.1 200\nContent-Type: application/octet-stream\nContent-Length: ";
    httpHeader += jarFileDescriptor.getLength() + "\n\n";
    String charsetName = "UTF-8";
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
    return checkValue == Short.MAX_VALUE;
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

  private int getServerPort(){//TODO
    return 0;
  }

  private void communicateNewServerSocketAddress() {
    while(serverSocket == null || serverSocket.getLocalSocketAddress() == null){

    }
    try{
      System.err.println("\n\nhalo adres: " + serverSocket.getLocalSocketAddress() + "\n\n");
      Thread.sleep(2000);
      System.err.println("\n\nhalo adres: " + serverSocket.getLocalSocketAddress() + "\n\n");
    }catch(InterruptedException e){
      System.err.println(e);
    }
    facilitator.communicateServerISA((InetSocketAddress) serverSocket.getLocalSocketAddress());
  }

  public void startServer(){
    serverThread = new Thread(this::runServer);
    serverThread.start();
    communicateNewServerSocketAddress();
  }

  public void restartServer(InetAddress address){
    shutdownServer();
    serverAddress = address;
    startServer();
  }

  public void shutdownServer() {
    if (serverThread != null) {
      serverThread.interrupt();
    } else {
      System.err.println("null serverThread");
    }
  }
}
