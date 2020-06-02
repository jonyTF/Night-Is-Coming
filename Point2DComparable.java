import java.awt.geom.Point2D;

public class Point2DComparable extends Point2D.Double implements Comparable<Point2DComparable> {
  public Point2DComparable(double x, double y) {
    super(x, y);
  }

  public int compareTo(Point2DComparable p) {
    if (equals(p)) {
      return 0;
    } else if (getY() == p.getY()) {
      double diff = getX() - p.getX();
      if (diff > 0)
        return 1;
      else 
        return -1;
    } else {
      double diff = getY() - p.getY();
      if (diff > 0)
        return 1;
      else 
        return -1;
    }
  }

  @Override
  public boolean equals(Object o) {
    Point2DComparable p = (Point2DComparable)o;
    return getX() == p.getX() && getY() == p.getY();
  } 

  @Override
  public int hashCode() {
    return (int)(17*getX() + 37*getY());
  }
}