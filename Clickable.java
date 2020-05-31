public class Clickable implements Comparable<Clickable> {
  private int type;
  private int x;
  private int y;
  private int w;
  private int h;
  private Object extraData;

  public Clickable(int type, int x, int y, int w, int h) {
    this(type, x, y, w, h, null);
  }
  
  public Clickable(int type, int x, int y, int w, int h, Object extraData) {
    this.type = type;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.extraData = extraData;
  }

  public boolean contains(int x, int y) {
    return x >= this.x && x <= this.x+this.w && y >= this.y && y <= this.y+this.h;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getW() {
    return w;
  }

  public int getH() {
    return h;
  }

  public int getType() {
    return type;
  }

  public Object getExtraData() {
    return extraData;
  }

  @Override 
  public boolean equals(Object o) {
    Clickable c = (Clickable)o;
    return x == c.getX() && y == c.getY() && w == c.getW() && h == c.getH() && type == c.getType();
  }

  public int compareTo(Clickable c) {
    if (equals(c)) {
      return 0;
    } else if (y == c.getY()) {
      return x - c.getX();
    } else {
      return y - c.getY();
    }
  }
}