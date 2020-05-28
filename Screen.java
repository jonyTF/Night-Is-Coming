import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.net.URL;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javax.swing.JButton;


import java.io.*;
import java.net.*;

public class Screen extends JPanel implements KeyListener, FocusListener, MouseListener, MouseMotionListener, ActionListener {
  public static final int SCREEN_WIDTH = 800;
  public static final int SCREEN_HEIGHT = 600;

  public static final int KEY_W = 0;
  public static final int KEY_A = 1;
  public static final int KEY_S = 2;
  public static final int KEY_D = 3;

  private final Color skinColor = new Color(255, 219, 172);
  private final Color treeColor = new Color(72, 166, 70);
  private final Color grassColor = new Color(165, 212, 106);
  private final Color woodColor = new Color(166, 105, 70);
  private final Color lightWoodColor = new Color(235, 209, 195);

  private ObjectOutputStream out;
  private int id;
  private GameData gameData;

  private boolean[] keyDown;
  private boolean mouseDown;
  private RenderingHints hints;
  private boolean windowFocused;
  private boolean playing;
  private boolean showInstructions;
  private String hintText;

  private JButton startBtn;
  private JButton instructionsBtn;
  private JButton closeBtn;
  
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
    mouseDown = false;

    hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    windowFocused = false;
    playing = false;
    showInstructions = false;
    hintText = "";

    startBtn = new JButton("Start");
    startBtn.setBounds(SCREEN_WIDTH/2-75, SCREEN_HEIGHT*5/6, 150, 30);
    startBtn.setFocusable(false);
    startBtn.addActionListener(this);
    this.add(startBtn);

    instructionsBtn = new JButton("Instructions");
    instructionsBtn.setBounds(SCREEN_WIDTH/2-75, SCREEN_HEIGHT*5/6+30+10, 150, 30);
    instructionsBtn.setFocusable(false);
    instructionsBtn.addActionListener(this);
    this.add(instructionsBtn);

    closeBtn = new JButton("Close");
    closeBtn.setBounds(SCREEN_WIDTH-20-100, 20, 100, 30);
    closeBtn.setFocusable(false);
    closeBtn.addActionListener(this);
    closeBtn.setVisible(false);
    this.add(closeBtn);
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

    if (!playing) {
      if (showInstructions) {
        drawInstructions(g2);
      } else {
        g2.setColor(Color.white);
        drawStringTC(g2, "Night is Coming", new Font(Font.MONOSPACED, Font.PLAIN, 40), SCREEN_WIDTH/2, SCREEN_HEIGHT/2 - 100);
      }
    } else if (getCurrentPlayer() != null) {
      // Draw players
      MyHashMap<Integer, Player> playerMap = gameData.getPlayerMap();
      DLList<Integer> ids = playerMap.getKeys();
      for (int i = 0; i < ids.size(); i++) {
        if (playing) {
          drawPlayer(g2, playerMap.get(ids.get(i)));
        }
      }

      // Draw background
      drawBackground(g2);

      // Draw Resources sidebar
      drawResources(g2);

      // Draw hint
      if (hintText.length() > 0)
        drawHint(g2);
    }
  }

  private void drawInstructions(Graphics2D g2) {
    g2.setColor(Color.gray);
    g2.fillRect(10, 10, SCREEN_WIDTH-20, SCREEN_HEIGHT-20);
    g2.setColor(Color.white);
    Font fontSmall = new Font(Font.MONOSPACED, Font.PLAIN, 20);
    drawStringTC(g2, "Instructions", new Font(Font.MONOSPACED, Font.PLAIN, 40), SCREEN_WIDTH/2, 20);

    String[] instructions = {
      "Use WASD to move",
      "Press E to pick up an item",
      "Move your mouse to aim",
      "Tap your mouse repeatedly while aiming at an object to break it",
      "Click items in your inventory to equip them"
    };
    for (int i = 0; i < instructions.length; i++) {
      drawStringTL(g2, instructions[i], fontSmall, 20, 60+i*20);
    }
  }

  private void drawHint(Graphics2D g2) {
    // Display hints to user such as "press E to pick up item"
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 20);
    FontMetrics metrics = getFontMetrics(font);
    
    g2.setColor(new Color(0, 0, 0, 130));
    fillRectCenter(g2, SCREEN_WIDTH/2, SCREEN_HEIGHT-15, metrics.stringWidth(hintText) + 20, 30);
    g2.setColor(Color.white);
    drawStringTC(g2, hintText, font, SCREEN_WIDTH/2, SCREEN_HEIGHT-30);
  }

  private void drawPlayer(Graphics2D g2, Player p) {
    int playerWH = (int)getScaledValue(p.getWidth());
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
    fillOvalCenter(g2, x, y, playerWH, playerWH);
    //g2.setColor(Color.black);
    //g2.drawOval(x-playerWH/2, y-playerWH/2, playerWH, playerWH);
    
    // Draw hands
    g2.setColor(skinColor);
    fillOvalCenter(g2, hand1X, hand1Y, handWH, handWH);
    fillOvalCenter(g2, hand2X, hand2Y, handWH, handWH);
    g2.setColor(Color.black);
    drawOvalCenter(g2, hand1X, hand1Y, handWH, handWH);
    drawOvalCenter(g2, hand2X, hand2Y, handWH, handWH);
    
    // Draw ID
    g2.setColor(Color.black);
    g2.drawString("" + p.getId(), x-playerWH/2, y+playerWH/4);
  }

  private void drawBackground(Graphics2D g2) {
    // Draw the objects contained in map

    MyHashMap<Integer, GameObject> gameObjects = gameData.getGameMap().getGameObjects();
    DLList<Integer> ids = gameObjects.getKeys();

    for (int i = 0; i < ids.size(); i++) {
      GameObject o = gameObjects.get(ids.get(i));
      switch (o.getType()) {
        case GameObject.TREE:
          drawTree(g2, o);
          break;
        case GameObject.WOOD:
          drawWood(g2, o);
          break;
      }
    }
  }
  
  private void drawTree(Graphics2D g2, GameObject treeObject) {
    int[] pos = getTransformedPos(treeObject.getX(), treeObject.getY(), getCurrentPlayer());
    int trunkWH = (int)getScaledValue(treeObject.getWidth());
    int leavesWH = (int)(trunkWH * 3);
    
    g2.setColor(woodColor);
    fillOvalCenter(g2, pos[0], pos[1], trunkWH, trunkWH);

    g2.setColor(new Color(treeColor.getRed(), treeColor.getGreen(), treeColor.getBlue(), 130));
    fillOvalCenter(g2, pos[0], pos[1], leavesWH, leavesWH);
  }

  private void drawWood(Graphics2D g2, GameObject woodObject) {
    int[] pos = getTransformedPos(woodObject.getX(), woodObject.getY(), getCurrentPlayer());
    int woodWH = (int)getScaledValue(woodObject.getWidth());

    drawWood(g2, pos[0], pos[1], woodWH);
  }

  private void drawWood(Graphics2D g2, int x, int y, int wh) {
    // Draw from center
    g2.setColor(woodColor);
    fillRectCenter(g2, x, y, wh, wh);
    fillOvalCenter(g2, x, y+wh/2, wh, (int)(0.4*wh));

    g2.setColor(lightWoodColor);
    fillOvalCenter(g2, x, y-wh/2, wh, (int)(0.4*wh));
  }

  private void drawResources(Graphics2D g2) {
    int width = 50;
    g2.setColor(new Color(0, 0, 0, 130));
    g2.fillRect(SCREEN_WIDTH-width, 0, width, SCREEN_HEIGHT);

    int resourceWH = width-15;
    int numH = 20;
    int resourceX = SCREEN_WIDTH-width/2;
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, numH);

    MyHashMap<Integer, Integer> resources = getCurrentPlayer().getResources();
    DLList<Integer> resourceTypes = resources.getKeys();
    for (int i = 0; i < resourceTypes.size(); i++) {
      int type = resourceTypes.get(i);
      int yStart = 50+i*(resourceWH + numH + 10);
      switch (type) {
        case GameObject.WOOD:
          drawWood(g2, resourceX, yStart, resourceWH);
          break;
      }
      g2.setColor(Color.white);
      drawStringTC(g2, resources.get(type).toString(), font, resourceX, yStart+resourceWH-10);
    }
  }

  private void fillOvalCenter(Graphics2D g2, int x, int y, int w, int h) {
    g2.fillOval(x-w/2, y-h/2, w, h);
  }

  private void drawOvalCenter(Graphics2D g2, int x, int y, int w, int h) {
    g2.drawOval(x-w/2, y-h/2, w, h);
  }

  private void fillRectCenter(Graphics2D g2, int x, int y, int w, int h) {
    g2.fillRect(x-w/2, y-h/2, w, h);
  }

  private Player getCurrentPlayer() {
    if (id == -1)
      return null;
    return gameData.getPlayerMap().get(id);
  }

  private int[] getTransformedPos(double x, double y, Player p) {
    // Gets position relative to player

    int newX = (int)(getScaledValue(x) - getScaledValue(p.getX()) + SCREEN_WIDTH/2);
    int newY = (int)(getScaledValue(y) - getScaledValue(p.getY()) + SCREEN_HEIGHT/2);

    return new int[] {newX, newY};
  }

  private double getScaledValue(double val) {
    // Converts grid coordinates to screen coordinates.
    
    int ratio = gameData.getGameMap().getPixelToGridRatio();
    return val*ratio; 
  }

  public void poll() throws IOException {
    // Wait until playing to connect
    while (!playing) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

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
    if (data.getType() != Data.UPDATE_PLAYER) 
      System.out.println(data);
    Object object = data.getObject();
    switch (data.getType()) {
      case Data.ASSIGN_ID:
        id = (int)object;
        break;
      case Data.UPDATE_GAME_DATA:
        gameData = (GameData)object;
        break;
      case Data.UPDATE_GAME_OBJECT:
        gameData.updateGameObject((GameObject)object);
        break;
      case Data.REMOVE_GAME_OBJECT:
        gameData.removeGameObject((int)object);
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

  private void sendPlayerData() {
    sendData(new Data(Data.UPDATE_PLAYER, getCurrentPlayer()));
  }

  private void updateGameObject(GameObject gameObject) {
    sendData(new Data(Data.UPDATE_GAME_OBJECT, gameObject));
  }

  private void removeGameObject(int id) {
    sendData(new Data(Data.REMOVE_GAME_OBJECT, id));
  } 

  private void sendData(Data data) {
    try {
      if (out != null) {
        out.reset();
        out.writeObject(data);
      }
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  // Key events
  public void keyPressed(KeyEvent e) {
    char keyChar = e.getKeyChar();
    
    // Movement
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

    // Picking up items
    GameObject collidingObject = getCurrentPlayer().getCollidingObject();
    if (keyChar == 'e' && collidingObject != null) {
      getCurrentPlayer().pickUpItem();
      // Remove object from map
      gameData.removeGameObject(collidingObject.getId());
      removeGameObject(collidingObject.getId());
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

  public void mousePressed(MouseEvent e) {
    mouseDown = true;
    if (getCurrentPlayer() != null) {
      GameObject objectFacing = getCurrentPlayer().getObjectFacing();
      if (objectFacing == null || objectFacing.hasFlag(GameObject.IS_COLLECTABLE)) {
        playSound("sound/whoosh1.wav");
      } else {
        switch (objectFacing.getType()) {
          case GameObject.TREE:
            playSound("sound/tree_impact.wav");
            objectFacing.damage(getCurrentPlayer().getDamage());
            System.out.println("damaged!");
            break;
        }
        updateGameObject(objectFacing);
      }
    }
  }
  
  public void mouseReleased(MouseEvent e) {
    mouseDown = false;
  }

  // Mouse Motion events
  public void mouseDragged(MouseEvent e) {}
  public void mouseMoved(MouseEvent e) {
    if (windowFocused) {
      // Get angle mouse is from center 
      double x = e.getX() - SCREEN_WIDTH/2;
      double y = SCREEN_HEIGHT/2 - e.getY();
      double angleFacing = Player.calculateAngle(x, y);

      Player currentPlayer = getCurrentPlayer();
      if (currentPlayer != null)
        currentPlayer.setAngleFacing(angleFacing);
    }
  }

  // Button press events
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == startBtn) {
      playing = true;
      startBtn.setVisible(false);
      instructionsBtn.setVisible(false);
      closeBtn.setVisible(false);
    } else if (e.getSource() == instructionsBtn) {
      showInstructions = true;
      startBtn.setVisible(false);
      instructionsBtn.setVisible(false);
      closeBtn.setVisible(true);
    } else if (e.getSource() == closeBtn) {
      showInstructions = false;
      startBtn.setVisible(true);
      instructionsBtn.setVisible(true);
      closeBtn.setVisible(false);
    }

    repaint();
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

  private void drawStringTL(Graphics2D g2, String string, Font font, int x, int y) {
    FontMetrics metrics = getFontMetrics(font);
    g2.setFont(font);
    g2.drawString(string, x, y+metrics.getHeight()-metrics.getDescent());
  }

  private void drawStringTC(Graphics2D g2, String string, Font font, int x, int y) {
    FontMetrics metrics = getFontMetrics(font);
    g2.setFont(font);
    g2.drawString(string, x - metrics.stringWidth(string)/2, y+metrics.getHeight()-metrics.getDescent());
  }
  
  private class AnimationThread extends Thread {
    public void run() {
      while (true) {
        if (playing) {
          
          // TODO: animate other players' movement so it's a smooth transition from one point to another
          Player p = getCurrentPlayer();
          if (p != null)
            p.move(keyDown, gameData.getGameMap());

          GameObject collidingObject = p.getCollidingObject();
          if (collidingObject != null && collidingObject.hasFlag(GameObject.IS_COLLECTABLE)) {
            hintText = "Press E to pick up " + collidingObject.getTypeString();
          } else {
            hintText = "";
          }
          

          /*
          MyHashMap<Integer, Player> playerMap = gameData.getPlayerMap();
          DLList<Integer> ids = playerMap.getKeys();
          for (int i = 0; i < ids.size(); i++) {
            Player p = playerMap.get(ids.get(i));
            if (ids.get(i) == id) 
              p.move(keyDown, gameData.getGameMap());
            else
              p.move();
          }
          */

          //System.out.println("Player Facing: " + getCurrentPlayer().getObjectFacing());
          
          repaint();
          
          try {
            Thread.sleep(1000/60);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
  }

  private class SendPlayerDataThread extends Thread {
    public void run() {
      while (true) {
        if (playing) {
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
}