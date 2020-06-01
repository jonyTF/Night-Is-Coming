import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.Point;

import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

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
import java.util.Arrays;
import java.util.Date;

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

  public static final int CRAFT = 0;
  public static final int CRAFT_ITEM = 1;
  public static final int BUILD = 2;

  private final Color skinColor = new Color(255, 219, 172);
  private final Color treeColor = new Color(72, 166, 70);
  private final Color grassColor = new Color(165, 212, 106);
  private final Color woodColor = new Color(166, 105, 70);
  private final Color lightWoodColor = new Color(235, 209, 195);
  private final Color stoneColor = new Color(138, 138, 138);
  private final Color blueprintColor = new Color(109, 156, 232);

  private ObjectOutputStream out;
  private int id;
  private GameData gameData;

  private boolean[] keyDown;
  private boolean mouseDown; 
  private Point mousePos; 
  private RenderingHints hints;
  private boolean windowFocused;
  private boolean playing;
  private boolean showInstructions;
  private String hintText;
  private String overlayText;
  private long prevMouseMoveTime;

  private JButton startBtn;
  private JButton instructionsBtn;
  private JButton closeBtn;
  private JButton craftBtn;

  private DLList<Clickable> clickables;
  private Clickable curClickable;

  private int curMode; // Specifies which menu is currently up. -1 means no menu
  private CraftMenu craftMenu;
  
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
    mousePos = null;
    prevMouseMoveTime = 0;

    hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    windowFocused = false;
    playing = false;
    showInstructions = false;
    hintText = "";
    overlayText = "";

    clickables = new DLList<Clickable>();
    curClickable = null;

    curMode = -1;
    craftMenu = new CraftMenu();

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
    closeBtn.setBounds(0, 0, 100, 30);
    closeBtn.setFocusable(false);
    closeBtn.addActionListener(this);
    closeBtn.setVisible(false);
    this.add(closeBtn);

    craftBtn = new JButton("Craft");
    craftBtn.setBounds(0, 0, 100, 30);
    craftBtn.setFocusable(false);
    craftBtn.addActionListener(this);
    craftBtn.setVisible(false);
    this.add(craftBtn);
  }

  public Dimension getPreferredSize() {
    return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Clear clickables
    clickables = new DLList<Clickable>();

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

      // Draw sidebar
      drawSidebar(g2);

      // Draw hint
      if (hintText.length() > 0)
        drawHint(g2);
    }

    if (curMode != -1) {
      switch (curMode) {
        case CRAFT:
          drawCraftMenu(g2);
          break;
        case BUILD:
          drawBuildOverlay(g2);
          break;
      }
    }

    // Draw overlay
    if (overlayText.length() > 0)
      drawOverlay(g2);
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

    closeBtn.setLocation(SCREEN_WIDTH-20-100, 20);
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

  private void drawOverlay(Graphics2D g2) {
    // Draws an overlay that follows the mouse
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 20);
    FontMetrics metrics = getFontMetrics(font);

    int x = (int)mousePos.getX();
    int y = (int)mousePos.getY();

    g2.setColor(new Color(0, 0, 0, 130));
    g2.fillRect(x, y, metrics.stringWidth(overlayText) + 20, 30);
    g2.setColor(Color.white);
    drawStringTL(g2, overlayText, font, x + 10, y);
  }

  private void drawPlayer(Graphics2D g2, Player p) {
    int playerWH = (int)toScreenValue(p.getWidth());
    int handWH = 3*playerWH/10;
    
    // Get body coords
    int x, y;
    if (id == p.getId()) {
      x = SCREEN_WIDTH/2;
      y = SCREEN_HEIGHT/2;
    } else {
      Point2D.Double pos = getRelativePos(p.getX(), p.getY(), getCurrentPlayer());
      x = (int)pos.getX();
      y = (int)pos.getY();
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
      Point2D.Double pos = getRelativePos(o.getX(), o.getY(), getCurrentPlayer());
      int width = (int)toScreenValue(o.getWidth());
      int height = (int)toScreenValue(o.getHeight());
      
      drawGameObject(g2, o.getType(), (int)pos.getX(), (int)pos.getY(), width, height); 
    }
  }

  private void drawGameObject(Graphics2D g2, int type, int x, int y, int width, int height) {
    switch (type) {
      case GameObject.TREE:
        drawTree(g2, x, y, width);
        break;
      case GameObject.WOOD:
        drawWood(g2, x, y, width);
        break;
      case GameObject.GRASS:
        drawGrass(g2, x, y, width);
        break;
      case GameObject.BOULDER:
        drawBoulder(g2, x, y, width);
        break;
      case GameObject.STONE:
        drawStone(g2, x, y, width);
        break;
    }
  }
  
  private void drawTree(Graphics2D g2, int x, int y, int trunkWH) {
    // C coords
    int leavesWH = (int)(trunkWH * 3);
    
    g2.setColor(woodColor);
    fillOvalCenter(g2, x, y, trunkWH, trunkWH);

    g2.setColor(new Color(treeColor.getRed(), treeColor.getGreen(), treeColor.getBlue(), 130));
    fillOvalCenter(g2, x, y, leavesWH, leavesWH);
  }

  private void drawWood(Graphics2D g2, int x, int y, int wh) {
    // // C coords
    g2.setColor(woodColor);
    fillRectCenter(g2, x, y, wh, wh);
    fillOvalCenter(g2, x, y+wh/2, wh, (int)(0.4*wh));

    g2.setColor(lightWoodColor);
    fillOvalCenter(g2, x, y-wh/2, wh, (int)(0.4*wh));
  }

  private void drawGrass(Graphics2D g2, int x, int y, int wh) {
    // C coords
    g2.setColor(treeColor);
    fillRectCenter(g2, x, y, wh, wh);
  }

  private void drawBoulder(Graphics2D g2, int x, int y, int wh) {
    // C coords
    g2.setColor(stoneColor.brighter());
    fillOvalCenter(g2, x, y, wh, wh);
    g2.setColor(stoneColor);
    fillOvalCenter(g2, x, y, (int)(.9*wh), (int)(.9*wh));
  }

  private void drawStone(Graphics2D g2, int x, int y, int wh) {
    g2.setColor(stoneColor);
    fillOvalCenter(g2, x, y, wh, wh);
  }

  private void drawSidebar(Graphics2D g2) {
    int width = 50;
    int xCenter = SCREEN_WIDTH-width/2;
    g2.setColor(new Color(0, 0, 0, 130));
    g2.fillRect(SCREEN_WIDTH-width, 0, width, SCREEN_HEIGHT);

    drawResources(g2, xCenter, 20, width);
    drawTools(g2, xCenter, SCREEN_WIDTH/4, width);

    drawBlueprintIcon(g2, xCenter, SCREEN_HEIGHT-2*10-2*width, (int)(.8*width));
    drawCraftIcon(g2, xCenter, SCREEN_HEIGHT-10-width, (int)(.8*width));
  }

  private void drawResources(Graphics2D g2, int xCenter, int yStart, int width) {
    int resourceWH = width-15;
    int numH = 20;
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, numH);

    MyHashMap<Integer, Integer> resources = getCurrentPlayer().getResources();
    DLList<Integer> resourceTypes = resources.getKeys();
    for (int i = 0; i < resourceTypes.size(); i++) {
      int type = resourceTypes.get(i);
      int yCenter = yStart+i*(resourceWH + 20) + resourceWH/2;
      drawGameObject(g2, type, xCenter, yCenter, resourceWH, resourceWH);
      g2.setColor(Color.white);
      drawStringC(g2, resources.get(type).toString(), font, xCenter, yCenter);
    }
  }

  private void drawTools(Graphics2D g2, int xCenter, int yStart, int width) {
    int slotWH = width-15;

    MyHashMap<Integer, Integer> tools = getCurrentPlayer().getTools();
    DLList<Integer> types = tools.getKeys();
    for (int i = 0; i < types.size(); i++) {
      int y = yStart+i*(slotWH + 10) + slotWH/2;
      g2.setColor(Color.white.darker());
      fillRectCenter(g2, xCenter, y, slotWH, slotWH);
      drawCraftItem(g2, tools.get(types.get(i)), xCenter, y, (int)(.8*slotWH));
    }
  }

  private void drawCraftItem(Graphics2D g2, int type, int x, int y, int wh) {
    // C coords
    switch (type) {
      case GameObject.PICKAXE:
        break;
      case GameObject.HAMMER:
        drawHammer(g2, x, y, wh);
        break;
    }
  }

  private void drawCraftMenu(Graphics2D g2) {
    // Draw at center of screen
    // One big box displaying item + craft button
    // 4 x 2 grid
    int padding = 10;
    int gridWH = 40;
    int itemWH = (int)(.8*gridWH);
    int bigBoxWH = 2*gridWH + padding;
    int bigItemWH = (int)(.8*bigBoxWH);
    int menuW = padding*5 + gridWH*4;
    int menuH = padding*6 + gridWH*4 + 60; // 60 is for the craft + close button

    int menuX = SCREEN_WIDTH/2 - menuW/2;
    int menuY = SCREEN_HEIGHT/2 - menuH/2;

    // Draw entire menu box
    g2.setColor(new Color(0, 0, 0, 130));
    g2.fillRect(menuX, menuY, menuW, menuH);

    // Draw big box
    g2.setColor(Color.white.darker());
    g2.fillRect(menuX+padding, menuY+padding, bigBoxWH, bigBoxWH);

    // Draw cur item and its type and description
    CraftItem curItem = craftMenu.getCurItem();
    if (curItem != null) {
      drawCraftItem(g2, craftMenu.getCurItemType(), menuX+padding + bigBoxWH/2, menuY+padding + bigBoxWH/2, bigItemWH);
      int x = menuX+padding+bigBoxWH+padding;
      int yStart = menuY+padding;
      int width = menuX+menuW-padding-x;

      Font fontBig = new Font(Font.MONOSPACED, Font.PLAIN, 20);
      Font fontSmall = new Font(Font.MONOSPACED, Font.PLAIN, 15);

      g2.setColor(Color.white);
      BBox titleBBox = drawStringWordWrap(g2, GameObject.getTypeString(craftMenu.getCurItemType()).toUpperCase(), fontBig, x, yStart, width);
      drawStringWordWrap(g2, curItem.getDescription(), fontSmall, x, (int)(yStart+titleBBox.getHeight()), width);

      if (curItem.canCraftWith(getCurrentPlayer().getResources()))
        craftBtn.setEnabled(true);
      else 
        craftBtn.setEnabled(false);
    } else {
      craftBtn.setEnabled(false);
    }
    
    int cols = 4;
    int rows = 2;
    for (int i = 0; i < cols; i++) {
      for (int j = 0; j < rows; j++) {
        int x = menuX+padding + i*(gridWH+padding);
        int y = menuY+padding*2+bigBoxWH + j*(gridWH+padding) + 30;

        // draw box
        g2.setColor(Color.white.darker());
        g2.fillRect(x, y, gridWH, gridWH);
        
        int index = j*cols + i;
        
        // draw all the craft items defined in craft menu 
        if (index < craftMenu.getItems().size()) {
          CraftItem item = craftMenu.getItems().get(index);
          clickables.add(new Clickable(CRAFT_ITEM, x, y, gridWH, gridWH, item));

          if (!item.canCraftWith(getCurrentPlayer().getResources())) {
            // draw red overlay showing you can't craft it
            g2.setColor(new Color(230, 0, 0, 60));
            g2.fillRect(x, y, gridWH, gridWH);
          }

          // draw the actual item
          drawCraftItem(g2, item.getType(), x+gridWH/2, y+gridWH/2, itemWH);
        }
        
      }
    }

    // Draw buttons
    Point closeBtnPos = new Point(menuX+menuW-padding-100, menuY+menuH-padding-30);
    if (!closeBtn.getLocation().equals(closeBtnPos))
      closeBtn.setLocation(closeBtnPos);
    closeBtn.setVisible(true);

    Point craftBtnPos = new Point(menuX+padding, menuY+padding+bigBoxWH);
    if (!craftBtn.getLocation().equals(craftBtnPos))
      craftBtn.setLocation(craftBtnPos);
    Dimension craftBtnDim = new Dimension(bigBoxWH, 30);
    if (!craftBtn.getSize().equals(craftBtnDim))
      craftBtn.setSize(craftBtnDim);
    craftBtn.setVisible(true);
  }

  private void drawBuildOverlay(Graphics2D g2) {
    int wh = (int)toScreenValue(GameMap.BUILD_INCREMENT);
    
    Point2D centeredMousePos = new Point2D.Double(mousePos.getX() - wh/2, mousePos.getY() - wh/2);
    Point2D mouseGridPos = getGridPosFromRelative(centeredMousePos, getCurrentPlayer());
    Point2D roundedPos = getRelativePos(gameData.getGameMap().roundBuildPos(mouseGridPos), getCurrentPlayer());

    int x = (int)roundedPos.getX();
    int y = (int)roundedPos.getY();
    g2.setColor(Color.white);
    g2.drawRect(x, y, wh, wh);
  }

  private void drawBlueprintIcon(Graphics2D g2, int x, int y, int wh) {
    // x center, y top coords
    if (playing)
      clickables.add(new Clickable(BUILD, x-wh/2, y, wh, wh));

    y += wh/2;
    int innerWH = (int)(.8*wh);
    int innerOffset = wh/2-innerWH/2;

    g2.setColor(blueprintColor);
    fillRectCenter(g2, x, y, wh, wh);
    g2.setColor(Color.white);
    drawRectCenter(g2, x, y, innerWH, innerWH);

    x -= wh/2;
    y -= wh/2;
    for (int row = 0; row < 3; row++) {
      int x1 = x + innerOffset;
      int x2 = x+wh - innerOffset;
      int y12 = y + innerOffset + (row+1)*innerWH/4;
      g2.drawLine(x1, y12, x2, y12);
    }

    for (int col = 0; col < 3; col++) {
      int y1 = y + innerOffset;
      int y2 = y+wh - innerOffset;
      int x12 = x + innerOffset + (col+1)*innerWH/4;
      g2.drawLine(x12, y1, x12, y2);
    }
  }

  private void drawCraftIcon(Graphics2D g2, int x, int y, int wh) {
    // x center, y top coords
    if (playing)
      clickables.add(new Clickable(CRAFT, x-wh/2, y, wh, wh));

    y += wh/2;

    AffineTransform old = g2.getTransform();
    g2.rotate(-Math.PI/4, x, y);
    drawWrench(g2, x, y, wh);
    g2.setTransform(old);
    g2.rotate(Math.PI/4, x, y);
    drawHammer(g2, x, y, wh);
    g2.setTransform(old);
  }

  private void drawWrench(Graphics2D g2, int x, int y, int wh) {
    // C coords
    g2.setColor(stoneColor);
    fillRectCenter(g2, x, y, (int)(0.2*wh), (int)(0.6*wh));
    Ellipse2D topCircle = getEllipseCenter(x, y + (0.7*wh/2), (0.3*wh), (0.3*wh));
    Ellipse2D topClip = getEllipseCenter(x, y + (1*wh/2), (0.15*wh), (0.25*wh));
    Ellipse2D bottomCircle = getEllipseCenter(x, y - (0.7*wh/2), (0.3*wh), (0.3*wh));
    Ellipse2D bottomClip = getEllipseCenter(x, y - (1*wh/2), (0.15*wh), (0.25*wh));

    Area top = new Area(topCircle);
    top.subtract(new Area(topClip));
    Area bottom = new Area(bottomCircle);
    bottom.subtract(new Area(bottomClip));

    g2.fill(top);
    g2.fill(bottom);
  }

  private void drawHammer(Graphics2D g2, int x, int y, int wh) {
    // C coords
    RoundRectangle2D handle = getRoundRectangleCenter(x, y, 0.2*wh, wh, 0.1*wh, 0.1*wh);
    RoundRectangle2D head = getRoundRectangleCenter(x, y-0.7*wh/2, 0.7*wh, 0.7*wh/2, 0.1*wh, 0.1*wh);
    g2.setColor(woodColor);
    g2.fill(handle);
    g2.setColor(stoneColor);
    g2.fill(head);
  }

  private String getCraftRequirements(CraftItem c) {
    String s = "Requires ";
    DLList<Integer> resources = c.getCost().getKeys();
    for (int i = 0; i < resources.size(); i++) {
      if (i != 0)
        s += ", ";
      
      int resource = resources.get(i);
      s += c.getCost().get(resource) + " " + GameObject.getTypeString(resource);
    }
    return s;
  }

  private Ellipse2D getEllipseCenter(double x, double y, double w, double h) {
    return new Ellipse2D.Double(x-w/2, y-h/2, w, h);
  }

  private RoundRectangle2D getRoundRectangleCenter(double x, double y, double w, double h, double arcw, double arch) {
    return new RoundRectangle2D.Double(x-w/2, y-h/2, w, h, arcw, arch);
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

  private void drawRectCenter(Graphics2D g2, int x, int y, int w, int h) {
    g2.drawRect(x-w/2, y-h/2, w, h);
  }

  private Player getCurrentPlayer() {
    if (id == -1)
      return null;
    return gameData.getPlayerMap().get(id);
  }

  private Point2D.Double getRelativePos(double x, double y, Player p) {
    // Gets position relative to player

    int newX = (int)(toScreenValue(x) - toScreenValue(p.getX()) + SCREEN_WIDTH/2);
    int newY = (int)(toScreenValue(y) - toScreenValue(p.getY()) + SCREEN_HEIGHT/2);

    return new Point2D.Double(newX, newY);
  }

  private Point2D.Double getRelativePos(Point pos, Player p) {
    // Gets position relative to player
    return getRelativePos(pos.getX(), pos.getY(), p);
  }

  private Point2D.Double getRelativePos(Point2D pos, Player p) {
    // Gets position relative to player
    return getRelativePos(pos.getX(), pos.getY(), p);
  }

  private Point2D.Double getGridPosFromRelative(double x, double y, Player p) {
    // Gets grid position based on position relative to player

    double newX = toGridValue(x - SCREEN_WIDTH/2 + toScreenValue(p.getX()));
    double newY = toGridValue(y - SCREEN_HEIGHT/2 + toScreenValue(p.getY()));

    return new Point2D.Double(newX, newY);
  }

  private Point2D.Double getGridPosFromRelative(Point pos, Player p) {
    // Gets position relative to player
    return getGridPosFromRelative(pos.getX(), pos.getY(), p);
  }

  private Point2D.Double getGridPosFromRelative(Point2D pos, Player p) {
    // Gets position relative to player
    return getGridPosFromRelative(pos.getX(), pos.getY(), p);
  }

  private double toScreenValue(double val) {
    // Converts grid coordinates to screen coordinates.
    
    int ratio = gameData.getGameMap().getPixelToGridRatio();
    return val*ratio; 
  }

  private double toGridValue(double val) {
    // Converts screen coordinates to grid coordinates.
    int ratio = gameData.getGameMap().getPixelToGridRatio();
    return val/ratio; 
  }

  /*
  private Point toScreenPoint(Point p) {
    p.setLocation(toScreenValue(p.getX()), toScreenValue(p.getY()));
    return p;
  }

  private Point toGridPoint(Point p) {
    p.setLocation(toGridValue(p.getX()), toGridValue(p.getY()));
    return p;
  }
  */

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
    if (getCurrentPlayer() != null) {
      GameObject collidingObject = getCurrentPlayer().getCollidingObject();
      if (keyChar == 'e' && collidingObject != null) {
        getCurrentPlayer().pickUpItem();
        // Remove object from map
        gameData.removeGameObject(collidingObject.getId());
        removeGameObject(collidingObject.getId());
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

  public void mousePressed(MouseEvent e) {
    mouseDown = true;
    if (getCurrentPlayer() != null && curMode == -1) {
      // Damage object facing
      GameObject objectFacing = getCurrentPlayer().getObjectFacing();
      if (objectFacing == null || objectFacing.hasFlag(GameObject.IS_COLLECTABLE)) {
        playSound("sound/whoosh1.wav");
      } else {
        switch (objectFacing.getType()) {
          case GameObject.TREE:
            playSound("sound/tree_impact.wav");
            break;
          case GameObject.BOULDER:
            playSound("sound/stone_impact.wav");
            break;
        }
        objectFacing.damage(getCurrentPlayer().getDamage());
        updateGameObject(objectFacing);
      }
    }

    // Check for click on Clickables
    if (curClickable != null) {
      switch(curClickable.getType()) {
        case CRAFT:
          curMode = CRAFT;
          break;
        case CRAFT_ITEM:
          CraftItem item = (CraftItem)(curClickable.getExtraData());
          craftMenu.setCurItemType(item.getType());
          break;
        case BUILD:
          curMode = BUILD;
          gameData.getGameMap().setZoom(0.5);
          break;
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
      mousePos = e.getPoint();

      // Get angle mouse is from center 
      double x = e.getX() - SCREEN_WIDTH/2;
      double y = SCREEN_HEIGHT/2 - e.getY();
      double angleFacing = Player.calculateAngle(x, y);

      // Set player angle facing
      Player currentPlayer = getCurrentPlayer();
      if (currentPlayer != null)
        currentPlayer.setAngleFacing(angleFacing);

      // Only update clickable cursor periodically (60fps)
      Date date = new Date();
      long curTime = date.getTime();
      if (curTime - prevMouseMoveTime > 1000/60) {
        prevMouseMoveTime = curTime;
        // Display hand cursor when on clickable
        overlayText = "";
        boolean displayHandCursor = false;
        for (int i = 0; i < clickables.size(); i++) {
          Clickable clickable = clickables.get(i);
          if (clickable.contains(e.getX(), e.getY())) {
            curClickable = clickable;
            displayHandCursor = true;
            break;
          }
        } 
        if (displayHandCursor) {
          setCursor(new Cursor(Cursor.HAND_CURSOR));

          switch (curClickable.getType()) {
            case CRAFT_ITEM:
              overlayText = getCraftRequirements((CraftItem)curClickable.getExtraData());
              break;
          }
        } else { 
          setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          curClickable = null;
        }
      }
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
      if (showInstructions) {
        showInstructions = false;
        startBtn.setVisible(true);
        instructionsBtn.setVisible(true);
        closeBtn.setVisible(false);
      } else if (curMode == CRAFT) {
        curMode = -1;
        closeBtn.setVisible(false);
        craftBtn.setVisible(false);
      }
    } else if (e.getSource() == craftBtn) {
      CraftItem item = craftMenu.getCurItem();
      getCurrentPlayer().setResources(item.craft(getCurrentPlayer().getResources()));
      getCurrentPlayer().setTool(GameObject.toolTypes.getToolTypeOf(item.getType()), item.getType());
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

  private void drawStringC(Graphics2D g2, String string, Font font, int x, int y) {
    FontMetrics metrics = getFontMetrics(font);
    g2.setFont(font);
    g2.drawString(string, x - metrics.stringWidth(string)/2, y+(metrics.getHeight()-metrics.getDescent())/2);
  }

  private BBox drawStringWordWrap(Graphics2D g2, String string, Font font, int x, int y, int w) {
    // Draws the string, constraining it to a certain width, going to a new line when it is past that width
    // RETURNS the bounding box of the string
    FontMetrics metrics = getFontMetrics(font);
    g2.setFont(font);
    String[] words = string.split(" ");
    String curWord = words[0];
    
    int h = 0; // current height to be drawing at
    for (int i = 1; i < words.length; i++) {
      // Check that curword plus the new word is less than width
      if (metrics.stringWidth(curWord + " " + words[i]) < w) {
        // if so, add another word
        curWord += " " + words[i];
      } else {
        // if not, draw the string on the cur line, and go to a new line
        drawStringTL(g2, curWord, font, x, y+h);
        h += metrics.getHeight() - metrics.getDescent();
        curWord = words[i];
      }
    }
    drawStringTL(g2, curWord, font, x, y+h);

    // make h reflect actual height of drawn string
    h += metrics.getHeight() - metrics.getDescent();

    return new BBox(x, y, w, h);
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