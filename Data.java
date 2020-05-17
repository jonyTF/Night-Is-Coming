import java.io.Serializable;

public class Data implements Serializable {
  public static final int ADD_PLAYER = 0;
  public static final int UPDATE_PLAYER = 1;
  public static final int REMOVE_PLAYER = 2;
  public static final int UPDATE_GAME_DATA = 3;
  public static final int ASSIGN_ID = 4;

  private int type;
  private Object object;

  public Data(int type, Object object) {
    this.type = type;
    this.object = object;
  }

  public int getType() {
    return type;
  }

  public Object getObject() {
    return object;
  }
}