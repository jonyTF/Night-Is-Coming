import java.net.*;
import java.io.*;

public class Server {
  public static void main(String[] args) throws IOException {
    int port = 1266;
    ServerSocket serverSocket = new ServerSocket(port);

    GameManager gm = new GameManager();
    Thread gmThread = new Thread(gm);
    gmThread.start();

    while (true) {
      System.out.println("Waiting for connection...");
      Socket clientSocket = serverSocket.accept();

      ServerThread st = new ServerThread(clientSocket, gm);
      gm.addThread(st);
      Thread thread = new Thread(st);
      thread.start();
    }
  }
}