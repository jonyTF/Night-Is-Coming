public class Test {
  public static void main(String[] args) {
    DLList<Integer> list = new DLList<Integer>();
    list.add(5);
    list.add(3);
    list.add(4);
    list.add(6);
    list.add(9);
    list.add(12);
    list.add(11);
    list.add(2);

    System.out.println(list);

    list.sort(DLList.INSERTION_SORT);

    System.out.println(list);
  }
}