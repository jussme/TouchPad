package com.example.touchpad.communication;

import android.content.res.AssetFileDescriptor;

import com.example.touchpad.ConnectServerActivity;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class LogInServer{
  private static final int VERSION = 1;
  public static final String CLIENT_INET_SOCKET_ADDRESS = "com.example.touchpad.SOCK_ADDRESS";
  private ServerSocket serverSocket;
  private ConnectServerActivity context;
  private Thread serverThread;
  private InetAddress serverAddress;

  public interface Refresher{
    public void refreshServer(InetAddress inetAddress);
  }

  public LogInServer(ConnectServerActivity context) {
    this.context = context;
    new NetworkInterfaceMaster(inetAddress -> {

    }, context);
  }

  private void runServer() {
    try {Thread.sleep(5000);
      serverSocket = new ServerSocket();
      serverSocket.bind(new InetSocketAddress(serverAddress, getServerPort()));
      Socket connection = serverSocket.accept();
      context.setServerAddressPrompt((InetSocketAddress) serverSocket.getLocalSocketAddress());
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

    context.launchTouchpadding(remoteUDPInetSocketAddress);
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
    return NetworkInterfaceMaster.getPhysicalIPv4Address();
  }
  private int getServerPort(){//TODO
    return 50000;
  }

  public void startServer(){
    serverThread = new Thread(this::runServer);
    serverThread.start();
  }

  public void restartServer(){
    shutdownServer();
    startServer();
  }

  public void shutdownServer() {
    serverThread.interrupt();
  }
}
