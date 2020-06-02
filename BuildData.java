import java.awt.geom.Point2D;

public class BuildData {
  private int curMaterial;
  private BuildBlock curBuildBlock;
  private DLList<BuildBlock> curBuild;
  
  public BuildData() {
    curMaterial = GameObject.WOOD;
    curBuild = new DLList<BuildBlock>();
  }

  public int getCurMaterial() {
    return curMaterial;
  }

  public void setCurMaterial(int material) {
    this.curMaterial = material;
  }

  public void setCurBuildBlock(BuildBlock b) {
    this.curBuildBlock = b;
  }

  public BuildBlock getCurBuildBlock() {
    return curBuildBlock;
  }

  public DLList<BuildBlock> getCurBuild() {
    return curBuild;
  }

  public void resetCurBuild() {
    curBuild = new DLList<BuildBlock>();
  }

  public void returnResourcesToPlayer(Player p) {
    for (int i = 0; i < curBuild.size(); i++) {
      p.changeResources(curBuild.get(i).getMaterial(), 1);
    }
  }

  public void addCurBlockToCurBuild() {
    if (curBuildBlock != null)
      curBuild.add(curBuildBlock);
  }

  public void removeBlockAtPos(double x, double y) {
    curBuild.remove(new BuildBlock(-1, x, y));
  }

  public boolean blockExistsAtPos(double x, double y) {
    return curBuild.contains(new BuildBlock(-1, x, y));
  }

  public void addCurBuildToMap(GameMap m, Screen sc) {
    for (int i = 0; i < curBuild.size(); i++) {
      BuildBlock b = curBuild.get(i);
      b.setId(GameMap.getNewId());
      m.addGameObject(b);
      sc.updateGameObject(b);
    }
  }
}