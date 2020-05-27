import java.io.Serializable;
import java.util.Comparator;

public class DLList < E extends Comparable > implements Serializable {
    public static final int INSERTION_SORT = 0;

    private Node < E > dummy;
    private int size;

    public DLList() {
        dummy = new Node < E > (null);
        dummy.setNext(dummy);
        dummy.setPrev(dummy);
        size = 0;
    }

    private Node < E > getNode(int index) {
        Node < E > current = null;
        if (index < size && index >= 0) {
            if (index < size / 2) {
                current = dummy.next();
                for (int i = 0; i < index; i++) {
                    current = current.next();
                }
            } else {
                current = dummy.prev();
                for (int i = size - 1; i > index; i--) {
                    current = current.prev();
                }
            }
            return current;
        } else {
            System.out.println("INDEX " + index + " OUT OF BOUNDS!");
            return null;
        }
    }

    public void add(E data) {
        Node < E > newNode = new Node < E > (data);

        if (size == 0) {
            dummy.setNext(newNode);
            dummy.setPrev(newNode);
            newNode.setNext(dummy);
            newNode.setPrev(dummy);
        } else {
            // Add to the end of the list
            Node < E > prev = dummy.prev();
            prev.setNext(newNode);
            newNode.setPrev(prev);
            dummy.setPrev(newNode);
            newNode.setNext(dummy);
        }
        size++;
    }
	
	public void add(int index, E data) {
        if (index == size) {
            add(data);
        } else {
            Node<E> newNode = new Node<E>(data);
            Node<E> next = getNode(index);
            Node<E> prev = next.prev();
            prev.setNext(newNode);
            newNode.setPrev(prev);
            newNode.setNext(next);
            next.setPrev(newNode);
            
            size++;
        }
	}

    public E get(int index) {
        return getNode(index).get();
    }

    public void remove(int index) {
        Node < E > toRemove = getNode(index);
        Node < E > prev = toRemove.prev();
        Node < E > next = toRemove.next();

        prev.setNext(next);
        next.setPrev(prev);
        size--;
    }

    public void remove(E data) {
        Node < E > current = dummy.next();
        for (int i = 0; i < size; i++) {
            if (current.get().equals(data)) {
                Node < E > prev = current.prev();
                Node < E > next = current.next();

                prev.setNext(next);
                next.setPrev(prev);
                size--;
                return;
            }
            current = current.next();
        }
    }

    public boolean contains(E data) {
        Node < E > current = dummy.next();
        for (int i = 0; i < size; i++) {
            if (current.get().equals(data)) {
                return true;
            }
            current = current.next();
        }
        return false;
    }

    public int count(E data) {
        Node < E > current = dummy.next();
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (current.get().equals(data)) {
                count++;
            }
            current = current.next();
        }
        return count;
    }

    public void set(int index, E data) {
        getNode(index).set(data);
    }

    public String toString() {
        if (size == 0) 
            return "[]";

        String s = "[";
        Node < E > current = dummy.next();
        for (int i = 0; i < size; i++) {
            s += current.get().toString() + ", ";
            current = current.next();
        }
        return s.substring(0, s.length() - 2) + "]";
    }

    public int size() {
        return size;
    }

    public void sort(int type) {
        sort(type, new DefaultComparator());
    }

    public void sort(int type, Comparator comparator) {
        switch (type) {
            case INSERTION_SORT:
                insertionSort(comparator);    
                break;
        }
    }

    private void insertionSort(Comparator comparator) {
        if (size > 1) {
            Node<E> outer = dummy.next().next();
            for (int i = 1; i < size; i++) {
                Node<E> inner = outer;
                for (int j = i; j > 0; j--) {
                    if (comparator.compare(inner.prev().get(), inner.get()) < 0) {
                        // If previous is less than current
                        break;
                    }
                    E tmp = inner.get();
                    inner.set(inner.prev().get());
                    inner.prev().set(tmp);
                    inner = inner.prev();
                }
                outer = outer.next();
            }
        }
    }
}