import java.io.Serializable;

public class GameObject implements Serializable, Comparable<GameObject> {
  // X and Y are the center coordinates of the object unless specified otherwise

  int id;

  private int type;
  private double x;
  private double y;
  private double initialWidth;
  private double initialHeight;
  private int hp;
  private int maxHp;

  private DLList<Integer> flags;

  public static final int IS_CIRCLE = 0;
  public static final int GET_SMALLER_ON_DAMAGE = 1;
  public static final int IS_COLLIDABLE = 2; // If you can collide with this object
  public static final int TL_COORDS = 3; // If X and Y are the top left coordinates
  public static final int IS_COLLECTABLE = 4;

  public static final int PLAYER = 0;
  public static final int TREE = 1;
  public static final int WOOD = 2;

  public static final double PLAYER_WH = 0.5;
  public static final double TREE_WH = 0.3;
  public static final double WOOD_WH = 0.2;

  public static final int PLAYER_HP = 100;
  public static final int TREE_HP = 5;

  public GameObject(int id, double x, double y) {
    this(id, -1, x, y, 0, 0, 1, 1, new int[0]);
  }

  public GameObject(int id, int type, double x, double y, double initialWidth, double initialHeight, int[] flags) {
    this(id, type, x, y, initialWidth, initialHeight, 1, 1, flags);
  }

  public GameObject(int id, int type, double x, double y, double initialWidth, double initialHeight, int hp) {
    this(id, type, x, y, initialWidth, initialHeight, hp, hp, new int[0]);
  }

  public GameObject(int id, int type, double x, double y, double initialWidth, double initialHeight, int hp, int[] flags) {
    this(id, type, x, y, initialWidth, initialHeight, hp, hp, flags);
  }

  public GameObject(int id, int type, double x, double y, double initialWidth, double initialHeight, int hp, int maxHp, int[] flags) {
    this.id = id;
    this.type = type;
    this.x = x;
    this.y = y;
    this.initialWidth = initialWidth;
    this.initialHeight = initialHeight;
    this.hp = hp;
    this.maxHp = maxHp;
    
    this.flags = new DLList<Integer>();
    for (int i = 0; i < flags.length; i++) {
      this.flags.add(flags[i]);
    }
  }

  public int getId() {
    return id;
  }

  public int compareTo(GameObject go) {
    if (equals(go)) {
      return 0;
    } else if (y == go.getY()) {
      double diff = x - go.getX();
      if (diff > 0)
        return 1;
      else 
        return -1;
    } else {
      double diff = y - go.getY();
      if (diff > 0)
        return 1;
      else 
        return -1;
    }
  }

  @Override
  public boolean equals(Object o) {
    GameObject go = (GameObject)o;
    return x == go.getX() && y == go.getY();
  } 

  public String getTypeString() {
    switch (type) {
      case PLAYER:
        return "player";
      case TREE:
        return "tree";
    }
    return "INVALID";
  }

  public String toString() {
    return "[GameObject type="+ getTypeString() +" hp="+ hp +"]";
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {
    this.y = y;
  }

  public int getHp() {
    return hp;
  }

  public void setHpToMax() {
    this.hp = maxHp;
  }

  public int getType() {
    return type;
  }

  public double getWidth() {
    if (hasFlag(GET_SMALLER_ON_DAMAGE)) {
      double minWidth = initialWidth/5;
      return minWidth + ((double)hp / maxHp)*(initialWidth - minWidth);
    } else {
      return initialWidth;
    }
  }

  public double getHeight() {
    if (hasFlag(GET_SMALLER_ON_DAMAGE)) {
      double minHeight = initialHeight/5;
      return minHeight + ((double)hp / maxHp)*(initialHeight - minHeight);
    } else {
      return initialHeight;
    }
  }

  public double getRadius() {
    // get the average width and divide it by 2
    return (getWidth()+getHeight())/2 / 2;
  }

  public AABB getAABB() {
    if (hasFlag(TL_COORDS)) {
      return new AABB(x, y, x+getWidth(), y+getHeight());
    } else {
      return new AABB(x-getWidth()/2, y-getHeight()/2, x+getWidth()/2, y+getHeight()/2);
    }
  }

  public DLList<Integer> getFlags() {
    return flags;
  }

  public boolean hasFlag(int flag) {
    return flags.contains(flag);
  }

  public void damage(int amount) {
    if (hp - amount > 0)
      hp -= amount;
    else
      hp = 0;
  }

  public void heal(int amount) {
    if (hp + amount < maxHp) 
      hp += amount;
    else
      hp = maxHp;
  }

  public boolean isDead() {
    return hp == 0;
  }

  public DLList<GameObject> getRemnants() {
    DLList<GameObject> remnants = new DLList<GameObject>();
    switch (type) {
      case TREE:
        remnants.add(new GameObject(GameMap.getNewId(), WOOD, x-1.1*WOOD_WH, y-1.1*WOOD_WH, WOOD_WH, WOOD_WH, new int[]{IS_COLLECTABLE}));
        remnants.add(new GameObject(GameMap.getNewId(), WOOD, x+1.1*WOOD_WH, y+1.1*WOOD_WH, WOOD_WH, WOOD_WH, new int[]{IS_COLLECTABLE}));
        break;
    }
    
    return remnants;
  }

  public boolean isCollidingWith(GameObject o) {
    return isColliding(this, o);
  }

  public static boolean isColliding(GameObject o1, GameObject o2) {
    if (o1.hasFlag(IS_COLLIDABLE) && o2.hasFlag(IS_COLLIDABLE)) {
      if (o1.hasFlag(IS_CIRCLE) && o2.hasFlag(IS_CIRCLE)) {
        // Circle collision
        return getDistanceBetween(o1, o2) < 0;
      } else {
        // Rectangle collision
        AABB rect1 = o1.getAABB();
        AABB rect2 = o2.getAABB();
        if (rect1.left() < rect2.right() && rect1.right() > rect2.left() && rect1.top() < rect2.bottom() && rect1.bottom() > rect2.top())
          return true;
      }
    }
    return false;
  }

  public double getDistanceTo(GameObject o) {
    return getDistanceBetween(this, o);
  }

  public static double getDistanceBetween(GameObject o1, GameObject o2) {
    double xDiff = o1.getX() - o2.getX();
    double yDiff = o1.getY() - o2.getY();
    double dist = Math.sqrt(xDiff*xDiff + yDiff*yDiff);

    return dist - (o1.getRadius() + o2.getRadius());
  }
}