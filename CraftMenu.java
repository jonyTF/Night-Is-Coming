public class CraftMenu {
  private DLList<CraftItem> items;
  private int curItemIndex;

  public CraftMenu() {
    items = new DLList<CraftItem>();

    MyHashMap<Integer, Integer> pickaxeCost = new MyHashMap<Integer, Integer>();
    pickaxeCost.put(GameObject.WOOD, 6);
    items.add(new CraftItem(
      GameObject.PICKAXE,
      pickaxeCost,
      "Used to break boulders"
    ));

    MyHashMap<Integer, Integer> hammerCost = new MyHashMap<Integer, Integer>();
    hammerCost.put(GameObject.WOOD, 2);
    hammerCost.put(GameObject.STONE, 3);
    items.add(new CraftItem(
      GameObject.HAMMER,
      hammerCost,
      "Used to build stuff"
    ));
  }

  public void setCurItemIndex(int index) {
    curItemIndex = index;
  }

  public int getCurItemIndex() {
    return curItemIndex;
  }
  
  public DLList<CraftItem> getItems() {
    return items;
  }
}