import java.awt.Color;
import java.util.ArrayList;

import javalib.worldimages.Posn;
import tester.Tester;

// contains miscellaneous useful functions
class Utils {
  // produces a list of strings where each item in the list
  // is a single character (string) from the given string in the same order as in
  // the given string
  ArrayList<String> explode(String s) {
    ArrayList<String> exploded = new ArrayList<String>();
    // loop iterates over every character(string) in the given string
    // for every character in the string, the character is added to exploded
    for (int i = 0; i < s.length(); i += 1) {
      exploded.add(s.substring(i, i + 1));
    }
    return exploded;
  }

  // produces a list of ICell where the given cell is only added to the list if no
  // cells
  // in the list have the same coordinates
  ArrayList<ICell> noDupes(ArrayList<ICell> list, ICell cell) {
    // for every cell in list, if the cell has the same posn as the given cell, then
    // the
    // given list is returned, else the given cell is added to the
    // list and that new list is returned
    for (ICell c : list) {
      if (new Utils().samePosn(cell.accept(new CellPosnVisitor()),
          c.accept(new CellPosnVisitor()))) {
        return list;
      }
    }
    list.add(cell);
    return list;
  }

  // produces a list of cells based on the given string
  ArrayList<ICell> toLevelCells(String given, boolean isGround) {
    ArrayList<String> explodedGiven = new Utils().explode(given);
    ArrayList<ICell> result = new ArrayList<ICell>();
    int column = 1;
    int row = 1;
    // loop iterates over every single character string in the exploded list
    // and if it is the end character "\n" increases the row by one but otherwise
    // adds a new cell to the result with the column, row, and isGround
    for (int c = 0; c < explodedGiven.size(); c += 1) {
      if (explodedGiven.get(c).equals("\n")) {
        column = 1;
        row += 1;
      }
      else {
        result.add(new Utils().makeCell(explodedGiven.get(c), column, row, isGround));
        column += 1;
      }
    }
    return result;
  }

  // produces a posn representing the size of the Sokoban board based on the given
  // string
  Posn findSize(String given) {
    ArrayList<String> explodedGiven = new Utils().explode(given);
    int column = 1;
    int row = 1;
    // loop iterates over every single character string in the exploded list
    // and if it is the end character "\n" increases the row by one but otherwise
    // adds one to the column
    for (int c = 0; c < explodedGiven.size(); c += 1) {
      if (explodedGiven.get(c).equals("\n")) {
        column = 0;
        row += 1;
      }
      else {
        column += 1;
      }
    }
    return new Posn(column, row);
  }

  // produces a cell based on the given name as the location (column, row)
  ICell makeCell(String given, int column, int row, boolean isGround) {
    Posn coord = new Posn(column, row);
    if (given.equals("_")) {
      return new Blank(coord);
    }
    else if (given.equals("Y")) {
      return new Target(coord, Color.yellow);
    }
    else if (given.equals("G")) {
      return new Target(coord, Color.green);
    }
    else if (given.equals("B") && isGround) {
      return new Target(coord, Color.blue);
    }
    else if (given.equals("R")) {
      return new Target(coord, Color.red);
    }
    else if (given.equals("y")) {
      return new Trophy(coord, Color.yellow);
    }
    else if (given.equals("g")) {
      return new Trophy(coord, Color.green);
    }
    else if (given.equals("b")) {
      return new Trophy(coord, Color.blue);
    }
    else if (given.equals("r")) {
      return new Trophy(coord, Color.red);
    }
    else if (given.equals("W")) {
      return new Wall(coord);
    }
    else if (given.equals("B") && !isGround) {
      return new Box(coord);
    }
    else if (given.equals(">") || given.equals("<") || given.equals("^") || given.equals("v")) {
      return new Player(coord);
    }
    else if (given.equals("H")) {
      return new Hole(coord);
    }
    else {
      throw new IllegalArgumentException("Invalid character given to build the level");
    }
  }

  // determines if two posn have the same x and y values
  boolean samePosn(Posn p1, Posn p2) {
    return (p1.x == p2.x) && (p1.y == p2.y);
  }

  // produces the index at which the Player ICell exists in the list
  // returns negative 1 if unable to find the player
  int findPlayerIndex(ArrayList<ICell> list) {
    for (int i = 0; i < list.size(); i += 1) {
      ICell cell = list.get(i);
      if ((cell.findPlayer().x > 0) && (cell.findPlayer().y > 0)) {
        return i;
      }
    }
    return -1;
  }

  // produces the ICell at the given location (x, y) from the list
  ICell findCell(ArrayList<ICell> list, int x, int y) {
    for (int i = 0; i < list.size(); i += 1) {
      ICell cell = list.get(i);
      if (((cell.accept(new CellPosnVisitor()).x == x)
          && (cell.accept(new CellPosnVisitor()).y == y))) {
        return cell;
      }
    }
    throw new RuntimeException("Cell not found");
  }

  // produces the next ICell from the given location in the given direction based
  // on the given list
  // and takes into account the size of the board
  ICell findNext(ArrayList<ICell> list, String direction, Posn start, Posn size) {
    if ((direction.equals("right")) && ((start.x + 1) <= size.x)) {
      return this.findCell(list, start.x + 1, start.y);
    }
    else if ((direction.equals("left")) && ((start.x - 1) > 0)) {
      return this.findCell(list, start.x - 1, start.y);
    }
    else if ((direction.equals("up")) && ((start.y - 1) > 0)) {
      return this.findCell(list, start.x, start.y - 1);

    }
    else if ((direction.equals("down")) && ((start.y + 1) <= size.y)) {
      return this.findCell(list, start.x, start.y + 1);
    }
    else {
      throw new RuntimeException("Out of board");
    }
  }
}

// tests and examples for Utils
class ExamplesUtils {

  // tests and examples for explode in Utils
  boolean testExplode_Utils(Tester t) {

    String pizza = "pizza";
    ArrayList<String> pizzaList = new ArrayList<String>();
    pizzaList.add("p");
    pizzaList.add("i");
    pizzaList.add("z");
    pizzaList.add("z");
    pizzaList.add("a");

    String lol = "lol";
    ArrayList<String> lolList = new ArrayList<String>();
    lolList.add("l");
    lolList.add("o");
    lolList.add("l");

    return t.checkExpect(new Utils().explode(pizza), pizzaList)
        && t.checkExpect(new Utils().explode(lol), lolList)
        && t.checkExpect(new Utils().explode(""), new ArrayList<String>());
  }

  // tests and examples for noDupes in Utils
  boolean testNoDupes_Utils(Tester t) {
    ArrayList<ICell> shortExLevelGround0 = new ArrayList<ICell>();
    shortExLevelGround0.add(new Blank(new Posn(1, 1)));
    shortExLevelGround0.add(new Blank(new Posn(2, 1)));
    shortExLevelGround0.add(new Blank(new Posn(1, 2)));

    ArrayList<ICell> shortExLevelGround1 = new ArrayList<ICell>();
    shortExLevelGround1.add(new Blank(new Posn(1, 1)));
    shortExLevelGround1.add(new Blank(new Posn(2, 1)));
    shortExLevelGround1.add(new Blank(new Posn(1, 2)));
    shortExLevelGround1.add(new Blank(new Posn(2, 2)));

    return t.checkExpect(new Utils().noDupes(shortExLevelGround0, new Blank(new Posn(1, 1))),
        shortExLevelGround0)
        && t.checkExpect(new Utils().noDupes(shortExLevelGround0, new Blank(new Posn(2, 2))),
            shortExLevelGround1);
  }

  // tests and examples for makeCell in Utils
  boolean testMakeCell_Utils(Tester t) {
    Posn ex = new Posn(0, 0);
    ICell blank = new Blank(ex);
    ICell yTarget = new Target(ex, Color.yellow);
    ICell gTarget = new Target(ex, Color.green);
    ICell bTarget = new Target(ex, Color.blue);
    ICell rTarget = new Target(ex, Color.red);
    ICell yTrophy = new Trophy(ex, Color.yellow);
    ICell gTrophy = new Trophy(ex, Color.green);
    ICell bTrophy = new Trophy(ex, Color.blue);
    ICell rTrophy = new Trophy(ex, Color.red);
    ICell wall = new Wall(ex);
    ICell box = new Box(ex);
    ICell player = new Player(ex);
    ICell hole = new Hole(ex);

    return t.checkExpect(new Utils().makeCell("_", 0, 0, false), blank)
        && t.checkExpect(new Utils().makeCell("_", 0, 0, true), blank)
        && t.checkExpect(new Utils().makeCell("Y", 0, 0, true), yTarget)
        && t.checkExpect(new Utils().makeCell("G", 0, 0, true), gTarget)
        && t.checkExpect(new Utils().makeCell("B", 0, 0, true), bTarget)
        && t.checkExpect(new Utils().makeCell("R", 0, 0, false), rTarget)
        && t.checkExpect(new Utils().makeCell("y", 0, 0, false), yTrophy)
        && t.checkExpect(new Utils().makeCell("g", 0, 0, false), gTrophy)
        && t.checkExpect(new Utils().makeCell("b", 0, 0, false), bTrophy)
        && t.checkExpect(new Utils().makeCell("r", 0, 0, false), rTrophy)
        && t.checkExpect(new Utils().makeCell("W", 0, 0, false), wall)
        && t.checkExpect(new Utils().makeCell("B", 0, 0, false), box)
        && t.checkExpect(new Utils().makeCell(">", 0, 0, false), player)
        && t.checkExpect(new Utils().makeCell("<", 0, 0, false), player)
        && t.checkExpect(new Utils().makeCell("^", 0, 0, false), player)
        && t.checkExpect(new Utils().makeCell("v", 0, 0, false), player)
        && t.checkExpect(new Utils().makeCell("H", 0, 0, false), hole)
        && t.checkException(
            new IllegalArgumentException("Invalid character given to build the level"), new Utils(),
            "makeCell", "Z", 0, 0, false);
  }

  // tests and examples for toLevelCells in Utils
  boolean testToLevelCells_Utils(Tester t) {

    String givenExLevelGround = "________\n" + "___R____\n" + "________\n" + "_B____Y_\n"
        + "________\n" + "___G____\n" + "________";

    ArrayList<ICell> givenExLevelGroundList = new ArrayList<ICell>();
    givenExLevelGroundList.add(new Blank(new Posn(1, 1)));
    givenExLevelGroundList.add(new Blank(new Posn(2, 1)));
    givenExLevelGroundList.add(new Blank(new Posn(3, 1)));
    givenExLevelGroundList.add(new Blank(new Posn(4, 1)));
    givenExLevelGroundList.add(new Blank(new Posn(5, 1)));
    givenExLevelGroundList.add(new Blank(new Posn(6, 1)));
    givenExLevelGroundList.add(new Blank(new Posn(7, 1)));
    givenExLevelGroundList.add(new Blank(new Posn(8, 1)));
    givenExLevelGroundList.add(new Blank(new Posn(1, 2)));
    givenExLevelGroundList.add(new Blank(new Posn(2, 2)));
    givenExLevelGroundList.add(new Blank(new Posn(3, 2)));
    givenExLevelGroundList.add(new Target(new Posn(4, 2), Color.red));
    givenExLevelGroundList.add(new Blank(new Posn(5, 2)));
    givenExLevelGroundList.add(new Blank(new Posn(6, 2)));
    givenExLevelGroundList.add(new Blank(new Posn(7, 2)));
    givenExLevelGroundList.add(new Blank(new Posn(8, 2)));
    givenExLevelGroundList.add(new Blank(new Posn(1, 3)));
    givenExLevelGroundList.add(new Blank(new Posn(2, 3)));
    givenExLevelGroundList.add(new Blank(new Posn(3, 3)));
    givenExLevelGroundList.add(new Blank(new Posn(4, 3)));
    givenExLevelGroundList.add(new Blank(new Posn(5, 3)));
    givenExLevelGroundList.add(new Blank(new Posn(6, 3)));
    givenExLevelGroundList.add(new Blank(new Posn(7, 3)));
    givenExLevelGroundList.add(new Blank(new Posn(8, 3)));
    givenExLevelGroundList.add(new Blank(new Posn(1, 4)));
    givenExLevelGroundList.add(new Target(new Posn(2, 4), Color.blue));
    givenExLevelGroundList.add(new Blank(new Posn(3, 4)));
    givenExLevelGroundList.add(new Blank(new Posn(4, 4)));
    givenExLevelGroundList.add(new Blank(new Posn(5, 4)));
    givenExLevelGroundList.add(new Blank(new Posn(6, 4)));
    givenExLevelGroundList.add(new Target(new Posn(7, 4), Color.yellow));
    givenExLevelGroundList.add(new Blank(new Posn(8, 4)));
    givenExLevelGroundList.add(new Blank(new Posn(1, 5)));
    givenExLevelGroundList.add(new Blank(new Posn(2, 5)));
    givenExLevelGroundList.add(new Blank(new Posn(3, 5)));
    givenExLevelGroundList.add(new Blank(new Posn(4, 5)));
    givenExLevelGroundList.add(new Blank(new Posn(5, 5)));
    givenExLevelGroundList.add(new Blank(new Posn(6, 5)));
    givenExLevelGroundList.add(new Blank(new Posn(7, 5)));
    givenExLevelGroundList.add(new Blank(new Posn(8, 5)));
    givenExLevelGroundList.add(new Blank(new Posn(1, 6)));
    givenExLevelGroundList.add(new Blank(new Posn(2, 6)));
    givenExLevelGroundList.add(new Blank(new Posn(3, 6)));
    givenExLevelGroundList.add(new Target(new Posn(4, 6), Color.green));
    givenExLevelGroundList.add(new Blank(new Posn(5, 6)));
    givenExLevelGroundList.add(new Blank(new Posn(6, 6)));
    givenExLevelGroundList.add(new Blank(new Posn(7, 6)));
    givenExLevelGroundList.add(new Blank(new Posn(8, 6)));
    givenExLevelGroundList.add(new Blank(new Posn(1, 7)));
    givenExLevelGroundList.add(new Blank(new Posn(2, 7)));
    givenExLevelGroundList.add(new Blank(new Posn(3, 7)));
    givenExLevelGroundList.add(new Blank(new Posn(4, 7)));
    givenExLevelGroundList.add(new Blank(new Posn(5, 7)));
    givenExLevelGroundList.add(new Blank(new Posn(6, 7)));
    givenExLevelGroundList.add(new Blank(new Posn(7, 7)));
    givenExLevelGroundList.add(new Blank(new Posn(8, 7)));

    String givenExLevelContents = "__WWW___\n" + "__W_WW__\n" + "WWWr_WWW\n" + "W_b>yB_W\n"
        + "WW_gWWWW\n" + "_WW_W___\n" + "__WWW___";

    ArrayList<ICell> givenExLevelContentsList = new ArrayList<ICell>();
    givenExLevelContentsList.add(new Blank(new Posn(1, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(4, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(4, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 3)));
    givenExLevelContentsList.add(new Trophy(new Posn(4, 3), Color.red));
    givenExLevelContentsList.add(new Blank(new Posn(5, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(7, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 4)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 4)));
    givenExLevelContentsList.add(new Trophy(new Posn(3, 4), Color.blue));
    givenExLevelContentsList.add(new Player(new Posn(4, 4)));
    givenExLevelContentsList.add(new Trophy(new Posn(5, 4), Color.yellow));
    givenExLevelContentsList.add(new Box(new Posn(6, 4)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 4)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 4)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 5)));
    givenExLevelContentsList.add(new Blank(new Posn(3, 5)));
    givenExLevelContentsList.add(new Trophy(new Posn(4, 5), Color.green));
    givenExLevelContentsList.add(new Wall(new Posn(5, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(7, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 5)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(4, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(4, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 7)));

    return t.checkExpect(new Utils().toLevelCells(givenExLevelGround, true), givenExLevelGroundList)
        && t.checkExpect(new Utils().toLevelCells(givenExLevelContents, false),
            givenExLevelContentsList);
  }

  // tests and examples for findSize in Utils
  boolean testFindSize_Utils(Tester t) {
    // size is 8 x 7
    String givenExLevelGround = "________\n" + "___R____\n" + "________\n" + "_B____Y_\n"
        + "________\n" + "___G____\n" + "________";

    String shortExLevelContents = "_WB_\n" + "bryg\n" + "WBWB";

    return t.checkExpect(new Utils().findSize(givenExLevelGround), new Posn(8, 7))
        && t.checkExpect(new Utils().findSize(shortExLevelContents), new Posn(4, 3));
  }

  // tests and examples for samePosn in Utils
  boolean testSamePosn_Utils(Tester t) {
    Posn p1 = new Posn(1, 1);
    Posn p2 = new Posn(1, 1);
    Posn p3 = new Posn(1, 2);
    Posn p4 = new Posn(2, 1);

    return t.checkExpect(new Utils().samePosn(p1, p1), true)
        && t.checkExpect(new Utils().samePosn(p1, p2), true)
        && t.checkExpect(new Utils().samePosn(p1, p3), false)
        && t.checkExpect(new Utils().samePosn(p1, p4), false);
  }

  // tests and examples for findCell in Utils
  boolean testFindCell_Utils(Tester t) {
    ArrayList<ICell> givenExLevelContentsList = new ArrayList<ICell>();
    givenExLevelContentsList.add(new Blank(new Posn(1, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(4, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(4, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 3)));
    givenExLevelContentsList.add(new Trophy(new Posn(4, 3), Color.red));
    givenExLevelContentsList.add(new Blank(new Posn(5, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(7, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 4)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 4)));
    givenExLevelContentsList.add(new Trophy(new Posn(3, 4), Color.blue));
    givenExLevelContentsList.add(new Player(new Posn(4, 4)));
    givenExLevelContentsList.add(new Trophy(new Posn(5, 4), Color.yellow));
    givenExLevelContentsList.add(new Box(new Posn(6, 4)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 4)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 4)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 5)));
    givenExLevelContentsList.add(new Blank(new Posn(3, 5)));
    givenExLevelContentsList.add(new Trophy(new Posn(4, 5), Color.green));
    givenExLevelContentsList.add(new Wall(new Posn(5, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(7, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 5)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(4, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(4, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 7)));

    return t.checkExpect(new Utils().findCell(givenExLevelContentsList, 8, 7),
        new Blank(new Posn(8, 7)))
        && t.checkExpect(new Utils().findCell(givenExLevelContentsList, 4, 5),
            new Trophy(new Posn(4, 5), Color.green))
        && t.checkExpect(new Utils().findCell(givenExLevelContentsList, 2, 6),
            new Wall(new Posn(2, 6)))
        && t.checkException(new RuntimeException("Cell not found"), new Utils(), "findCell",
            givenExLevelContentsList, 10, 5);
  }

  // tests and examples for findPlayerIndex in Utils
  boolean testFindPlayerIndex_Utils(Tester t) {
    ArrayList<ICell> short1ExLevelContentsList = new ArrayList<ICell>();
    short1ExLevelContentsList.add(new Blank(new Posn(1, 1)));
    short1ExLevelContentsList.add(new Blank(new Posn(2, 4)));
    short1ExLevelContentsList.add(new Trophy(new Posn(3, 4), Color.blue));
    short1ExLevelContentsList.add(new Player(new Posn(4, 4)));
    short1ExLevelContentsList.add(new Trophy(new Posn(5, 4), Color.yellow));
    short1ExLevelContentsList.add(new Box(new Posn(6, 4)));
    short1ExLevelContentsList.add(new Blank(new Posn(7, 4)));
    short1ExLevelContentsList.add(new Wall(new Posn(8, 4)));

    ArrayList<ICell> short2ExLevelContentsList = new ArrayList<ICell>();
    short2ExLevelContentsList.add(new Blank(new Posn(1, 1)));
    short2ExLevelContentsList.add(new Blank(new Posn(2, 1)));
    short2ExLevelContentsList.add(new Wall(new Posn(5, 1)));
    short2ExLevelContentsList.add(new Blank(new Posn(2, 4)));
    short2ExLevelContentsList.add(new Trophy(new Posn(3, 4), Color.blue));
    short2ExLevelContentsList.add(new Trophy(new Posn(5, 4), Color.yellow));
    short2ExLevelContentsList.add(new Box(new Posn(6, 4)));
    short2ExLevelContentsList.add(new Blank(new Posn(7, 4)));
    short2ExLevelContentsList.add(new Wall(new Posn(8, 4)));
    short2ExLevelContentsList.add(new Player(new Posn(4, 4)));

    ArrayList<ICell> emptyList = new ArrayList<ICell>();

    return t.checkExpect(new Utils().findPlayerIndex(short1ExLevelContentsList), 3)
        && t.checkExpect(new Utils().findPlayerIndex(short2ExLevelContentsList), 9)
        && t.checkExpect(new Utils().findPlayerIndex(emptyList), -1);
  }

  // tests and examples for findNext in Utils
  boolean testFindNext_Utils(Tester t) {
    ArrayList<ICell> givenExLevelContentsList = new ArrayList<ICell>();
    givenExLevelContentsList.add(new Blank(new Posn(1, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(4, 1)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 1)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(4, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 2)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 2)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 3)));
    givenExLevelContentsList.add(new Trophy(new Posn(4, 3), Color.red));
    givenExLevelContentsList.add(new Blank(new Posn(5, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(7, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 3)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 4)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 4)));
    givenExLevelContentsList.add(new Trophy(new Posn(3, 4), Color.blue));
    givenExLevelContentsList.add(new Player(new Posn(4, 4)));
    givenExLevelContentsList.add(new Trophy(new Posn(5, 4), Color.yellow));
    givenExLevelContentsList.add(new Box(new Posn(6, 4)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 4)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 4)));
    givenExLevelContentsList.add(new Wall(new Posn(1, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 5)));
    givenExLevelContentsList.add(new Blank(new Posn(3, 5)));
    givenExLevelContentsList.add(new Trophy(new Posn(4, 5), Color.green));
    givenExLevelContentsList.add(new Wall(new Posn(5, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(6, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(7, 5)));
    givenExLevelContentsList.add(new Wall(new Posn(8, 5)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(2, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(4, 6)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 6)));
    givenExLevelContentsList.add(new Blank(new Posn(1, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(2, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(3, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(4, 7)));
    givenExLevelContentsList.add(new Wall(new Posn(5, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(6, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(7, 7)));
    givenExLevelContentsList.add(new Blank(new Posn(8, 7)));

    return t.checkExpect(
        new Utils().findNext(givenExLevelContentsList, "right", new Posn(1, 1), new Posn(8, 7)),
        new Blank(new Posn(2, 1)))
        && t.checkExpect(
            new Utils().findNext(givenExLevelContentsList, "left", new Posn(4, 4), new Posn(8, 7)),
            new Trophy(new Posn(3, 4), Color.blue))
        && t.checkExpect(
            new Utils().findNext(givenExLevelContentsList, "up", new Posn(5, 6), new Posn(8, 7)),
            new Wall(new Posn(5, 5)))
        && t.checkExpect(
            new Utils().findNext(givenExLevelContentsList, "down", new Posn(7, 6), new Posn(8, 7)),
            new Blank(new Posn(7, 7)))
        && t.checkException(new RuntimeException("Out of board"), new Utils(), "findNext",
            givenExLevelContentsList, "left", new Posn(1, 1), new Posn(8, 7));
  }
}