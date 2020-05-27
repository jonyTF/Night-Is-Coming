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

  public void broadcast(int type, Object object) {
    Data data = new Data(type, object);
    broadcast(data);
  }

  public synchronized void broadcast(Data data) {
    for (int i = 0; i < serverThreads.size(); i++) {
      serverThreads.get(i).writeObject(data);
    }
  }

  public void update(Data data) {
    Object object = data.getObject();
    switch (data.getType()) {
      case Data.UPDATE_GAME_DATA:
        setGameData((GameData)object);
        break;
      case Data.UPDATE_PLAYER:
        updatePlayer((Player)object);
        break;
      case Data.UPDATE_GAME_OBJECT:
        updateGameObject((GameObject)object);
        break;
    }
    broadcast(data);
  }

  public GameData getGameData() {
    return gameData;
  }

  public void setGameData(GameData gameData) {
    this.gameData = gameData;
  }

  public void updateGameObject(GameObject gameObject) {
    gameData.updateGameObject(gameObject);
  }

  public void run() {
    while (true) {
      // TODO: reimplement?
      //gameData.getGameMap().sortGameObjects(AABB.X);
      
      // Replaces object with remnants when object is dead
      MyHashMap<Integer, GameObject> gameObjects = gameData.getGameMap().getGameObjects();
      DLList<Integer> keys = gameObjects.getKeys();
      for (int i = 0; i < keys.size(); i++) {
        GameObject gameObject = gameObjects.get(keys.get(i));
        if (gameObject.isDead()) {
          gameObjects.remove(keys.get(i));
          DLList<GameObject> remnants = gameObject.getRemnants();
          for (int j = 0; j < remnants.size(); j++) {
            GameObject remnant = remnants.get(j);
            gameObjects.put(remnant.getId(), remnant);
            broadcast(Data.UPDATE_GAME_OBJECT, remnant);
          }
        }
      }


      try {
        Thread.sleep(50);
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