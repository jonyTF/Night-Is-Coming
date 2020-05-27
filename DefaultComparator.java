import java.io.Serializable;
import java.util.Comparator;

public class DefaultComparator implements Comparator<Comparable>, Serializable {
  public int compare(Comparable c1, Comparable c2) {
    return c1.compareTo(c2);
  }
}