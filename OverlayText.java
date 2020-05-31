public class OverlayText {
  private String text;
  private int x;
  private int y;

  public OverlayText(String text, int x, int y) {
    this.text = text;
    this.x = x;
    this.y = y;
  }

  public OverlayText() {
    this("", 0, 0);
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public String getText() {
    return text;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}