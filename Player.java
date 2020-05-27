import java.io.Serializable;
import java.util.Date;

public class Player extends GameObject implements Serializable {
  public static final double INITIAL_SPEED = 3;
  // The distance the player must be to an object to be considered "close" to it
  public static final double CLOSE_DIST = 0.3; 
  // The range in radians for a player to be considered "facing" an object 
  public static final double FACING_ANGLE_RANGE = Math.PI/3; 

  private int id;
  private double speed;
  private int damage;
  private double angleFacing;
  private boolean[] keyDown;
  private DLList<GameObject> objectsCloseTo;

  private long prevMoveTime;
  private long curTime;
  
  public Player(double x, double y, int id) {
    super(
      id,
      GameObject.PLAYER, 
      x, 
      y, 
      GameObject.PLAYER_WH, 
      GameObject.PLAYER_WH, 
      GameObject.PLAYER_HP, 
      new int[]{ 
        GameObject.IS_CIRCLE, 
        GameObject.IS_COLLIDABLE 
      }
    );

    this.id = id;
    this.speed = INITIAL_SPEED;
    this.damage = 1;
    this.angleFacing = 0;
    this.keyDown = new boolean[] {false, false, false, false};
    this.objectsCloseTo = new DLList<GameObject>();

    Date date = new Date();
    this.curTime = date.getTime();
    this.prevMoveTime = curTime;
  }

  public int getId() {
    return id;
  }

  public double getSpeed() {
    return speed;
  }

  public int getDamage() {
    return damage;
  }

  public void setAngleFacing(double angleFacing) {
    this.angleFacing = angleFacing;
  }

  public double getAngleFacing() {
    return angleFacing;
  }

  public void moveUp() {
    setY(getY() - speed*deltaTime()/1000);
  }

  public void moveDown() {
    setY(getY() + speed*deltaTime()/1000);
  }

  public void moveLeft() {
    setX(getX() - speed*deltaTime()/1000);
  }

  public void moveRight() {
    setX(getX() + speed*deltaTime()/1000);
  }

  public long deltaTime() {
    return curTime-prevMoveTime;
  }

  public void move(boolean[] keyDown, GameMap gameMap) {
    this.keyDown = keyDown;
    //this.gameMap = gameMap;
    Date date = new Date();
    prevMoveTime = curTime;
    curTime = date.getTime();

    if (keyDown[Screen.KEY_W] || keyDown[Screen.KEY_A] || keyDown[Screen.KEY_S] || keyDown[Screen.KEY_D]) {
      double[] oldPos = {getX(), getY()};

      if (keyDown[Screen.KEY_W])
        moveUp();
      
      if (keyDown[Screen.KEY_A])
        moveLeft();
    
      if (keyDown[Screen.KEY_S])
        moveDown();

      if (keyDown[Screen.KEY_D]) 
        moveRight();

      // Collision and Close To detection
      if (gameMap != null) {
        objectsCloseTo = new DLList<GameObject>();

        MyHashMap<Integer, GameObject> gameObjects = gameMap.getGameObjects();
        DLList<Integer> keys = gameObjects.getKeys();
        for (int i = 0; i < keys.size(); i++) {
          GameObject gameObject = gameObjects.get(keys.get(i));
          
          // If collision not possible anymore, break
          // TODO: reimplement this ?
          /*if (gameObject.getAABB().getMins()[AABB.X] > this.getAABB().getMaxes()[AABB.X]) {
            break;
          }*/
          
          // Prevent movement on collision
          boolean colliding = this.isCollidingWith(gameObject);
          if (colliding) {
            setX(oldPos[0]);
            setY(oldPos[1]);
          }
          /*if (colliding) {
            double diffX = oldPos[0] - getX();
            double diffY = oldPos[1] - getY();
            double angle = calculateAngle(diffX, diffY);
            double moveDist = 0.01;
            double moveX = moveDist*Math.cos(angle);
            double moveY = moveDist*Math.sin(angle);
            while (colliding) {
              setX(getX() + moveX);
              setY(getY() + moveY);

              colliding = this.isCollidingWith(gameObject);
            }
          }*/

          // Add objects that are close to player
          double dist = this.getDistanceTo(gameObject);
          if (dist <= CLOSE_DIST) {
            objectsCloseTo.add(gameObject);
          }
        }
      }
    }
  }

  public void move() {
    move(keyDown, null);
  }

  public GameObject getObjectFacing() {
    // Gets the object in objectsCloseTo that the player is currently facing
    // Returns null if there is no such object

    for (int i = 0; i < objectsCloseTo.size(); i++) {
      GameObject gameObject = objectsCloseTo.get(i);
      double angleToObject = calculateAngleToObject(gameObject);
      if (Math.abs(angleFacing - angleToObject) < FACING_ANGLE_RANGE/2) {
        return gameObject;
      }
    }
    return null;
  }

  public double calculateAngleToObject(GameObject o) {
    double xDiff = o.getX() - this.getX();
    double yDiff = this.getY() - o.getY();
    return calculateAngle(xDiff, yDiff);
  }

  public static double calculateAngle(double x, double y) {
    double angle;
    if (x != 0) {
      angle = Math.atan(y/x);
      if (x < 0)
        angle += Math.PI;
      if (angle < 0)
        angle += 2*Math.PI;
    } else {
      if (y > 0)
        angle = Math.PI/2;
      else
        angle = 3*Math.PI/2;
    }
    return angle;
  }
}