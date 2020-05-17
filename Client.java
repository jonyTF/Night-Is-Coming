import javax.swing.JFrame;
import java.io.*;

public class Client {
  public static void main(String[] args) {
    JFrame fr = new JFrame("Night is Coming | Client");
    Screen sc = new Screen();

    fr.add(sc);
    fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    fr.pack();
    fr.setVisible(true);

    try {
      sc.poll();  
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}