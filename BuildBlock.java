import java.awt.geom.Point2D;

public class BuildBlock extends GameObject {
  private final int minOpacity = 130;

  private int material; // current material being used to build
  private double buildProgress; // from 0 to 1 of how much the block has been built
  
  public BuildBlock(int material, double x, double y) {
    super(
      -1,
      GameObject.BUILD_BLOCK, 
      x, 
      y, 
      GameObject.BUILD_BLOCK_WH,
      GameObject.PLAYER_HP, // TODO: CHANGE!!
      new int[]{
        GameObject.TL_COORDS,
      }
    );

    this.material = material;
  }

  public int getMaterial() {
    return material;
  }

  public double getBuildProgress() {
    return buildProgress;
  }

  public int getOpacity() {
    return minOpacity + (int)(buildProgress*(255-minOpacity));
  }

  public void setMaterial(int material) {
    this.material = material;
  }

  public void build(double change) {
    if (buildProgress + change < 1) {
      buildProgress += change;
    } else {
      buildProgress = 1;
      addFlag(GameObject.IS_COLLIDABLE);
    }
  }
}