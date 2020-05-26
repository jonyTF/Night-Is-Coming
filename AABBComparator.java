import java.io.Serializable;
import java.util.Comparator;

public class AABBComparator implements Comparator, Serializable {
  private int axis;

  public AABBComparator(int axis) {
    this.axis = axis;
  }
  
  public int compare(Object o1, Object o2) {
    AABB a1 = (AABB)o1;
    AABB a2 = (AABB)o2;

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