import java.net.*;
import java.io.*;

public class GameManager implements Runnable {
  private DLList<ServerThread> serverThreads;
  private GameData gameData;

  private int curId = 0;

  public GameManager() {
    serverThreads = new DLList<ServerThread>();
    gameData = new GameData();
  }

  public void addThread(ServerThread st) {
    serverThreads.add(st);
  }

  public void removeThread(ServerThread st) {
    serverThreads.remove(st);
  }

  public synchronized void broadcast(int type, Object object) {
    Data data = new Data(type, object);
    for (int i = 0; i < serverThreads.size(); i++) {
      serverThreads.get(i).writeObject(data);
    }
  }

  public GameData getGameData() {
    return gameData;
  }

  public void broadcastGameData() {
    broadcast(Data.UPDATE_GAME_DATA, gameData);
  }

  public void setGameData(GameData gameData) {
    this.gameData = gameData;
    broadcastGameData();
  }

  public void run() {
    while (true) {
      gameData.getGameMap().sortGameObjects(AABB.X);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public int addPlayer() {
    curId++;

    int w = gameData.getGameMap().getWidth();
    int h = gameData.getGameMap().getHeight();
    double x = (int)(Math.random() * w/2 + w/4);
    double y = (int)(Math.random() * h/2 + h/4);
    gameData.addPlayer(new Player(x, y, curId));
    broadcast(Data.ADD_PLAYER, gameData.getPlayerMap().get(curId));

    return curId;
  }

  public void updatePlayer(Player p) {
    gameData.updatePlayer(p);
    broadcast(Data.UPDATE_PLAYER, gameData.getPlayerMap().get(p.getId()));
  }

  public void removePlayer(int id) {
    gameData.removePlayerById(id);
    for (int i = 0; i < serverThreads.size(); i++) {
      if (serverThreads.get(i).getId() == id) {
        serverThreads.remove(i);
        i--;
      }
    }
    broadcast(Data.REMOVE_PLAYER, id);
  }
}