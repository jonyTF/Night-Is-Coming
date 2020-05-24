import java.io.Serializable;

public class Map implements Serializable {
  public static final int INITIAL_PIXEL_TO_GRID_RATIO = 100;

  private DLList<GameObject> gameObjects;
  private double zoom;
  private int width;
  private int height;

  public Map(int width, int height) {
    gameObjects = new DLList<GameObject>();
    zoom = 1;
    this.width = width;
    this.height = height;
  }

  public void generateMap() {
    clearMap();

    int randX = (int)(Math.random() * width);
    int randY = (int)(Math.random() * height);
    addTree(randX, randY);
  }

  public void clearMap() {
    gameObjects = new DLList<GameObject>();
  }

  public DLList<GameObject> getGameObjects() {
    return gameObjects;
  }

  public void setZoom(int zoom) {
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
    gameObjects.add(
      new GameObject(
        GameObject.TREE, 
        x, 
        y, 
        GameObject.TREE_WH, 
        GameObject.TREE_WH, 
        GameObject.TREE_HP, 
        new int[]{ 
          GameObject.IS_ROUND, 
          GameObject.GET_SMALLER_ON_DAMAGE,
          GameObject.IS_COLLIDABLE
        })
    );
  }

  public static boolean isColliding(GameObject o1, GameObject o2) {
    if (o1.hasFlag(GameObject.IS_ROUND) && o2.hasFlag(GameObject.IS_ROUND)) {

    } else {

    }
    return false;
  }
}