import java.io.Serializable;
import java.util.Date;

public class Player implements Serializable {
  public static final double INITIAL_SPEED = 3;

  private double x;
  private double y;
  private int id;
  private double speed;
  private double angleFacing;
  private boolean[] keyDown;

  private long prevMoveTime;
  private long curTime;
  
  public Player(double x, double y, int id) {
    this.x = x;
    this.y = y;
    this.id = id;
    this.speed = INITIAL_SPEED;
    this.angleFacing = 0;
    this.keyDown = new boolean[] {false, false, false, false};

    Date date = new Date();
    this.curTime = date.getTime();
    this.prevMoveTime = curTime;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
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
    y -= speed*deltaTime()/1000;
  }

  public void moveDown() {
    y += speed*deltaTime()/1000;
  }

  public void moveLeft() {
    x -= speed*deltaTime()/1000;
  }

  public void moveRight() {
    x += speed*deltaTime()/1000;
  }

  public long deltaTime() {
    return curTime-prevMoveTime;
  }

  public void move(boolean[] keyDown) {
    this.keyDown = keyDown;
    Date date = new Date();
    prevMoveTime = curTime;
    curTime = date.getTime();

    if (keyDown[Screen.KEY_W] || keyDown[Screen.KEY_A] || keyDown[Screen.KEY_S] || keyDown[Screen.KEY_D]) {
      if (keyDown[Screen.KEY_W])
        moveUp();
      
      if (keyDown[Screen.KEY_A])
        moveLeft();
    
      if (keyDown[Screen.KEY_S])
        moveDown();

      if (keyDown[Screen.KEY_D]) 
        moveRight();
    }
  }

  public void move() {
    move(keyDown);
  }
}