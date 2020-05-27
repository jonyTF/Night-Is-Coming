import java.io.Serializable;
import java.util.Comparator;

public class GameObjectAABBComparator implements Comparator<GameObject>, Serializable {
  private int axis;

  public GameObjectAABBComparator(int axis) {
    this.axis = axis;
  }
  
  public int compare(GameObject o1, GameObject o2) {
    AABB a1 = o1.getAABB();
    AABB a2 = o2.getAABB();

    double a1Min = a1.getMins()[axis];
    double a2Min = a2.getMins()[axis];
    
    if (a1Min == a2Min) 
      return 0;
    else if (a1Min > a2Min) 
      return 1;
    else
      return -1;
  }
}