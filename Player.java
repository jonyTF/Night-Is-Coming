import java.io.Serializable;
import java.util.Date;

public class Player extends GameObject implements Serializable {
  public static final double INITIAL_SPEED = 3;

  private int id;
  private double speed;
  private double angleFacing;
  private boolean[] keyDown;

  private GameMap gameMap;

  private long prevMoveTime;
  private long curTime;

  private GameObject gameObject;
  
  public Player(double x, double y, int id) {
    super(
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
    this.angleFacing = 0;
    this.keyDown = new boolean[] {false, false, false, false};

    Date date = new Date();
    this.curTime = date.getTime();
    this.prevMoveTime = curTime;
    this.gameMap = null;
  }

  public GameObject getGameObject() {
    return gameObject;
  }

  public int getId() {
    return id;
  }

  public double getSpeed() {
    return speed;
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

      // If colliding with another object,
      // Move player back to oldPos
      if (gameMap != null) {
        DLList<GameObject> gameObjects = gameMap.getGameObjects();
        for (int i = 0; i < gameObjects.size(); i++) {
          GameObject gameObject = gameObjects.get(i);
          
          // If collision not possible anymore, break
          if (gameObject.getAABB().getMins()[AABB.X] > this.getAABB().getMaxes()[AABB.X]) {
            break;
          }

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
        }
      }
    }
  }

  public void move() {
    move(keyDown, null);
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