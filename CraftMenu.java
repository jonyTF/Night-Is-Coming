public class CraftMenu {
  private DLList<CraftItem> items;
  private int curItemType;

  public CraftMenu() {
    curItemType = -1;
    items = new DLList<CraftItem>();

    MyHashMap<Integer, Integer> pickaxeCost = new MyHashMap<Integer, Integer>();
    pickaxeCost.put(GameObject.WOOD, 6);
    items.add(
      new CraftItem(
        GameObject.PICKAXE,
        pickaxeCost,
        "Used to break boulders"
      )
    );

    MyHashMap<Integer, Integer> hammerCost = new MyHashMap<Integer, Integer>();
    hammerCost.put(GameObject.WOOD, 2);
    hammerCost.put(GameObject.STONE, 3);
    items.add(
      new CraftItem(
        GameObject.HAMMER,
        hammerCost,
        "Used to build stuff"
      )
    );
  }

  public void setCurItemType(int type) {
    curItemType = type;
  }

  public int getCurItemType() {
    return curItemType;
  }

  public CraftItem getCurItem() {
    if (curItemType == -1)
      return null;
    return items.get(
      new CraftItem(
        curItemType
      )
    );
  }
  
  public DLList<CraftItem> getItems() {
    return items;
  }
}