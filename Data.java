import java.io.Serializable;

public class Data implements Serializable {
  public static final int ADD_PLAYER = 0;
  public static final int UPDATE_PLAYER = 1;
  public static final int REMOVE_PLAYER = 2;
  public static final int UPDATE_GAME_DATA = 3;
  public static final int ASSIGN_ID = 4;
  public static final int UPDATE_GAME_OBJECT = 5;
  public static final int REMOVE_GAME_OBJECT = 6;

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

  public String getTypeString() {
    switch (type) {
      case ADD_PLAYER:
        return "add_player";
      case UPDATE_PLAYER: 
        return "update_player";
      case REMOVE_PLAYER:
        return "move_player";
      case UPDATE_GAME_DATA:
        return "update_game_data";
      case ASSIGN_ID:
        return "assign_id";
      case UPDATE_GAME_OBJECT:
        return "update_game_object";
      case REMOVE_GAME_OBJECT:
        return "remove_game_object";
    }
    return "INVALID";
  }

  public String toString() {
    return "[Data type="+ getTypeString() +" object="+ object +"]";
  }
}