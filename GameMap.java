import java.io.Serializable;

public class GameMap implements Serializable {
  public static final int INITIAL_PIXEL_TO_GRID_RATIO = 100;

  private DLList<GameObject> gameObjects;
  private double zoom;
  private int width;
  private int height;

  public GameMap(int width, int height) {
    gameObjects = new DLList<GameObject>();
    zoom = 1;
    this.width = width;
    this.height = height;
  }

  public void generateGameMap() {
    clearGameMap();

    for (int i = 0; i < 1; i++) {
      int randX = (int)(Math.random() * width);
      int randY = (int)(Math.random() * height);
      addTree(randX, randY);
    }
  }

  public void clearGameMap() {
    gameObjects = new DLList<GameObject>();
  }

  public DLList<GameObject> getGameObjects() {
    return gameObjects;
  }

  public void sortGameObjects(int axis) {
    gameObjects.sort(DLList.INSERTION_SORT, new GameObjectAABBComparator(axis));
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
          GameObject.IS_CIRCLE, 
          GameObject.GET_SMALLER_ON_DAMAGE,
          GameObject.IS_COLLIDABLE
        })
    );
  }
}