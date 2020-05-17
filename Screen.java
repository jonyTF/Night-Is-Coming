import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.net.URL;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


import java.io.*;
import java.net.*;

public class Screen extends JPanel implements KeyListener, FocusListener, MouseListener, MouseMotionListener {
  public static final int SCREEN_WIDTH = 800;
  public static final int SCREEN_HEIGHT = 600;

  public static final int KEY_W = 0;
  public static final int KEY_A = 1;
  public static final int KEY_S = 2;
  public static final int KEY_D = 3;

  private final Color skinColor = new Color(255, 219, 172);
  private final Color treeColor = new Color(119, 153, 76);
  private final Color grassColor = new Color(165, 212, 106);

  private ObjectOutputStream out;
  private int id;
  private GameData gameData;

  private boolean[] keyDown;
  private RenderingHints hints;
  private boolean windowFocused;
  
  public Screen() {
    this.setLayout(null);
    this.addKeyListener(this);
    this.addFocusListener(this);
    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.setFocusable(true);

    gameData = new GameData();
    id = -1;
    keyDown = new boolean[] {false, false, false, false};

    hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    windowFocused = false;
  }

  public Dimension getPreferredSize() {
    return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Set render options
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHints(hints);

    // Draw ground
    g2.setColor(grassColor);
    g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    if (getCurrentPlayer() != null) {
      g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 40));
      g2.drawString("Night is Coming", 100, 100);

      // Draw players
      MyHashMap<Integer, Player> playerMap = gameData.getPlayerMap();
      DLList<Integer> ids = playerMap.getKeys();
      for (int i = 0; i < ids.size(); i++) {
        drawPlayer(g2, playerMap.get(ids.get(i)));
      }

      // Draw background
      drawBackground(g2);
    }
  }

  private void drawPlayer(Graphics2D g2, Player p) {
    int playerWH = (int)(.5 * gameData.getMap().getPixelToGridRatio());
    int handWH = 3*playerWH/10;
    
    // Get body coords
    int x, y;
    if (id == p.getId()) {
      x = SCREEN_WIDTH/2;
      y = SCREEN_HEIGHT/2;
    } else {
      int[] pos = getTransformedPos(p.getX(), p.getY(), getCurrentPlayer());
      x = pos[0];
      y = pos[1];
    }

    // Get hand coords
    int hand1X = (int)(x+Math.cos(p.getAngleFacing() - 0.5)*playerWH/2);
    int hand1Y = (int)(y-Math.sin(p.getAngleFacing() - 0.5)*playerWH/2);
    int hand2X = (int)(x+Math.cos(p.getAngleFacing() + 0.5)*playerWH/2);
    int hand2Y = (int)(y-Math.sin(p.getAngleFacing() + 0.5)*playerWH/2);
    
    // Draw body
    g2.setColor(skinColor);
    g2.fillOval(x-playerWH/2, y-playerWH/2, playerWH, playerWH);
    //g2.setColor(Color.black);
    //g2.drawOval(x-playerWH/2, y-playerWH/2, playerWH, playerWH);
    
    // Draw hands
    g2.setColor(skinColor);
    g2.fillOval(hand1X-handWH/2, hand1Y-handWH/2, handWH, handWH);
    g2.fillOval(hand2X-handWH/2, hand2Y-handWH/2, handWH, handWH);
    g2.setColor(Color.black);
    g2.drawOval(hand1X-handWH/2, hand1Y-handWH/2, handWH, handWH);
    g2.drawOval(hand2X-handWH/2, hand2Y-handWH/2, handWH, handWH);
    
    // Draw ID
    g2.setColor(Color.black);
    g2.drawString("" + p.getId(), x-playerWH/2, y+playerWH/4);
  }

  private void drawBackground(Graphics2D g2) {
    int[][] grid = gameData.getMap().getGrid();
    for (int r = 0; r < grid.length; r++) {
      for (int c = 0; c < grid[r].length; c++) {
        switch(grid[r][c]) {
          case Map.TREE:
            int[] pos = getTransformedPos(c, r, getCurrentPlayer());
            drawTree(g2, pos[0], pos[1]);
            break;
        }
      }
    }
  }
  
  private void drawTree(Graphics2D g2, int x, int y) {
    int gridWH = gameData.getMap().getPixelToGridRatio();
    
    g2.setColor(treeColor);
    g2.fillOval(x, y, gridWH, gridWH);
  }

  private Player getCurrentPlayer() {
    if (id == -1)
      return null;
    return gameData.getPlayerMap().get(id);
  }

  private int[] getTransformedPos(double x, double y, Player p) {
    int ratio = gameData.getMap().getPixelToGridRatio();
    int newX = (int)(x*ratio - p.getX()*ratio + SCREEN_WIDTH/2);
    int newY = (int)(y*ratio - p.getY()*ratio + SCREEN_HEIGHT/2);

    return new int[] {newX, newY};
  }

  public void poll() throws IOException {
    String hostName = "localhost";
    int port = 1266;
    Socket serverSocket = new Socket(hostName, port);

    out = new ObjectOutputStream(serverSocket.getOutputStream());
    ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream());

    try {
      out.writeObject("Player connected.");
      
      // Set id
      handleData((Data)in.readObject());

      // Get initial gameData
      handleData((Data)in.readObject());
      
      // Start animating
      AnimationThread animationThread = new AnimationThread();
      animationThread.start();

      // Start sending game data
      SendPlayerDataThread sendPlayerDataThread = new SendPlayerDataThread();
      sendPlayerDataThread.start();

      while (true) {
        Data data = (Data) in.readObject();
        handleData(data);
        repaint();
      }
    } catch (UnknownHostException e) {
      System.err.println("Host unknown: " + hostName);
      System.exit(1);
    } catch (IOException e) {
      System.out.println(e);
      System.exit(1);
    } catch (ClassNotFoundException e) {
      System.err.println(e);
      System.exit(1);
    }
  }

  private void handleData(Data data) {
    Object object = data.getObject();
    switch (data.getType()) {
      case Data.ASSIGN_ID:
        id = (int)object;
        break;
      case Data.UPDATE_GAME_DATA:
        gameData = (GameData)object;
        break;
      case Data.ADD_PLAYER:
        gameData.addPlayer((Player)object);
        break;
      case Data.UPDATE_PLAYER:
        gameData.updatePlayer((Player)object);
        break;
      case Data.REMOVE_PLAYER:
        gameData.removePlayerById((int)object);
        break;
    }
    repaint();
  }

  private void sendGameData() {
    try {
      out.reset();
      out.writeObject(gameData);
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  private void sendPlayerData() {
    try {
      out.reset();
      out.writeObject(new Data(Data.UPDATE_PLAYER, getCurrentPlayer()));
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  // Key events
  public void keyPressed(KeyEvent e) {
    char keyChar = e.getKeyChar();

    if (!( keyChar == 'w' && keyChar == 's' )) {
      if (keyChar == 'w') {
        keyDown[KEY_W] = true;
      } else if (keyChar == 's') {
        keyDown[KEY_S] = true;
      }
    }

    if (!( keyChar == 'a' && keyChar == 'd' )) {
      if (keyChar == 'a') {
        keyDown[KEY_A] = true;
      } else if (keyChar == 'd') {
        keyDown[KEY_D] = true;
      }
    }
  }

  public void keyReleased(KeyEvent e) {
    char keyChar = e.getKeyChar();
    switch (keyChar) {
      case 'w':
        keyDown[KEY_W] = false;
        break;
      case 'a':
        keyDown[KEY_A] = false;
        break;
      case 's':
        keyDown[KEY_S] = false;
        break;
      case 'd':
        keyDown[KEY_D] = false;
        break;
    }
  }

  public void keyTyped(KeyEvent e) {}

  // Focus events
  public void focusGained(FocusEvent e) { windowFocused = true; }
  public void focusLost(FocusEvent e) {
    windowFocused = false;
    keyDown = new boolean[] {false, false, false, false};
  }

  // Mouse events
  public void mouseClicked(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}

  // Mouse Motion events
  public void mouseDragged(MouseEvent e) {}
  public void mouseMoved(MouseEvent e) {
    if (windowFocused) {
      // Get angle mouse is from center 
      double x = e.getX() - SCREEN_WIDTH/2;
      double y = SCREEN_HEIGHT/2 - e.getY();
      double angleFacing;
      if (x != 0) {
        angleFacing = Math.atan(y/x);
        if (x < 0)
          angleFacing += Math.PI;
        if (angleFacing < 0)
          angleFacing += 2*Math.PI;
      } else {
        if (y > 0)
          angleFacing = Math.PI/2;
        else
          angleFacing = 3*Math.PI/2;
      }

      Player currentPlayer = getCurrentPlayer();
      if (currentPlayer != null)
        currentPlayer.setAngleFacing(angleFacing);
    }
  }

  public void playSound(String filename) {
		try {
			URL url = this.getClass().getClassLoader().getResource(filename);
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(url));
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
  }
  
  private class AnimationThread extends Thread {
    public void run() {
      while (true) {
        /*
        Player p = getCurrentPlayer();
        if (p != null)
          p.move(keyDown);
        */
        
        MyHashMap<Integer, Player> playerMap = gameData.getPlayerMap();
        DLList<Integer> ids = playerMap.getKeys();
        for (int i = 0; i < ids.size(); i++) {
          Player p = playerMap.get(ids.get(i));
          if (ids.get(i) == id) 
            p.move(keyDown);
          else
            p.move();
        }
        
        repaint();
        
        try {
          Thread.sleep(1000/60);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  private class SendPlayerDataThread extends Thread {
    public void run() {
      while (true) {
        Player p = getCurrentPlayer();
        if (p != null)
          sendPlayerData();

        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      } 
    }
  }
}