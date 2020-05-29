import java.io.Serializable;

public class GameData implements Serializable {
  public static final int MAX_PLAYERS = 100;
  private MyHashMap<Integer, Player> playerMap;
  private GameMap gameMap;

  public GameData() {
    playerMap = new MyHashMap<Integer, Player>(MAX_PLAYERS);
    gameMap = new GameMap(20, 20);
    gameMap.generateGameMap();
  }

  public void updateGameObject(GameObject o) {
    gameMap.updateGameObject(o);
  }

  public void removeGameObject(int id) {
    gameMap.removeGameObject(id);
  }

  public void addPlayer(Player p) {
    playerMap.put(p.getId(), p);
  }

  public void updatePlayer(Player p) {
    playerMap.put(p.getId(), p);
  }

  public void removePlayerById(int id) {
    playerMap.remove(id);
  }

  public MyHashMap<Integer, Player> getPlayerMap() {
    return playerMap;
  }

  public GameMap getGameMap() {
    return gameMap;
  }
}