import java.io.Serializable;
import java.util.Comparator;

public class MyHashMap<K extends Comparable, V> implements Serializable {
  private DLList<K> keys;
  private Object[] values;

  public MyHashMap() {
    keys = new DLList<K>();
    values = new Object[1000];
  }

  public MyHashMap(int size) {
    keys = new DLList<K>();
    values = new Object[size];
  }

  public void put(K key, V value) {
    if (!keys.contains(key))
      keys.add(key);
    
    values[key.hashCode() % values.length] = value;
  }

  public V get(K key) {
    @SuppressWarnings("unchecked")
    final V value = (V) values[key.hashCode() % values.length];
    return value;
  }

  public void remove(K key) {
    keys.remove(key);
    values[key.hashCode() % values.length] = null;
  }

  public DLList<K> getKeys() {
    return keys;
  }

  public void sort(int type) {
    keys.sort(type);
  }

  public void sort(int type, Comparator comparator) {
    keys.sort(type, comparator);
  }
}