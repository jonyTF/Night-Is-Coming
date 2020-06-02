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

  // Flags
  public static final int IS_CIRCLE = 0;
  public static final int GET_SMALLER_ON_DAMAGE = 1;
  public static final int IS_COLLIDABLE = 2; // If you can collide with this object
  public static final int TL_COORDS = 3; // If X and Y are the top left coordinates
  public static final int IS_COLLECTABLE = 4;
  public static final int IS_RESOURCE = 5;
  public static final int SAME_WH = 6; // If width and height are the same
  public static final int IS_BLUEPRINT = 7;

  // Types
  public static final int EMPTY = -1;
  public static final int PLAYER = 100;
  public static final int TREE = 101;
  public static final int WOOD = 102;
  public static final int GRASS = 103;
  public static final int BOULDER = 104;
  public static final int STONE = 105;
  public static final int TOOL_BUILD = 106; // Hammers and such
  public static final int TOOL_UTIL = 107; // Pickaxes and such
  public static final int TOOL_MELEE = 108; // Swords
  public static final int TOOL_RANGED = 109; // Bow and Arrows/guns
  public static final int HAMMER = 110;
  public static final int PICKAXE = 111;
  public static final int BUILD_BLOCK = 112;

  // Dimensions
  public static final double PLAYER_WH = 0.5;
  public static final double TREE_WH = 0.3;
  public static final double WOOD_WH = 0.2;
  public static final double GRASS_WH = 0.05;
  public static final double BOULDER_WH = 0.6;
  public static final double STONE_WH = 0.2;
  public static final double BUILD_BLOCK_WH = 0.5;

  // HP
  public static final int PLAYER_HP = 100;
  public static final int TREE_HP = 5;
  public static final int BOULDER_HP = 10;
  public static final int BUILD_BLOCK_HP = 10;

  // Damage and build power
  public static final double HAMMER_BUILD_AMT = 0.1;

  public GameObject(int id, double x, double y) {
    this(id, -1, x, y, 0, 0, 1, 1, new int[0]);
  }

  public GameObject(int id, int type, double x, double y, double initialWH) {
    this(id, type, x, y, initialWH, initialWH, 1, 1, new int[0]);
  }

  public GameObject(int id, int type, double x, double y, double initialWidth, double initialHeight) {
    this(id, type, x, y, initialWidth, initialHeight, 1, 1, new int[0]);
  }

  public GameObject(int id, int type, double x, double y, double initialWidth, double initialHeight, int[] flags) {
    this(id, type, x, y, initialWidth, initialHeight, 1, 1, flags);
  }

  public GameObject(int id, int type, double x, double y, double initialWH, int[] flags) {
    this(id, type, x, y, initialWH, initialWH, 1, 1, flags);
  }

  public GameObject(int id, int type, double x, double y, double initialWidth, double initialHeight, int hp) {
    this(id, type, x, y, initialWidth, initialHeight, hp, hp, new int[0]);
  }

  public GameObject(int id, int type, double x, double y, double initialWH, int hp) {
    this(id, type, x, y, initialWH, initialWH, hp, hp, new int[0]);
  }

  public GameObject(int id, int type, double x, double y, double initialWidth, double initialHeight, int hp, int[] flags) {
    this(id, type, x, y, initialWidth, initialHeight, hp, hp, flags);
  }

  public GameObject(int id, int type, double x, double y, double initialWH, int hp, int[] flags) {
    this(id, type, x, y, initialWH, initialWH, hp, hp, flags);
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
    if (initialWidth == initialHeight)
      this.flags.add(SAME_WH);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public static int getToolTypeOf(int tool) {
    switch (tool) {
      case HAMMER:
        return TOOL_BUILD;
      case PICKAXE:
        return TOOL_UTIL;
      default:
        return -1;
    }
  }

  public static double getBuildAmtOf(int tool) {
    switch (tool) {
      case HAMMER:
        return HAMMER_BUILD_AMT;
      default:
        return 0;
    }
  }

  public static String getTypeString(int type) {
    switch (type) {
      case PLAYER:
        return "player";
      case TREE:
        return "tree";
      case WOOD:
        return "wood";
      case GRASS:
        return "grass";
      case BOULDER:
        return "boulder";
      case STONE:
        return "stone";
      case HAMMER:
        return "hammer";
      case PICKAXE:
        return "pickaxe";
    }
    return "INVALID";
  }

  public String getTypeString() {
    return getTypeString(type);
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

  public double getCenterX() {
    if (hasFlag(TL_COORDS)) {
      return x + getWidth()/2;
    }
    return x;
  }

  public double getCenterY() {
    if (hasFlag(TL_COORDS)) {
      return y + getHeight()/2;
    }
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

  public boolean hasFlag(Integer flag) {
    return flags.contains(flag);
  }

  public void addFlag(Integer flag) {
    flags.add(flag);
  }

  public void removeFlag(Integer flag) {
    flags.remove(flag);
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
        remnants.add(new GameObject(GameMap.getNewId(), WOOD, x-1.1*WOOD_WH, y-1.1*WOOD_WH, WOOD_WH, new int[]{IS_COLLECTABLE, IS_RESOURCE}));
        remnants.add(new GameObject(GameMap.getNewId(), WOOD, x+1.1*WOOD_WH, y+1.1*WOOD_WH, WOOD_WH, new int[]{IS_COLLECTABLE, IS_RESOURCE}));
        break;
      case BOULDER:
        remnants.add(new GameObject(GameMap.getNewId(), STONE, x-1.1*STONE_WH, y-1.1*STONE_WH, STONE_WH, new int[]{IS_COLLECTABLE, IS_RESOURCE}));
        remnants.add(new GameObject(GameMap.getNewId(), STONE, x-1.1*STONE_WH, y+1.1*STONE_WH, STONE_WH, new int[]{IS_COLLECTABLE, IS_RESOURCE}));
        remnants.add(new GameObject(GameMap.getNewId(), STONE, x+1.1*STONE_WH, y-1.1*STONE_WH, STONE_WH, new int[]{IS_COLLECTABLE, IS_RESOURCE}));
        remnants.add(new GameObject(GameMap.getNewId(), STONE, x+1.1*STONE_WH, y+1.1*STONE_WH, STONE_WH, new int[]{IS_COLLECTABLE, IS_RESOURCE}));
        break;
    }
    
    return remnants;
  }

  public boolean isCollidingWith(GameObject o) {
    return isColliding(this, o);
  }

  public static boolean isColliding(GameObject o1, GameObject o2) {
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