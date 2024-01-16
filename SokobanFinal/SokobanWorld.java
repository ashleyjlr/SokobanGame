import java.awt.Color;
import java.util.ArrayList;

import javalib.funworld.World;
import javalib.funworld.WorldScene;
import tester.Tester;
import javalib.worldimages.*;

// represents a world in which Sokoban is played
class SokobanWorld extends World {
  // represents the state of the Sokoban board
  SokobanBoard boardState;

  SokobanWorld(SokobanBoard boardState) {
    this.boardState = boardState;
  }

  // renders this world's board into a scene
  public WorldScene makeScene() {
    return this.boardState.render();
  }

  // allows the player to move around based on a key input
  // by producing a new world based on their input
  // stops when player is not found (fell into black hole) OR when level is won
  public World onKeyEvent(String key) {
    if (this.boardState.shouldEnd()) {
      if (this.boardState.levelWon()) {
        return this.endOfWorld("Level Won");
      }
      else {
        return this.endOfWorld("Level Lost");
      }
    }
    else if (key.equals(">") || key.equals("d") || key.equals("right")) {
      return new SokobanWorld(this.boardState.playerMove("right"));
    }
    else if ((key.equals("<") || key.equals("a")) || key.equals("left")) {
      return new SokobanWorld(this.boardState.playerMove("left"));
    }
    else if ((key.equals("^") || key.equals("w")) || key.equals("up")) {
      return new SokobanWorld(this.boardState.playerMove("up"));
    }
    else if ((key.equals("v") || key.equals("s")) || key.equals("down")) {
      return new SokobanWorld(this.boardState.playerMove("down"));
    }
    else {
      return this;
    }
  }

  // overrides lastScene to return an appropriate image based on the message.
  // sends the message to lastScene within SokobanGame class to access the
  // appropriate size
  // to ensure no field of field access
  public WorldScene lastScene(String msg) {
    return this.boardState.lastScene(msg);
  }
}

// tests and examples for SokobanWorld
class ExamplesSokobanWorld {

  // tests and examples for makeScene in SokobanWorld
  boolean testMakeScene_SokobanWorld(Tester t) {
    WorldImage BLANK = new ComputedPixelImage(120, 120);
    WorldImage RTARGET = new FromFileImage("SokobanImages/RedTarget.png");
    WorldImage YTARGET = new FromFileImage("SokobanImages/YellowTarget.png");
    WorldImage BTARGET = new FromFileImage("SokobanImages/BlueTarget.png");
    WorldImage GTARGET = new FromFileImage("SokobanImages/GreenTarget.png");
    WorldImage PLAYER = new FromFileImage("SokobanImages/Player.png");
    WorldImage WALL = new FromFileImage("SokobanImages/Wall.png");
    WorldImage BOX = new FromFileImage("SokobanImages/Box.png");
    WorldImage RTROPHY = new FromFileImage("SokobanImages/RedTrophy.png");
    WorldImage YTROPHY = new FromFileImage("SokobanImages/YellowTrophy.png");
    WorldImage BTROPHY = new FromFileImage("SokobanImages/BlueTrophy.png");
    WorldImage GTROPHY = new FromFileImage("SokobanImages/GreenTrophy.png");
    WorldImage HOLE = new FromFileImage("SokobanImages/Hole.png");

    ArrayList<ICell> shortExLevelGround = new ArrayList<ICell>();
    shortExLevelGround.add(new Blank(new Posn(1, 1)));
    shortExLevelGround.add(new Blank(new Posn(2, 1)));
    shortExLevelGround.add(new Blank(new Posn(3, 1)));
    shortExLevelGround.add(new Blank(new Posn(4, 1)));
    shortExLevelGround.add(new Target(new Posn(5, 1), Color.red));
    shortExLevelGround.add(new Target(new Posn(1, 2), Color.yellow));
    shortExLevelGround.add(new Target(new Posn(2, 2), Color.blue));
    shortExLevelGround.add(new Target(new Posn(3, 2), Color.green));
    shortExLevelGround.add(new Blank(new Posn(4, 2)));
    shortExLevelGround.add(new Blank(new Posn(5, 2)));

    ArrayList<ICell> shortExLevelContents0 = new ArrayList<ICell>();
    shortExLevelContents0.add(new Player(new Posn(1, 1)));
    shortExLevelContents0.add(new Blank(new Posn(2, 1)));
    shortExLevelContents0.add(new Wall(new Posn(3, 1)));
    shortExLevelContents0.add(new Box(new Posn(4, 1)));
    shortExLevelContents0.add(new Trophy(new Posn(5, 1), Color.red));
    shortExLevelContents0.add(new Trophy(new Posn(1, 2), Color.yellow));
    shortExLevelContents0.add(new Trophy(new Posn(2, 2), Color.blue));
    shortExLevelContents0.add(new Trophy(new Posn(3, 2), Color.green));
    shortExLevelContents0.add(new Hole(new Posn(4, 2)));
    shortExLevelContents0.add(new Hole(new Posn(5, 2)));

    SokobanBoard shortExBoard = new SokobanBoard(new Posn(9, 1), shortExLevelGround,
        shortExLevelContents0);

    WorldScene result = new WorldScene(9 * 120, 1 * 120);
    result = result.placeImageXY(BLANK, 60, 60);
    result = result.placeImageXY(BLANK, 180, 60);
    result = result.placeImageXY(BLANK, 300, 60);
    result = result.placeImageXY(BLANK, 420, 60);
    result = result.placeImageXY(RTARGET, 540, 60);

    result = result.placeImageXY(YTARGET, 60, 180);
    result = result.placeImageXY(BTARGET, 180, 180);
    result = result.placeImageXY(GTARGET, 300, 180);
    result = result.placeImageXY(BLANK, 420, 180);
    result = result.placeImageXY(BLANK, 540, 180);

    result = result.placeImageXY(PLAYER, 60, 60);
    result = result.placeImageXY(BLANK, 180, 60);
    result = result.placeImageXY(WALL, 300, 60);
    result = result.placeImageXY(BOX, 420, 60);
    result = result.placeImageXY(RTROPHY, 540, 60);

    result = result.placeImageXY(YTROPHY, 60, 180);
    result = result.placeImageXY(BTROPHY, 180, 180);
    result = result.placeImageXY(GTROPHY, 300, 180);
    result = result.placeImageXY(HOLE, 420, 180);
    result = result.placeImageXY(HOLE, 540, 180);

    SokobanWorld shortExWorld = new SokobanWorld(shortExBoard);

    return t.checkExpect(shortExWorld.makeScene(), result);
  }

  // tests and examples for onKeyEvent in SokobanWorld
  boolean testOnKeyEvent_SokobanWorld(Tester t) {
    ArrayList<ICell> shortExLevelGround = new ArrayList<ICell>();
    shortExLevelGround.add(new Target(new Posn(1, 1), Color.red));
    shortExLevelGround.add(new Blank(new Posn(2, 1)));
    shortExLevelGround.add(new Blank(new Posn(1, 2)));
    shortExLevelGround.add(new Blank(new Posn(2, 2)));
    ArrayList<ICell> shortExLevelContents0 = new ArrayList<ICell>();
    shortExLevelContents0.add(new Player(new Posn(1, 1)));
    shortExLevelContents0.add(new Blank(new Posn(2, 1)));
    shortExLevelContents0.add(new Blank(new Posn(1, 2)));
    shortExLevelContents0.add(new Blank(new Posn(2, 2)));
    SokobanBoard shortExB0 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents0);
    SokobanWorld shortExW0 = new SokobanWorld(shortExB0);
    // player moves down
    ArrayList<ICell> shortExLevelContents1 = new ArrayList<ICell>();
    shortExLevelContents1.add(new Blank(new Posn(2, 1)));
    shortExLevelContents1.add(new Blank(new Posn(1, 2)));
    shortExLevelContents1.add(new Blank(new Posn(2, 2)));
    shortExLevelContents1.add(new Player(new Posn(1, 2)));
    shortExLevelContents1.add(new Blank(new Posn(1, 1)));
    SokobanBoard shortExB1 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents1);
    SokobanWorld shortExW1 = new SokobanWorld(shortExB1);
    // player moves right
    ArrayList<ICell> shortExLevelContents2 = new ArrayList<ICell>();
    shortExLevelContents2.add(new Blank(new Posn(2, 1)));
    shortExLevelContents2.add(new Blank(new Posn(1, 2)));
    shortExLevelContents2.add(new Blank(new Posn(2, 2)));
    shortExLevelContents2.add(new Blank(new Posn(1, 1)));
    shortExLevelContents2.add(new Player(new Posn(2, 2)));
    SokobanBoard shortExB2 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents2);
    SokobanWorld shortExW2 = new SokobanWorld(shortExB2);
    // player moves up
    ArrayList<ICell> shortExLevelContents3 = new ArrayList<ICell>();
    shortExLevelContents3.add(new Blank(new Posn(2, 1)));
    shortExLevelContents3.add(new Blank(new Posn(1, 2)));
    shortExLevelContents3.add(new Blank(new Posn(2, 2)));
    shortExLevelContents3.add(new Blank(new Posn(1, 1)));
    shortExLevelContents3.add(new Player(new Posn(2, 1)));
    SokobanBoard shortExB3 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents3);
    SokobanWorld shortExW3 = new SokobanWorld(shortExB3);
    // player moves left
    ArrayList<ICell> shortExLevelContents4 = new ArrayList<ICell>();
    shortExLevelContents4.add(new Blank(new Posn(2, 1)));
    shortExLevelContents4.add(new Blank(new Posn(1, 2)));
    shortExLevelContents4.add(new Blank(new Posn(2, 2)));
    shortExLevelContents4.add(new Blank(new Posn(1, 1)));
    shortExLevelContents4.add(new Player(new Posn(1, 1)));
    SokobanBoard shortExB4 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents4);
    SokobanWorld shortExW4 = new SokobanWorld(shortExB4);
    return t.checkExpect(shortExW0.onKeyEvent("down"), shortExW1)
        && t.checkExpect(shortExW1.onKeyEvent("right"), shortExW2)
        && t.checkExpect(shortExW2.onKeyEvent("up"), shortExW3)
        && t.checkExpect(shortExW3.onKeyEvent("left"), shortExW4)
        && t.checkExpect(shortExW0.onKeyEvent("pizza"), shortExW0);
  }

  boolean testLastScene(Tester t) {
    WorldImage BLANK = new ComputedPixelImage(120, 120);
    WorldImage RTARGET = new FromFileImage("SokobanImages/RedTarget.png");
    WorldImage YTARGET = new FromFileImage("SokobanImages/YellowTarget.png");
    WorldImage BTARGET = new FromFileImage("SokobanImages/BlueTarget.png");
    WorldImage GTARGET = new FromFileImage("SokobanImages/GreenTarget.png");
    WorldImage PLAYER = new FromFileImage("SokobanImages/Player.png");
    WorldImage WALL = new FromFileImage("SokobanImages/Wall.png");
    WorldImage BOX = new FromFileImage("SokobanImages/Box.png");
    WorldImage RTROPHY = new FromFileImage("SokobanImages/RedTrophy.png");
    WorldImage YTROPHY = new FromFileImage("SokobanImages/YellowTrophy.png");
    WorldImage BTROPHY = new FromFileImage("SokobanImages/BlueTrophy.png");
    WorldImage GTROPHY = new FromFileImage("SokobanImages/GreenTrophy.png");
    WorldImage HOLE = new FromFileImage("SokobanImages/Hole.png");

    ArrayList<ICell> shortExLevelGround = new ArrayList<ICell>();
    shortExLevelGround.add(new Blank(new Posn(1, 1)));
    shortExLevelGround.add(new Blank(new Posn(2, 1)));
    shortExLevelGround.add(new Blank(new Posn(3, 1)));
    shortExLevelGround.add(new Blank(new Posn(4, 1)));
    shortExLevelGround.add(new Target(new Posn(5, 1), Color.red));
    shortExLevelGround.add(new Target(new Posn(1, 2), Color.yellow));
    shortExLevelGround.add(new Target(new Posn(2, 2), Color.blue));
    shortExLevelGround.add(new Target(new Posn(3, 2), Color.green));
    shortExLevelGround.add(new Blank(new Posn(4, 2)));
    shortExLevelGround.add(new Blank(new Posn(5, 2)));

    ArrayList<ICell> shortExLevelContents0 = new ArrayList<ICell>();
    shortExLevelContents0.add(new Player(new Posn(1, 1)));
    shortExLevelContents0.add(new Blank(new Posn(2, 1)));
    shortExLevelContents0.add(new Wall(new Posn(3, 1)));
    shortExLevelContents0.add(new Box(new Posn(4, 1)));
    shortExLevelContents0.add(new Trophy(new Posn(5, 1), Color.red));
    shortExLevelContents0.add(new Trophy(new Posn(1, 2), Color.yellow));
    shortExLevelContents0.add(new Trophy(new Posn(2, 2), Color.blue));
    shortExLevelContents0.add(new Trophy(new Posn(3, 2), Color.green));
    shortExLevelContents0.add(new Hole(new Posn(4, 2)));
    shortExLevelContents0.add(new Hole(new Posn(5, 2)));

    SokobanBoard shortExBoard = new SokobanBoard(new Posn(9, 1), shortExLevelGround,
        shortExLevelContents0);

    WorldScene result = new WorldScene(9 * 120, 1 * 120);
    result = result.placeImageXY(BLANK, 60, 60);
    result = result.placeImageXY(BLANK, 180, 60);
    result = result.placeImageXY(BLANK, 300, 60);
    result = result.placeImageXY(BLANK, 420, 60);
    result = result.placeImageXY(RTARGET, 540, 60);

    result = result.placeImageXY(YTARGET, 60, 180);
    result = result.placeImageXY(BTARGET, 180, 180);
    result = result.placeImageXY(GTARGET, 300, 180);
    result = result.placeImageXY(BLANK, 420, 180);
    result = result.placeImageXY(BLANK, 540, 180);

    result = result.placeImageXY(PLAYER, 60, 60);
    result = result.placeImageXY(BLANK, 180, 60);
    result = result.placeImageXY(WALL, 300, 60);
    result = result.placeImageXY(BOX, 420, 60);
    result = result.placeImageXY(RTROPHY, 540, 60);

    result = result.placeImageXY(YTROPHY, 60, 180);
    result = result.placeImageXY(BTROPHY, 180, 180);
    result = result.placeImageXY(GTROPHY, 300, 180);
    result = result.placeImageXY(HOLE, 420, 180);
    result = result.placeImageXY(HOLE, 540, 180);

    SokobanWorld shortExWorld = new SokobanWorld(shortExBoard);

    WorldScene scene = new WorldScene(9 * 120, 1 * 120);

    TextImage levelWonImage = new TextImage("Level Won", 24, FontStyle.BOLD, Color.BLACK);
    WorldScene levelWon = scene.placeImageXY(levelWonImage, (9 * 120) / 2, (1 * 120) / 2);

    TextImage levelLostImage = new TextImage("Level Lost", 24, FontStyle.BOLD, Color.BLACK);
    WorldScene levelLost = scene.placeImageXY(levelLostImage, (9 * 120) / 2, (1 * 120) / 2);

    return t.checkExpect(shortExWorld.lastScene("Level Won"), levelWon)
        && t.checkExpect(shortExWorld.lastScene("Level Lost"), levelLost);

  }
}

// tests and examples for SokobanWorld (basic)
class ExamplesSokobanWorldBasic {
  boolean testsWorld(Tester t) {
    String givenExLevelGround = "________\n" + "___R____\n" + "________\n" + "_B____Y_\n"
        + "________\n" + "___G____\n" + "________";

    String givenExLevelContents = "__WWW___\n" + "__W_WW__\n" + "WW_r_WWW\n" + "W_b>y__W\n"
        + "WWHgWWWW\n" + "_WW_W___\n" + "__WWW___";

    SokobanBoard shortExB = new SokobanBoard(givenExLevelGround, givenExLevelContents);
    SokobanWorld shortExW = new SokobanWorld(shortExB);

    return shortExW.bigBang(shortExB.size.x * 120, shortExB.size.y * 120, 0.1);
  }
}

//tests and examples as the first level of sokobon
class ExamplesSokobanWorldLevelWon {
  boolean testsWorld(Tester t) {
    String givenExLevelGround = "________\n" + "________\n" + "_B______\n" + "_____G__\n"
        + "_R______\n" + "____Y___\n" + "______R_\n" + "____G___\n" + "________";

    String givenExLevelContents = "__WWWWW_\n" + "WWW___W_\n" + "W_<b__W_\n" + "WWW_g_W_\n"
        + "W_WWy_W_\n" + "W_W___WW\n" + "Wr_bgr_W\n" + "W______W\n" + "WWWWWWWW";

    SokobanBoard shortExB = new SokobanBoard(givenExLevelGround, givenExLevelContents);
    SokobanWorld shortExW = new SokobanWorld(shortExB);

    return shortExW.bigBang(shortExB.size.x * 120, shortExB.size.y * 120, 0.1);
  }
}

// tests and examples for specifically holes
class ExamplesSokobanWorldHoles {
  boolean testsWorld(Tester t) {
    String givenExLevelGround = "_______\n" + "_______\n" + "_______\n" + "__R____\n" + "_______\n"
        + "_______\n" + "_______";

    String givenExLevelContents = "WWWWWWW\n" + "W_>___W\n" + "W_H_r_W\n" + "WH_HB_W\n"
        + "W_H___W\n" + "W_____W\n" + "WWWWWWW";

    SokobanBoard shortExB = new SokobanBoard(givenExLevelGround, givenExLevelContents);
    SokobanWorld shortExW = new SokobanWorld(shortExB);

    return shortExW.bigBang(shortExB.size.x * 120, shortExB.size.y * 120, 0.1);
  }
}