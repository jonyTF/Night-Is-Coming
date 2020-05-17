import java.io.Serializable;

public class GameData implements Serializable {
  public static final int MAX_PLAYERS = 100;
  private MyHashMap<Integer, Player> playerMap;
  private Map map;

  public GameData() {
    playerMap = new MyHashMap<Integer, Player>(MAX_PLAYERS);
    map = new Map(10, 10);
    map.generateMap();
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

  public Map getMap() {
    return map;
  }
}