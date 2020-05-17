import java.io.Serializable;

public class Map implements Serializable {
  public static final int INITIAL_PIXEL_TO_GRID_RATIO = 100;

  public static final int EMPTY = 0;
  public static final int TREE = 1;

  private int[][] grid;
  private double zoom;

  public Map(int width, int height) {
    grid = new int[height][width];
    zoom = 1;
  }

  public void generateMap() {
    clearMap();

    int randR = (int)(Math.random() * grid.length);
    int randC = (int)(Math.random() * grid[0].length);
    grid[randR][randC] = TREE;
  }

  public void clearMap() {
    for (int r = 0; r < grid.length; r++) {
      for (int c = 0; c < grid[r].length; c++) {
        grid[r][c] = EMPTY;
      }
    }
  }

  public int[][] getGrid() {
    return grid;
  }

  public void setZoom(int zoom) {
    this.zoom = zoom;
  }

  public double getZoom() {
    return zoom;
  }

  public int getPixelToGridRatio() {
    return (int)(zoom * INITIAL_PIXEL_TO_GRID_RATIO);
  }

  public int getWidth() {
    return grid[0].length;
  }

  public int getHeight() {
    return grid.length;
  }
}