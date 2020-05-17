import java.net.*;
import java.io.*;

public class ServerThread implements Runnable{
  private Socket clientSocket;
  private GameManager gm;
  private ObjectOutputStream out;
  private int id;

  public ServerThread(Socket clientSocket, GameManager gm) {
    this.clientSocket = clientSocket;
    this.gm = gm;
    this.id = -1;
  }

  public int getId() {
    return id;
  }

  public void writeObject(Object object) {
    if (out != null && id != -1) {
      try {
        out.reset();
        out.writeObject(object);
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  public void run() {
    System.out.println(Thread.currentThread().getName() + ": connection opened.");

    try {
      out = new ObjectOutputStream(clientSocket.getOutputStream());
      ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

      String message = (String) in.readObject();
      System.out.println(message + ": " + Thread.currentThread().getName() + ": connection successful!");

      id = gm.addPlayer();
      gm.addThread(this);
      writeObject(new Data(Data.ASSIGN_ID, id));
      writeObject(new Data(Data.UPDATE_GAME_DATA, gm.getGameData()));

      while (true) {
        Data data = (Data)in.readObject();
        Object object = data.getObject();
        switch (data.getType()) {
          case Data.UPDATE_GAME_DATA:
            gm.setGameData((GameData)object);
            break;
          case Data.UPDATE_PLAYER:
            gm.updatePlayer((Player)object);
            break;
        }
      }
    } catch (IOException e) {
      System.out.println("Connection closed");
      System.out.println(e.getMessage());

      gm.removePlayer(id);
      gm.broadcastGameData();
    } catch (ClassNotFoundException e) {
      System.out.println(e);
    }
  }
}