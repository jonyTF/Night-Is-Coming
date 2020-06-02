import java.io.Serializable;
import java.awt.geom.Point2D;

public class GameMap implements Serializable {
  public static final int INITIAL_PIXEL_TO_GRID_RATIO = 100;
  public static final double BUILD_INCREMENT = GameObject.BUILD_BLOCK_WH; // where building blocks line up on the grid. 
  public static int curId = 0;

  private MyHashMap<Integer, GameObject> gameObjects;
  private double zoom;
  private int width;
  private int height;

  public GameMap(int width, int height) {
    gameObjects = new MyHashMap<Integer, GameObject>();
    zoom = 1;
    this.width = width;
    this.height = height;
  }

  public void generateGameMap() {
    clearGameMap();

    for (int i = 0; i < 5; i++) {
      int randX = (int)(Math.random() * width);
      int randY = (int)(Math.random() * height);
      addTree(randX, randY);
    }

    for (int i = 0; i < 5; i++) {
      int randX = (int)(Math.random() * width);
      int randY = (int)(Math.random() * height);
      addBoulder(randX, randY);
    }

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        double randX = Math.random()+i;
        double randY = Math.random()+j;
        addGrass(randX, randY);
      }
    }
  }

  public Point2D.Double roundBuildPos(Point2D p) {
    return new Point2D.Double(roundBuildVal(p.getX()), roundBuildVal(p.getY()));
  }

  public double roundBuildVal(double val) {
    return Math.round(val*1/BUILD_INCREMENT) * BUILD_INCREMENT;
  }

  public void clearGameMap() {
    gameObjects = new MyHashMap<Integer, GameObject>();
  }

  public void addGameObject(GameObject o) {
    updateGameObject(o);
  }

  public void updateGameObject(GameObject o) {
    gameObjects.put(o.getId(), o);
  }

  public void removeGameObject(int id) {
    gameObjects.remove(id);
  }

  public MyHashMap<Integer, GameObject> getGameObjects() {
    return gameObjects;
  }

  public void sortGameObjects(int axis) {
    gameObjects.sort(DLList.INSERTION_SORT, new GameObjectAABBComparator(axis));
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  public double getZoom() {
    return zoom;
  }

  public int getPixelToGridRatio() {
    return (int)(zoom * INITIAL_PIXEL_TO_GRID_RATIO);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
  
  private void addTree(int x, int y) {
    int id = getNewId();
    gameObjects.put( 
      id,
      new GameObject(
        id,
        GameObject.TREE, 
        x, 
        y, 
        GameObject.TREE_WH,
        GameObject.TREE_HP, 
        new int[]{ 
          GameObject.IS_CIRCLE, 
          GameObject.GET_SMALLER_ON_DAMAGE,
          GameObject.IS_COLLIDABLE
        })
    );
  }

  private void addBoulder(int x, int y) {
    int id = getNewId();
    gameObjects.put( 
      id,
      new GameObject(
        id,
        GameObject.BOULDER, 
        x, 
        y, 
        GameObject.BOULDER_WH,
        GameObject.BOULDER_HP, 
        new int[]{ 
          GameObject.IS_CIRCLE, 
          GameObject.GET_SMALLER_ON_DAMAGE,
          GameObject.IS_COLLIDABLE
        })
    );
  }

  private void addGrass(double x, double y) {
    int id = getNewId();
    gameObjects.put( 
      id,
      new GameObject(
        id,
        GameObject.GRASS, 
        x, 
        y, 
        GameObject.GRASS_WH
      )
    );
  }

  public static int getNewId() {
    return curId++;
  }
}