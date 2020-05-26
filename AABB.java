import java.io.Serializable;

public class AABB implements Serializable {
  private double left;
  private double top;
  private double right;
  private double bottom;

  public static final int X = 0;
  public static final int Y = 1;
  
  public AABB(double left, double top, double right, double bottom) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  public double[] getMins() {
    return new double[] {left, top};
  }

  public double[] getMaxes() {
    return new double[] {right, bottom};
  }

  public double[] getCenter() {
    return new double[] { (left+right)/2,  (right+bottom)/2 };
  }

  public double left() {
    return left;
  }

  public double top() {
    return top;
  }

  public double right() {
    return right;
  }

  public double bottom() {
    return bottom;
  }
}