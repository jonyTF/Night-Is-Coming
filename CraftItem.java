public class CraftItem implements Comparable<CraftItem> {
  private int type;
  private MyHashMap<Integer, Integer> cost;
  private String description;

  public CraftItem(int type) {
    this(type, null, null);
  } 
  
  public CraftItem(int type, MyHashMap<Integer, Integer> cost, String description) {
    this.type = type;
    this.cost = cost;
    this.description = description;
  } 

  public int getType() {
    return type;
  }

  public MyHashMap<Integer, Integer> getCost() {
    return cost;
  }

  public String getDescription() {
    return description;
  }

  public boolean canCraftWith(MyHashMap<Integer, Integer> resources) {
    // Returns whether the given resources can craft this item
    DLList<Integer> items = cost.getKeys();
    for (int i = 0; i < items.size(); i++) {
      int item = items.get(i);
      if (cost.get(item) > resources.get(item)) {
        return false;
      }
    }
    return true;
  }

  public MyHashMap<Integer, Integer> craft(MyHashMap<Integer, Integer> resources) {
    // returns a hashmap of the new resources map without the resources in cost
    DLList<Integer> items = cost.getKeys();
    for (int i = 0; i < items.size(); i++) {
      int item = items.get(i);
      int oldAmt = resources.get(item);
      int newAmt = oldAmt - cost.get(item);
      resources.put(item, newAmt);
    }
    return resources;
  }

  @Override
  public boolean equals(Object o) {
    CraftItem c = (CraftItem)o;
    return type == c.getType();
  }

  public int compareTo(CraftItem c) {
    return type - c.getType();
  }
}