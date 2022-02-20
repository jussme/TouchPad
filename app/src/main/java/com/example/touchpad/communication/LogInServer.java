package com.example.touchpad.communication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

public class LogInServer{
  private static final int VERSION = 1;
  public static final String CLIENT_INET_SOCKET_ADDRESS = "com.example.touchpad.SOCK_ADDRESS";

  private Context context;
  private Facilitator facilitator;
  private NetworkInterfaceMaster networkInterfaceMaster;

  private Thread serverThread;
  private ServerSocket serverSocket;



  public interface Facilitator {
    public void communicateServerAddresses(Map<Transport, List<InetAddress>> addresses, int serverSocketPort);
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
    this.networkInterfaceMaster = new NetworkInterfaceMaster(addresses -> {
      this.facilitator.communicateServerAddresses(addresses, this.serverSocket.getLocalPort());
    }, context);
    startServer();
  }

  private void runServer() {
    try (ServerSocket serverSocketAC = new ServerSocket(getPortForServer())){
      this.serverSocket = serverSocketAC;
      while(!serverSocket.isClosed()) {
        System.err.println("before accept, " + serverSocket.getLocalPort());
        Socket connection = serverSocket.accept();
        serviceConnection(connection);//same thread - one user per phone; doesn't throw an exception
        //when the server is closed and the file is still being sent - unintentional but desirable?
        //This is despite the fact that the accepted socket's port is the same as the serverSocket's
      }
    } catch (SocketException ioe) {//server is being closed and no io is happening, just the accept
      ioe.printStackTrace();
    } catch (Exception e) {//any other exception
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



    //client jar connection TODO, currently an assumption that the next client will be a pc one
    connection = serverSocket.accept();
    serverSocket.close();
    //to drain
    isClientConnection(connection);
  }

  private void sendJarToBrowserOverHttp(Socket socket) throws IOException{
    BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
    AssetFileDescriptor jarFileDescriptor = context.getAssets().openFd("client.jar");

    String httpHeader = "HTTP/1.1 200\nContent-Type: application/octet-stream\nContent-Length: ";
    httpHeader += jarFileDescriptor.getLength() + "\n\n";
    String charsetName = "UTF-8";
    outputStream.write(httpHeader.getBytes(charsetName));

    FileInputStream jarFileStream = jarFileDescriptor.createInputStream();
    int readBuff;
    while ((readBuff = jarFileStream.read()) != -1) {//TODO buffer
      outputStream.write(readBuff);
    }
    outputStream.flush();
  }

  private boolean isClientConnection(Socket connection) throws IOException{
    /*
    DataInputStream clientDataInputStream = new DataInputStream(connection.getInputStream());
    short checkValue = clientDataInputStream.readShort();
    return checkValue == Short.MAX_VALUE;
    */
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    try{
      for(int it =0; it < 40; ++it){
        System.err.println(it + ". " + bufferedReader.readLine());
      }
    } catch(Exception e){
      e.printStackTrace();
      System.err.println("exc");
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

  private int getPortForServer(){
    int[] ports = {12345, 23456, 34567, 55555};
    for (int port : ports){
      try(ServerSocket trySocket = new ServerSocket(port)){
        return port;
      } catch (IOException ioe){

      }
    }
    return 0;
  }

  public void startServer(){
    serverThread = new Thread(this::runServer);
    serverThread.start();
  }

  public void shutdownServer() {
    if (serverSocket != null) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("null serverSocket");
    }
  }
}
