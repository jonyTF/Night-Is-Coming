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

    int randX = (int)(Math.random() * width);
    int randY = (int)(Math.random() * height);
    addTree(randX, randY);
  }

  public void clearGameMap() {
    gameObjects = new DLList<GameObject>();
  }

  public DLList<GameObject> getGameObjects() {
    return gameObjects;
  }

  public void sortGameObjects(int axis) {
    gameObjects.sort(DLList.INSERTION_SORT, new AABBComparator(axis));
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
    /*if (o1.hasFlag(GameObject.IS_ROUND) && o2.hasFlag(GameObject.IS_ROUND)) {*/

    /*} else {*/
    AABB rect1 = o1.getAABB();
    AABB rect2 = o2.getAABB();
    if (rect1.top() > rect2.top() && rect1.top() < rect2.bottom() && rect1.left() > rect2.left() && rect1.left() < rect2.right())
      return true;
    if (rect2.top() > rect1.top() && rect2.top() < rect1.bottom() && rect2.left() > rect1.left() && rect2.left() < rect1.right())
      return true;
    //}
    return false;
  }
}