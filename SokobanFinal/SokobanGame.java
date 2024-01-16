
import java.awt.Color;
import java.util.ArrayList;

import javalib.funworld.WorldScene;
import javalib.worldimages.ComputedPixelImage;
import javalib.worldimages.FontStyle;
import javalib.worldimages.FromFileImage;
import javalib.worldimages.Posn;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;
import tester.Tester;

// represents the state of the Sokoban game's board
class SokobanBoard {
  // represents the width and height of this board
  Posn size;
  // represents the ground cells of this board
  ArrayList<ICell> levelGroundCells = new ArrayList<ICell>();
  // represents the content cells of this board
  ArrayList<ICell> levelContentsCells = new ArrayList<ICell>();

  SokobanBoard(Posn size, ArrayList<ICell> levelGroundCells, ArrayList<ICell> levelContentsCells) {
    this.size = size;
    this.levelGroundCells = levelGroundCells;
    this.levelContentsCells = levelContentsCells;
  }

  // constructor to create a board based on two strings
  SokobanBoard(String levelGround, String levelContents) {
    if (!(new Utils().samePosn(new Utils().findSize(levelContents),
        (new Utils().findSize(levelGround))))) {
      throw new IllegalArgumentException(
          "Dimensions of given level ground do not match dimensions of given level contents");
    }
    this.size = new Utils().findSize(levelContents);
    this.levelGroundCells.addAll(new Utils().toLevelCells(levelGround, true));
    this.levelContentsCells.addAll(new Utils().toLevelCells(levelContents, false));
  }

  // renders this Sokoban board into an image
  WorldScene render() {
    WorldScene result = new WorldScene(this.size.x * 120, this.size.y * 120);
    ArrayList<ICell> fullBoard = new ArrayList<ICell>();
    fullBoard.addAll(this.levelGroundCells);
    fullBoard.addAll(this.levelContentsCells);
    // for every cell in the list,
    // places the image at the given coordinates in the resulting world
    for (ICell cell : fullBoard) {
      int x = cell.accept(new CellPosnVisitor()).x;
      int y = cell.accept(new CellPosnVisitor()).y;
      result = result.placeImageXY(cell.drawICell(), (x * 120) - 60, (y * 120) - 60);
    }
    return result;
  }

  // produces a new board based on this board with the player moved in the given
  // direction
  // if the player is able to move there
  SokobanBoard playerMove(String direction) {
    ICell player = this.levelContentsCells
        .get(new Utils().findPlayerIndex(this.levelContentsCells));
    ICell next = new Utils().findNext(this.levelContentsCells, direction,
        player.accept(new CellPosnVisitor()), this.size);
    ICell newPlayer = next
        .accept(new MovePlayerVisitor(player, direction, this.levelContentsCells, this.size));
    ICell fillPlace = new Blank(player.accept(new CellPosnVisitor()));
    this.levelContentsCells.remove(new Utils().findPlayerIndex(this.levelContentsCells));
    this.levelContentsCells.add(newPlayer);
    this.levelContentsCells = new Utils().noDupes(this.levelContentsCells, fillPlace);
    return new SokobanBoard(this.size, this.levelGroundCells, this.levelContentsCells);
  }

  // determines if this board has been won
  // (every target has a trophy on top with the correct color)
  boolean levelWon() {
    boolean result = true;
    ICell contentCell;
    // for every cell in the list, determines if the cell is a good pair (winnable
    // pair) with the
    // cell at the same location in this board's list of contents
    for (ICell cell : this.levelGroundCells) {
      contentCell = new Utils().findCell(this.levelContentsCells,
          cell.accept(new CellPosnVisitor()).x, cell.accept(new CellPosnVisitor()).y);
      result = result && cell.accept(new GoodPairVisitor(contentCell));
    }
    return result;
  }

  // checks if the level should end
  // under the conditions that level is won or that no player is found
  boolean shouldEnd() {
    boolean levelWon = this.levelWon();
    boolean noPlayer = new Utils().findPlayerIndex(this.levelContentsCells) == -1;
    return levelWon || noPlayer;
  }

  // displays the appropriate ending screening based on the given string
  // returns an image of level won if all trophies are on the targets
  // returns an image of level lost if there is no player found
  // is called within lastScene in SokobanWorld class so it is displayed properly
  public WorldScene lastScene(String msg) {
    WorldScene scene = new WorldScene(this.size.x * 120, this.size.y * 120);

    TextImage levelWonImage = new TextImage("Level Won", 24, FontStyle.BOLD, Color.BLACK);
    WorldScene levelWon = scene.placeImageXY(levelWonImage, (this.size.x * 120) / 2,
        (this.size.y * 120) / 2);

    TextImage levelLostImage = new TextImage("Level Lost", 24, FontStyle.BOLD, Color.BLACK);
    WorldScene levelLost = scene.placeImageXY(levelLostImage, (this.size.x * 120) / 2,
        (this.size.y * 120) / 2);

    if (msg.equals("Level Won")) {
      return levelWon;
    }
    else {
      return levelLost;
    }
  }
}

// represents a cell in the board
interface ICell {

  // produces an image of this cell
  WorldImage drawICell();

  // to return the result of applying the given visitor to this Cell
  <T> T accept(ICellVisitor<T> visitor);

  // helps determine the location of the player in the board
  Posn findPlayer();

  // produces a new cell moved to the new coordinates
  // based on the given direction and this cell
  ICell move(String direction);

  // determines if this cell is a good pair with the given color
  // (good pair is only a trophy with the corresponding color)
  boolean goodPair(Color color);

  // used to find the location of an ICell, only used within testing
  Posn findCoords();
}

// represents any cell in the board
abstract class AICell implements ICell {
  // represents the location of this Cell
  Posn coord;
  // represents whether this cell is a ground cell
  boolean isGround;

  AICell(Posn coord, boolean isGround) {
    this.coord = coord;
    this.isGround = isGround;
  }

  // produces an image of this cell
  abstract public WorldImage drawICell();

  // to return the result of applying the given visitor to this Cell
  abstract public <T> T accept(ICellVisitor<T> visitor);

  // helps determine the location of the player in the board
  public Posn findPlayer() {
    return new Posn(-1, -1);
  }

  // produces a new cell moved to the new coordinates
  // based on the given direction and this cell
  public ICell move(String direction) {
    return this;
  }

  // determines if this cell is a good pair with the given color
  // (good pair is only a trophy with the corresponding color)
  public boolean goodPair(Color color) {
    return false;
  }

  // used to find the location of an ICell, only used within testing
  public Posn findCoords() {
    return this.coord;
  }
}

// represents a blank cell in the board
class Blank extends AICell {

  Blank(Posn coord) {
    super(coord, true);
  }

  // produces an image of this Blank
  public WorldImage drawICell() {
    return new ComputedPixelImage(120, 120);
  }

  // to return the result of applying the given visitor to this Blank
  public <T> T accept(ICellVisitor<T> visitor) {
    return visitor.visitBlank(this);
  }

}

//represents a wall cell in the board
class Wall extends AICell {

  Wall(Posn coord) {
    super(coord, false);
  }

  // produces an image of this Wall
  public WorldImage drawICell() {
    return new FromFileImage("SokobanImages/Wall.png");
  }

  // to return the result of applying the given visitor to this Wall
  public <T> T accept(ICellVisitor<T> visitor) {
    return visitor.visitWall(this);
  }

}

// represents a box cell in the board
class Box extends AICell {

  Box(Posn coord) {
    super(coord, false);
  }

  // produces an image of this Box
  public WorldImage drawICell() {
    return new FromFileImage("SokobanImages/Box.png");
  }

  // to return the result of applying the given visitor to this Box
  public <T> T accept(ICellVisitor<T> visitor) {
    return visitor.visitBox(this);
  }

  // produces a new box moved to the new coordinates
  // based on the given direction and this box cell
  public ICell move(String direction) {
    if (direction.equals("right")) {
      return new Box(new Posn(this.coord.x + 1, this.coord.y));
    }
    else if (direction.equals("left")) {
      return new Box(new Posn(this.coord.x - 1, this.coord.y));
    }
    else if (direction.equals("up")) {
      return new Box(new Posn(this.coord.x, this.coord.y - 1));

    }
    else if (direction.equals("down")) {
      return new Box(new Posn(this.coord.x, this.coord.y + 1));
    }
    else {
      return this;
    }
  }

}

//represents a player cell in the board
class Player extends AICell {

  Player(Posn coord) {
    super(coord, false);
  }

  // produces an image of this Player
  public WorldImage drawICell() {
    return new FromFileImage("SokobanImages/Player.png");
  }

  // to return the result of applying the given visitor to this Player
  public <T> T accept(ICellVisitor<T> visitor) {
    return visitor.visitPlayer(this);
  }

  // to determines the location of this player
  public Posn findPlayer() {
    return this.coord;
  }

  // produces a new player moved to the new coordinates
  // based on the given direction and this player cell
  public ICell move(String direction) {
    if (direction.equals("right")) {
      return new Player(new Posn(this.coord.x + 1, this.coord.y));
    }
    else if (direction.equals("left")) {
      return new Player(new Posn(this.coord.x - 1, this.coord.y));
    }
    else if (direction.equals("up")) {
      return new Player(new Posn(this.coord.x, this.coord.y - 1));

    }
    else if (direction.equals("down")) {
      return new Player(new Posn(this.coord.x, this.coord.y + 1));
    }
    else {
      return this;
    }
  }

}

//represents a target cell in the board
class Target extends AICell {
  // represents this target's color
  Color color;

  Target(Posn coord, Color color) {
    super(coord, true);
    this.color = color;
  }

  // produces an image of this Target based on the color
  public WorldImage drawICell() {
    if (this.color.equals(Color.yellow)) {
      return new FromFileImage("SokobanImages/YellowTarget.png");
    }
    else if (this.color.equals(Color.green)) {
      return new FromFileImage("SokobanImages/GreenTarget.png");
    }
    else if (this.color.equals(Color.blue)) {
      return new FromFileImage("SokobanImages/BlueTarget.png");
    }
    else {
      return new FromFileImage("SokobanImages/RedTarget.png");
    }
  }

  // to return the result of applying the given visitor to this Target
  public <T> T accept(ICellVisitor<T> visitor) {
    return visitor.visitTarget(this);
  }
}

//represents a trophy cell in the board
class Trophy extends AICell {
  // represents this trophy's color
  Color color;

  Trophy(Posn coord, Color color) {
    super(coord, true);
    this.color = color;
  }

  // produces an image of this Trophy based on the color
  public WorldImage drawICell() {
    if (this.color.equals(Color.yellow)) {
      return new FromFileImage("SokobanImages/YellowTrophy.png");
    }
    else if (this.color.equals(Color.green)) {
      return new FromFileImage("SokobanImages/GreenTrophy.png");
    }
    else if (this.color.equals(Color.blue)) {
      return new FromFileImage("SokobanImages/BlueTrophy.png");
    }
    else {
      return new FromFileImage("SokobanImages/RedTrophy.png");
    }
  }

  // to return the result of applying the given visitor to this Trophy
  public <T> T accept(ICellVisitor<T> visitor) {
    return visitor.visitTrophy(this);
  }

  // produces a new Trophy moved to the new coordinates
  // based on the given direction and this Trophy cell
  public ICell move(String direction) {
    if (direction.equals("right")) {
      return new Trophy(new Posn(this.coord.x + 1, this.coord.y), this.color);
    }
    else if (direction.equals("left")) {
      return new Trophy(new Posn(this.coord.x - 1, this.coord.y), this.color);
    }
    else if (direction.equals("up")) {
      return new Trophy(new Posn(this.coord.x, this.coord.y - 1), this.color);

    }
    else if (direction.equals("down")) {
      return new Trophy(new Posn(this.coord.x, this.coord.y + 1), this.color);
    }
    else {
      return this;
    }
  }

  // determines if this trophy cell is a good pair with the given color
  // (good pair is only a trophy with the corresponding color)
  public boolean goodPair(Color color) {
    return this.color.equals(color);
  }

}

// represents a hole cell in the board
class Hole extends AICell {

  Hole(Posn coord) {
    super(coord, false);
  }

  // produces an image of this Hole
  public WorldImage drawICell() {
    return new FromFileImage("SokobanImages/Hole.png");
  }

  // to return the result of applying the given visitor to this Hole
  public <T> T accept(ICellVisitor<T> visitor) {
    return visitor.visitHole(this);
  }
}

// to represent a visitor that visit (implements a function over ICell objects)
// an ICell and produces a result of type T
interface ICellVisitor<T> {

  // to represent a visitor that visits a blank ICell and produces a result of
  // type T
  T visitBlank(Blank blank);

  // to represent a visitor that visits a wall ICell and produces a result of type
  // T
  T visitWall(Wall wall);

  // to represent a visitor that visits a box ICell and produces a result of type
  // T
  T visitBox(Box box);

  // to represent a visitor that visits a player ICell and produces a result of
  // type T
  T visitPlayer(Player player);

  // to represent a visitor that visits a target ICell and produces a result of
  // type T
  T visitTarget(Target target);

  // to represent a visitor that visits a trophy ICell and produces a result of
  // type T
  T visitTrophy(Trophy trophy);

  // to represent a visitor that visits a hole ICell and produces a result of type
  // T
  T visitHole(Hole hole);
}

// to represent an ICellVisitor that visits an ICell and evaluates the cell to a Posn
// representing the location of the visited cell
class CellPosnVisitor implements ICellVisitor<Posn> {

  // to represent a visitor that visits a blank ICell and produces a Posn
  // representing the location of the visited blank cell
  public Posn visitBlank(Blank blank) {
    return blank.coord;
  }

  // to represent a visitor that visits a wall ICell and produces a Posn
  // representing the location of the visited wall cell
  public Posn visitWall(Wall wall) {
    return wall.coord;
  }

  // to represent a visitor that visits a box ICell and produces a Posn
  // representing the location of the visited box cell
  public Posn visitBox(Box box) {
    return box.coord;
  }

  // to represent a visitor that visits a player ICell and produces a Posn
  // representing the location of the visited player cell
  public Posn visitPlayer(Player player) {
    return player.coord;
  }

  // to represent a visitor that visits a target ICell and produces a Posn
  // representing the location of the visited target cell
  public Posn visitTarget(Target target) {
    return target.coord;
  }

  // to represent a visitor that visits a trophy ICell and produces a Posn
  // representing the location of the visited trophy cell
  public Posn visitTrophy(Trophy trophy) {
    return trophy.coord;
  }

  // to represent a visitor that visits a hole ICell and produces a Posn
  // representing the location of the visited hole cell
  public Posn visitHole(Hole hole) {
    return hole.coord;
  }
}

// to represent an ICellVisitor that visits an ICell and evaluates the cell to an ICell
// representing a new player based on the given player either moved to a new location
// based on the given direction or in the same place
class MovePlayerVisitor implements ICellVisitor<ICell> {
  // to represent a player in the game
  ICell player;
  // to represent a direction the player can move
  String direction;
  // to represent the level content of the board
  ArrayList<ICell> levelContentsCells;
  // to represent the size of the board
  Posn size;

  MovePlayerVisitor(ICell player, String direction, ArrayList<ICell> levelContentsCells,
      Posn size) {
    this.player = player;
    this.direction = direction;
    this.levelContentsCells = levelContentsCells;
    this.size = size;
  }

  // to represent a visitor that visits a blank ICell and produces an ICell
  // representing a new player moved in the given direction
  public ICell visitBlank(Blank blank) {
    return this.player.move(this.direction);
  }

  // to represent a visitor that visits a wall ICell and produces an ICell
  // representing a new player not moved
  public ICell visitWall(Wall wall) {
    return this.player;
  }

  // to represent a visitor that visits a box ICell and produces an ICell
  // representing a new player moved in the given direction
  // will also move the box and the player concurrently in the given direction of
  // the player
  // under the condition that there is not a wall or other object next to the box
  // in the direction the player is moving
  public ICell visitBox(Box box) {
    ICell next = new Utils().findNext(this.levelContentsCells, this.direction,
        box.accept(new CellPosnVisitor()), this.size);
    // checks that the box has a free space to move, else does not move the player
    // or the box
    if (next.accept(new CanMoveToVisitor())) {
      ICell newBox = next.accept(new MoveBoxVisitor(box, this.direction));
      ICell newPlayer = this.player.move(this.direction);
      this.levelContentsCells.set(this.levelContentsCells.indexOf(box),
          new Blank(box.accept(new CellPosnVisitor())));
      this.levelContentsCells.set(this.levelContentsCells.indexOf(next), newBox);
      this.levelContentsCells.set(this.levelContentsCells.indexOf(this.player), newPlayer);
      return newPlayer;
    }
    else {
      return this.player;
    }
  }

  // to represent a visitor that visits a player ICell and produces an ICell
  // representing a new player moved in the given direction
  public ICell visitPlayer(Player player) {
    return this.player.move(this.direction);
  }

  // to represent a visitor that visits a target ICell and produces an ICell
  // representing anew player moved in the given direction
  public ICell visitTarget(Target target) {
    return this.player.move(this.direction);
  }

  // to represent a visitor that visits a trophy ICell and produces an ICell
  // representing anew player moved in the given direction
  // will also move the trophy and the player concurrently in the given direction
  // of the player
  // under the condition that there is not a wall or other object next to the
  // trophy
  // in the direction the player is moving
  public ICell visitTrophy(Trophy trophy) {
    ICell next = new Utils().findNext(this.levelContentsCells, this.direction,
        trophy.accept(new CellPosnVisitor()), this.size);
    // checks that the trophy has a free space to move, else does not move the
    // player or the box
    if (next.accept(new CanMoveToVisitor())) {
      ICell newTrophy = next.accept(new MoveTrophyVisitor(trophy, this.direction));
      ICell newPlayer = this.player.move(this.direction);
      this.levelContentsCells.set(this.levelContentsCells.indexOf(trophy),
          new Blank(trophy.accept(new CellPosnVisitor())));
      this.levelContentsCells.set(this.levelContentsCells.indexOf(next), newTrophy);
      this.levelContentsCells.set(this.levelContentsCells.indexOf(this.player), newPlayer);
      return newPlayer;
    }
    else {
      return this.player;
    }
  }

  // to represent a visitor that visits a hole ICell and produces an ICell
  // representing anew player moved in the given direction
  public ICell visitHole(Hole hole) {
    return new Blank(hole.coord);

  }
}

// to represent an ICellVisitor that visits an ICell
// returns whether or not the given ICell should be able to be moved
// used on boxes and trophies to check if there is another ICell next to them
// that makes them unable to be moved
class CanMoveToVisitor implements ICellVisitor<Boolean> {
  // returns true because an ICell can move to a blank
  public Boolean visitBlank(Blank blank) {
    return true;
  }

  // returns false because an ICell (more specifically Boxes and Trophies) cannot
  // move to a wall
  public Boolean visitWall(Wall wall) {
    return false;
  }

  // returns false because an ICell (more specifically Boxes and Trophies) cannot
  // move to a Box
  public Boolean visitBox(Box box) {
    return false;
  }

  // returns false because an ICell (more specifically Boxes and Trophies) cannot
  // move to a Player
  public Boolean visitPlayer(Player player) {
    return false;
  }

  // returns true because an ICell can move to a Player
  public Boolean visitTarget(Target target) {
    return true;
  }

  // returns false because an ICell (more specifically boxes and Trophies) cannot
  // move to a Trophy
  public Boolean visitTrophy(Trophy trophy) {
    return false;
  }

  // returns true because an ICell can move to Hole
  // but will then be lost
  public Boolean visitHole(Hole hole) {
    return true;
  }
}

//to represent an ICellVisitor that visits an ICell and moves a box to the given ICell
//if an only if it should be able to be moved
class MoveBoxVisitor implements ICellVisitor<ICell> {
  ICell box;
  String direction;

  MoveBoxVisitor(ICell box, String direction) {
    this.box = box;
    this.direction = direction;
  }

  // moves a box onto a blank space
  public ICell visitBlank(Blank blank) {
    return this.box.move(this.direction);
  }

  // returns a wall as a box is unable to move onto a wall
  public ICell visitWall(Wall wall) {
    return this.box;
  }

  // returns a box as a box is unable to move onto a box
  public ICell visitBox(Box box) {
    return this.box;
  }

  // returns a player as a box is unable to move onto a player
  public ICell visitPlayer(Player player) {
    return this.box;
  }

  // moves a box as it moves onto a target
  public ICell visitTarget(Target target) {
    return this.box.move(this.direction);
  }

  // returns a trophy as it is unable to move onto a player
  public ICell visitTrophy(Trophy trophy) {
    return this.box;
  }

  // returns a blank as an item is now lost forever and the hole is gone
  public ICell visitHole(Hole hole) {
    return new Blank(hole.coord);
  }
}

// to represent an ICellVisitor that visits an ICell and moves a trophy to the given ICell
// if an only if it should be able to be moved
class MoveTrophyVisitor implements ICellVisitor<ICell> {
  ICell trophy;
  String direction;

  MoveTrophyVisitor(ICell trophy, String direction) {
    this.trophy = trophy;
    this.direction = direction;
  }

  // moves trophy onto a blank
  public ICell visitBlank(Blank blank) {
    return this.trophy.move(this.direction);
  }

  // unable to move a trophy onto a wall, so returns the trophy as is
  public ICell visitWall(Wall wall) {
    return this.trophy;
  }

  // unable to move a trophy onto a box, so returns the trophy as is
  public ICell visitBox(Box box) {
    return this.trophy;
  }

  // unable to move a trophy onto a player, so returns the trophy as is
  public ICell visitPlayer(Player player) {
    return this.trophy;
  }

  // moves trophy onto a target
  public ICell visitTarget(Target target) {
    return this.trophy.move(this.direction);
  }

  // unable to move a trophy onto a player, so returns the trophy as is
  public ICell visitTrophy(Trophy trophy) {
    return this.trophy;
  }

  // returns a blank as an item is now lost forever and the hole is gone
  public ICell visitHole(Hole hole) {
    return new Blank(hole.coord);
  }
}

// to represent an ICellVisitor that visits an ICell and evaluates the cell to a boolean
// representing whether the given cell and the visited cell
// are a good pair (target has the correct color)
class GoodPairVisitor implements ICellVisitor<Boolean> {
  // to represent a cell in the game
  ICell cell;

  GoodPairVisitor(ICell cell) {
    this.cell = cell;
  }

  // to represent a visitor that visits a blank ICell and produces a boolean
  // representing that the given cell and the visited blank cell are a good pair
  public Boolean visitBlank(Blank blank) {
    return true;
  }

  // to represent a visitor that visits a wall ICell and produces a boolean
  // representing that the given cell and the visited wall cell are a good pair
  public Boolean visitWall(Wall wall) {
    return true;
  }

  // to represent a visitor that visits a box ICell and produces a boolean
  // representing that the given cell and the visited box cell are a good pair
  public Boolean visitBox(Box box) {
    return true;
  }

  // to represent a visitor that visits a player ICell and produces a boolean
  // representing that the given cell and the visited player cell are a good pair
  public Boolean visitPlayer(Player player) {
    return true;
  }

  // to represent a visitor that visits a target ICell and produces a boolean
  // representing if this visitor's cell is a match for the target's color
  public Boolean visitTarget(Target target) {
    return this.cell.goodPair(target.color);
  }

  // to represent a visitor that visits a trophy ICell and produces a boolean
  // representing that the given cell and the visited trophy cell are a good pair
  // since only the target's pair is important
  public Boolean visitTrophy(Trophy trophy) {
    return true;
  }

  // to represent a visitor that visits a hole ICell and produces a boolean
  // representing that the given cell and the visited hole cell are a good pair
  public Boolean visitHole(Hole hole) {
    return true;
  }
}

// tests and examples for SokobanGame
class ExamplesSokobanGame {

  // tests and examples for constructor for SokobanBoard
  boolean testSokobanBoardConstructor_SokobanBoard(Tester t) {
    String givenExLevelGround = "________\n" + "___R____\n" + "________\n" + "_B____Y_\n"
        + "________\n" + "___G____\n" + "________";

    String givenExLevelContents = "__WWW___\n" + "__W_WW__\n" + "WWWr_WWW\n" + "W_b>yB_W\n"
        + "WW_gWWWW\n" + "_WW_W___\n" + "__WWW___";

    SokobanBoard givenExB = new SokobanBoard(givenExLevelGround, givenExLevelContents);

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

    return t.checkConstructorNoException("testSokobanBoardConstructor", "SokobanBoard",
        givenExLevelGround, givenExLevelContents)
        && t.checkExpect(givenExB,
            new SokobanBoard(new Posn(8, 7), givenExLevelGroundList, givenExLevelContentsList))
        && t.checkConstructorException(
            new IllegalArgumentException(
                "Dimensions of given level ground do not match dimensions of given level contents"),
            "SokobanBoard", givenExLevelGround, "");
  }

  // tests and examples for render in SokobanBoard
  boolean testRender_SokobanBoard(Tester t) {

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

    return t.checkExpect(shortExBoard.render(), result);
  }

  // tests and examples for playerMove in SokobanBoard
  boolean testPlayerMove_SokobanBoard(Tester t) {

    ArrayList<ICell> shortExLevelGround = new ArrayList<ICell>();
    shortExLevelGround.add(new Blank(new Posn(1, 1)));
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

    // player moves down
    ArrayList<ICell> shortExLevelContents1 = new ArrayList<ICell>();
    shortExLevelContents1.add(new Blank(new Posn(2, 1)));
    shortExLevelContents1.add(new Blank(new Posn(1, 2)));
    shortExLevelContents1.add(new Blank(new Posn(2, 2)));
    shortExLevelContents1.add(new Player(new Posn(1, 2)));
    shortExLevelContents1.add(new Blank(new Posn(1, 1)));

    SokobanBoard shortExB1 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents1);

    // player moves right
    ArrayList<ICell> shortExLevelContents2 = new ArrayList<ICell>();
    shortExLevelContents2.add(new Blank(new Posn(2, 1)));
    shortExLevelContents2.add(new Blank(new Posn(1, 2)));
    shortExLevelContents2.add(new Blank(new Posn(2, 2)));
    shortExLevelContents2.add(new Blank(new Posn(1, 1)));
    shortExLevelContents2.add(new Player(new Posn(2, 2)));

    SokobanBoard shortExB2 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents2);

    // player moves up
    ArrayList<ICell> shortExLevelContents3 = new ArrayList<ICell>();
    shortExLevelContents3.add(new Blank(new Posn(2, 1)));
    shortExLevelContents3.add(new Blank(new Posn(1, 2)));
    shortExLevelContents3.add(new Blank(new Posn(2, 2)));
    shortExLevelContents3.add(new Blank(new Posn(1, 1)));
    shortExLevelContents3.add(new Player(new Posn(2, 1)));

    SokobanBoard shortExB3 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents3);

    // player moves left
    ArrayList<ICell> shortExLevelContents4 = new ArrayList<ICell>();
    shortExLevelContents4.add(new Blank(new Posn(2, 1)));
    shortExLevelContents4.add(new Blank(new Posn(1, 2)));
    shortExLevelContents4.add(new Blank(new Posn(2, 2)));
    shortExLevelContents4.add(new Blank(new Posn(1, 1)));
    shortExLevelContents4.add(new Player(new Posn(1, 1)));

    SokobanBoard shortExB4 = new SokobanBoard(new Posn(2, 2), shortExLevelGround,
        shortExLevelContents4);

    return t.checkExpect(shortExB0.playerMove("down"), shortExB1)
        && t.checkExpect(shortExB1.playerMove("right"), shortExB2)
        && t.checkExpect(shortExB2.playerMove("up"), shortExB3)
        && t.checkExpect(shortExB3.playerMove("left"), shortExB4);
  }

  // tests and examples for levelWon in SokobanBoard
  boolean testLevelWon_SokobanBoard(Tester t) {

    String emptyGround = "__";

    String emptyContents = "__";

    SokobanBoard empty = new SokobanBoard(emptyGround, emptyContents);

    String wonExactExLevelGround = "___R____\n" + "_B____Y_\n" + "___G____";

    String wonExactExLevelContents = "___r____\n" + "_b____y_\n" + "___g____";

    SokobanBoard wonExact = new SokobanBoard(wonExactExLevelGround, wonExactExLevelContents);

    String wonInexactExLevelGround = "___R____\n" + "_B____Y_\n" + "___G____";

    String wonInexactExLevelContents = "___r__g_\n" + "_b_b__y_\n" + "r__g_y__";

    SokobanBoard wonInexact = new SokobanBoard(wonInexactExLevelGround, wonInexactExLevelContents);

    String givenExLevelGround = "________\n" + "___R____\n" + "________\n" + "_B____Y_\n"
        + "________\n" + "___G____\n" + "________";

    String givenExLevelContents = "__WWW___\n" + "__W_WW__\n" + "WWWr_WWW\n" + "W_b>yB_W\n"
        + "WW_gWWWW\n" + "_WW_W___\n" + "__WWW___";

    SokobanBoard givenExB = new SokobanBoard(givenExLevelGround, givenExLevelContents);

    return t.checkExpect(empty.levelWon(), true) && t.checkExpect(wonExact.levelWon(), true)
        && t.checkExpect(wonInexact.levelWon(), true) && t.checkExpect(givenExB.levelWon(), false);
  }

  // tests and examples for drawICell for ICell
  boolean testDrawICell_ICell(Tester t) {
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

    return t.checkExpect(blank.drawICell(), BLANK) && t.checkExpect(rTarget.drawICell(), RTARGET)
        && t.checkExpect(yTarget.drawICell(), YTARGET)
        && t.checkExpect(bTarget.drawICell(), BTARGET)
        && t.checkExpect(gTarget.drawICell(), GTARGET) && t.checkExpect(player.drawICell(), PLAYER)
        && t.checkExpect(wall.drawICell(), WALL) && t.checkExpect(box.drawICell(), BOX)
        && t.checkExpect(rTrophy.drawICell(), RTROPHY)
        && t.checkExpect(yTrophy.drawICell(), YTROPHY)
        && t.checkExpect(bTrophy.drawICell(), BTROPHY)
        && t.checkExpect(gTrophy.drawICell(), GTROPHY) && t.checkExpect(hole.drawICell(), HOLE);
  }

  // tests and examples for accept for ICell
  boolean testAccept_ICell(Tester t) {
    Posn ex = new Posn(1, 1);
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
    ICell player = new Player(new Posn(0, 0));
    ICell hole = new Hole(ex);

    return t.checkExpect(blank.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(yTarget.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(gTarget.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(bTarget.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(rTarget.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(yTrophy.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(gTrophy.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(bTrophy.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(rTrophy.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(wall.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(box.accept(new CellPosnVisitor()), ex)
        && t.checkExpect(player.accept(new CellPosnVisitor()), new Posn(0, 0))
        && t.checkExpect(hole.accept(new CellPosnVisitor()), ex);
  }

  // tests and examples for findPlayer for ICell
  boolean testFindPlayer_ICell(Tester t) {
    Posn ex = new Posn(0, 0);
    Posn bad = new Posn(-1, -1);
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
    ICell player = new Player(new Posn(1, 2));
    ICell hole = new Hole(ex);

    return t.checkExpect(blank.findPlayer(), bad) && t.checkExpect(yTarget.findPlayer(), bad)
        && t.checkExpect(gTarget.findPlayer(), bad) && t.checkExpect(bTarget.findPlayer(), bad)
        && t.checkExpect(rTarget.findPlayer(), bad) && t.checkExpect(yTrophy.findPlayer(), bad)
        && t.checkExpect(gTrophy.findPlayer(), bad) && t.checkExpect(bTrophy.findPlayer(), bad)
        && t.checkExpect(rTrophy.findPlayer(), bad) && t.checkExpect(wall.findPlayer(), bad)
        && t.checkExpect(box.findPlayer(), bad)
        && t.checkExpect(player.findPlayer(), new Posn(1, 2))
        && t.checkExpect(hole.findPlayer(), bad);
  }

  // tests and examples for move for ICell
  boolean testMove_ICell(Tester t) {
    Posn ex = new Posn(1, 1);
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

    return t.checkExpect(blank.move("right"), blank) && t.checkExpect(yTarget.move("left"), yTarget)
        && t.checkExpect(gTarget.move("right"), gTarget)
        && t.checkExpect(bTarget.move("left"), bTarget)
        && t.checkExpect(rTarget.move("up"), rTarget) && t.checkExpect(wall.move("down"), wall)
        && t.checkExpect(hole.move("up"), hole)
        && t.checkExpect(yTrophy.move("right"), new Trophy(new Posn(2, 1), Color.yellow))
        && t.checkExpect(gTrophy.move("left"), new Trophy(new Posn(0, 1), Color.green))
        && t.checkExpect(bTrophy.move("down"), new Trophy(new Posn(1, 2), Color.blue))
        && t.checkExpect(rTrophy.move("up"), new Trophy(new Posn(1, 0), Color.red))
        && t.checkExpect(box.move("right"), new Box(new Posn(2, 1)))
        && t.checkExpect(box.move("left"), new Box(new Posn(0, 1)))
        && t.checkExpect(box.move("down"), new Box(new Posn(1, 2)))
        && t.checkExpect(box.move("up"), new Box(new Posn(1, 0)))
        && t.checkExpect(player.move("right"), new Player(new Posn(2, 1)))
        && t.checkExpect(player.move("left"), new Player(new Posn(0, 1)))
        && t.checkExpect(player.move("down"), new Player(new Posn(1, 2)))
        && t.checkExpect(player.move("up"), new Player(new Posn(1, 0)));
  }

  // tests and examples for goodPair for ICell
  boolean testGoodPair_ICell(Tester t) {
    Posn ex = new Posn(1, 1);
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

    return t.checkExpect(blank.goodPair(Color.yellow), false)
        && t.checkExpect(yTarget.goodPair(Color.green), false)
        && t.checkExpect(gTarget.goodPair(Color.blue), false)
        && t.checkExpect(bTarget.goodPair(Color.red), false)
        && t.checkExpect(rTarget.goodPair(Color.yellow), false)
        && t.checkExpect(wall.goodPair(Color.green), false)
        && t.checkExpect(box.goodPair(Color.blue), false)
        && t.checkExpect(player.goodPair(Color.red), false)
        && t.checkExpect(hole.goodPair(Color.yellow), false)
        && t.checkExpect(yTrophy.goodPair(Color.yellow), true)
        && t.checkExpect(gTrophy.goodPair(Color.red), false)
        && t.checkExpect(bTrophy.goodPair(Color.blue), true)
        && t.checkExpect(rTrophy.goodPair(Color.yellow), false)
        && t.checkExpect(rTrophy.goodPair(Color.green), false)
        && t.checkExpect(rTrophy.goodPair(Color.red), true)
        && t.checkExpect(rTrophy.goodPair(Color.blue), false);
  }

  // tests and examples for CellPosnVisitor
  boolean test_CellPosnVisitor(Tester t) {
    Posn ex = new Posn(1, 1);

    Blank blankO = new Blank(ex);
    Target yTargetO = new Target(ex, Color.yellow);
    Target gTargetO = new Target(ex, Color.green);
    Target bTargetO = new Target(ex, Color.blue);
    Target rTargetO = new Target(ex, Color.red);
    Trophy yTrophyO = new Trophy(ex, Color.yellow);
    Trophy gTrophyO = new Trophy(ex, Color.green);
    Trophy bTrophyO = new Trophy(ex, Color.blue);
    Trophy rTrophyO = new Trophy(ex, Color.red);
    Wall wallO = new Wall(ex);
    Box boxO = new Box(ex);
    Player playerO = new Player(ex);
    Hole holeO = new Hole(ex);

    CellPosnVisitor visitor = new CellPosnVisitor();

    return t.checkExpect(visitor.visitBlank(blankO), ex)
        && t.checkExpect(visitor.visitTarget(yTargetO), ex)
        && t.checkExpect(visitor.visitTarget(gTargetO), ex)
        && t.checkExpect(visitor.visitTarget(bTargetO), ex)
        && t.checkExpect(visitor.visitTarget(rTargetO), ex)
        && t.checkExpect(visitor.visitTrophy(yTrophyO), ex)
        && t.checkExpect(visitor.visitTrophy(gTrophyO), ex)
        && t.checkExpect(visitor.visitTrophy(bTrophyO), ex)
        && t.checkExpect(visitor.visitTrophy(rTrophyO), ex)
        && t.checkExpect(visitor.visitWall(wallO), ex) && t.checkExpect(visitor.visitBox(boxO), ex)
        && t.checkExpect(visitor.visitPlayer(playerO), ex)
        && t.checkExpect(visitor.visitHole(holeO), ex);
  }

  // tests and examples for GoodPairVisitor
  boolean test_GoodPairVisitor(Tester t) {
    Posn ex = new Posn(1, 1);

    Blank blankO = new Blank(ex);
    Target yTargetO = new Target(ex, Color.yellow);
    Target gTargetO = new Target(ex, Color.green);
    Target bTargetO = new Target(ex, Color.blue);
    Target rTargetO = new Target(ex, Color.red);
    Trophy yTrophyO = new Trophy(ex, Color.yellow);
    Trophy gTrophyO = new Trophy(ex, Color.green);
    Trophy bTrophyO = new Trophy(ex, Color.blue);
    Trophy rTrophyO = new Trophy(ex, Color.red);
    Wall wallO = new Wall(ex);
    Box boxO = new Box(ex);
    Player playerO = new Player(ex);
    Hole holeO = new Hole(ex);

    GoodPairVisitor visitorTrophy = new GoodPairVisitor(yTrophyO);
    GoodPairVisitor visitorWall = new GoodPairVisitor(wallO);

    return t.checkExpect(visitorTrophy.visitBlank(blankO), true)
        && t.checkExpect(visitorTrophy.visitTarget(yTargetO), true)
        && t.checkExpect(visitorTrophy.visitTarget(gTargetO), false)
        && t.checkExpect(visitorTrophy.visitTarget(bTargetO), false)
        && t.checkExpect(visitorTrophy.visitTarget(rTargetO), false)
        && t.checkExpect(visitorTrophy.visitTrophy(yTrophyO), true)
        && t.checkExpect(visitorTrophy.visitTrophy(gTrophyO), true)
        && t.checkExpect(visitorTrophy.visitTrophy(bTrophyO), true)
        && t.checkExpect(visitorTrophy.visitTrophy(rTrophyO), true)
        && t.checkExpect(visitorTrophy.visitWall(wallO), true)
        && t.checkExpect(visitorTrophy.visitBox(boxO), true)
        && t.checkExpect(visitorTrophy.visitPlayer(playerO), true)
        && t.checkExpect(visitorTrophy.visitHole(holeO), true)
        && t.checkExpect(visitorWall.visitBlank(blankO), true)
        && t.checkExpect(visitorWall.visitTarget(yTargetO), false)
        && t.checkExpect(visitorWall.visitTarget(gTargetO), false)
        && t.checkExpect(visitorWall.visitTarget(bTargetO), false)
        && t.checkExpect(visitorWall.visitTarget(rTargetO), false)
        && t.checkExpect(visitorWall.visitTrophy(yTrophyO), true)
        && t.checkExpect(visitorWall.visitTrophy(gTrophyO), true)
        && t.checkExpect(visitorWall.visitTrophy(bTrophyO), true)
        && t.checkExpect(visitorWall.visitTrophy(rTrophyO), true)
        && t.checkExpect(visitorWall.visitWall(wallO), true)
        && t.checkExpect(visitorWall.visitBox(boxO), true)
        && t.checkExpect(visitorWall.visitPlayer(playerO), true)
        && t.checkExpect(visitorWall.visitHole(holeO), true);
  }

  // tests on shouldEnd()
  boolean testShouldEnd(Tester t) {

    String emptyGround = "__";
    String emptyContents = "__";
    SokobanBoard empty = new SokobanBoard(emptyGround, emptyContents);

    String wonExactExLevelGround = "___R____\n" + "_B____Y_\n" + "___G____";
    String wonExactExLevelContents = "___r____\n" + "_b____y_\n" + "___g____";
    SokobanBoard wonExact = new SokobanBoard(wonExactExLevelGround, wonExactExLevelContents);

    String wonInexactExLevelGround = "___R____\n" + "_B____Y_\n" + "___G____";
    String wonInexactExLevelContents = "___r__g_\n" + "_b_b__y_\n" + "r__g_y__";
    SokobanBoard wonInexact = new SokobanBoard(wonInexactExLevelGround, wonInexactExLevelContents);

    String givenExLevelGround = "________\n" + "___R____\n" + "________\n" + "_B____Y_\n"
        + "________\n" + "___G____\n" + "________";
    String givenExLevelContents = "__WWW___\n" + "__W_WW__\n" + "WWWr_WWW\n" + "W_b>yB_W\n"
        + "WW_gWWWW\n" + "_WW_W___\n" + "__WWW___";
    SokobanBoard givenExB = new SokobanBoard(givenExLevelGround, givenExLevelContents);

    return t.checkExpect(empty.shouldEnd(), true) && t.checkExpect(wonExact.shouldEnd(), true)
        && t.checkExpect(wonInexact.shouldEnd(), true)
        && t.checkExpect(givenExB.shouldEnd(), false);
  }

  // tests the visitBlank method of the MovePlayer class
  boolean testVisitBlankMovePlayerLeft(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(0, 1)));
    ICell player = new Player(new Posn(1, 1));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "left", levelContentsCells,
        new Posn(2, 2));

    ICell newPlayer = visitor.visitBlank(new Blank(new Posn(0, 0)));

    return t.checkExpect(newPlayer, new Player(new Posn(0, 1)));
  }

  // tests the visitWall method of the MovePlayer class
  boolean testVisitWallMovePlayerLeft(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Wall(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(0, 1)));
    ICell player = new Player(new Posn(1, 1));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "left", levelContentsCells,
        new Posn(2, 2));

    ICell newPlayer = visitor.visitWall(new Wall(new Posn(0, 0)));

    return t.checkExpect(newPlayer, new Player(new Posn(1, 1)));
  }

  // Tests the visitPlayer method of the MovePlayer class
  boolean testVisitPlayerMovePlayerLeft(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(0, 1)));
    ICell player = new Player(new Posn(1, 1));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "left", levelContentsCells,
        new Posn(2, 2));

    ICell newPlayer = visitor.visitPlayer(new Player(new Posn(1, 1)));

    return t.checkExpect(newPlayer, new Player(new Posn(0, 1)));
  }

  // tests the visitTarget method of the MovePlayer class
  boolean testVisitTargetMovePlayerLeft(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Target(new Posn(0, 1), new Color(1)));
    ICell player = new Player(new Posn(1, 1));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "left", levelContentsCells,
        new Posn(2, 2));

    ICell newPlayer = visitor.visitTarget(new Target(new Posn(0, 1), new Color(1)));

    return t.checkExpect(newPlayer, new Player(new Posn(0, 1)));
  }

  // tests the visitBlank method of the MovePlayer class
  boolean testVisitBlankMovePlayerRight(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(0, 1)));
    ICell player = new Player(new Posn(0, 0));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "right", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitBlank(new Blank(new Posn(0, 1)));

    return t.checkExpect(newPlayer, new Player(new Posn(1, 0)));
  }

  // tests the visitWall method of the MovePlayer class
  boolean testVisitWallMovePlayerRight(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Wall(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(0, 1)));
    ICell player = new Player(new Posn(0, 0));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "right", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitWall(new Wall(new Posn(0, 1)));

    return t.checkExpect(newPlayer, new Player(new Posn(0, 0)));
  }

  // Tests the visitPlayer method of the MovePlayer class
  boolean testVisitPlayerMovePlayerRight(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(0, 1)));
    ICell player = new Player(new Posn(0, 0));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "right", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitPlayer(new Player(new Posn(0, 1)));

    return t.checkExpect(newPlayer, new Player(new Posn(1, 0)));
  }

  // tests the visitTarget method of the MovePlayer class
  boolean testVisitTargetMovePlayerRight(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Target(new Posn(0, 1), new Color(1)));
    ICell player = new Player(new Posn(0, 0));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "right", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitTarget(new Target(new Posn(0, 1), new Color(1)));

    return t.checkExpect(newPlayer, new Player(new Posn(1, 0)));
  }

  // tests the visitBlank method of the MovePlayer class
  boolean testVisitBlankMovePlayerDown(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(1, 0)));
    ICell player = new Player(new Posn(0, 0));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "down", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitBlank(new Blank(new Posn(1, 0)));

    return t.checkExpect(newPlayer, new Player(new Posn(0, 1)));
  }

  // tests the visitWall method of the MovePlayer class
  boolean testVisitWallMovePlayerDown(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Wall(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(1, 0)));
    ICell player = new Player(new Posn(0, 0));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "down", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitWall(new Wall(new Posn(1, 0)));

    return t.checkExpect(newPlayer, new Player(new Posn(0, 0)));
  }

  // Tests the visitPlayer method of the MovePlayer class
  boolean testVisitPlayerMovePlayerDown(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(1, 0)));
    ICell player = new Player(new Posn(0, 0));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "down", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitPlayer(new Player(new Posn(1, 0)));

    return t.checkExpect(newPlayer, new Player(new Posn(0, 1)));
  }

  // tests the visitTarget method of the MovePlayer class
  boolean testVisitTargetMovePlayerDown(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Target(new Posn(1, 0), new Color(1)));
    ICell player = new Player(new Posn(0, 0));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "down", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitTarget(new Target(new Posn(1, 0), new Color(1)));

    return t.checkExpect(newPlayer, new Player(new Posn(0, 1)));
  }

  // tests the visitBlank method of the MovePlayer class
  boolean testVisitBlankMovePlayerUp(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(1, 0)));
    ICell player = new Player(new Posn(1, 1));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "up", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitBlank(new Blank(new Posn(1, 0)));

    return t.checkExpect(newPlayer, new Player(new Posn(1, 0)));
  }

  // tests the visitWall method of the MovePlayer class
  boolean testVisitWallMovePlayerUp(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Wall(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(1, 0)));
    ICell player = new Player(new Posn(1, 1));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "up", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitWall(new Wall(new Posn(1, 0)));

    return t.checkExpect(newPlayer, new Player(new Posn(1, 1)));
  }

  // Tests the visitPlayer method of the MovePlayer class
  boolean testVisitPlayerMovePlayerUp(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Blank(new Posn(1, 0)));
    ICell player = new Player(new Posn(1, 1));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "up", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitPlayer(new Player(new Posn(1, 0)));

    return t.checkExpect(newPlayer, new Player(new Posn(1, 0)));
  }

  // tests the visitTarget method of the MovePlayer class
  boolean testVisitTargetMovePlayerUp(Tester t) {
    ArrayList<ICell> levelContentsCells = new ArrayList<>();
    levelContentsCells.add(new Blank(new Posn(0, 0)));
    levelContentsCells.add(new Target(new Posn(1, 0), new Color(1)));
    ICell player = new Player(new Posn(1, 1));
    MovePlayerVisitor visitor = new MovePlayerVisitor(player, "up", levelContentsCells,
        new Posn(2, 2));
    ICell newPlayer = visitor.visitTarget(new Target(new Posn(1, 0), new Color(1)));

    return t.checkExpect(newPlayer, new Player(new Posn(1, 0)));
  }

  // Tests the visitBlank method of the MoveBoxVisitor class
  boolean testVisitBlankMoveBoxVisitorleft(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "left");
    Blank blank = new Blank(new Posn(1, 0));

    ICell newBox = visitor.visitBlank(blank);

    return t.checkExpect(newBox.findCoords(), new Posn(0, 1));
  }

  // Tests the visitWall method of the MoveBoxVisitor class
  boolean testVisitWallMoveBoxVisitorleft(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "left");
    Wall wall = new Wall(new Posn(1, 0));

    ICell newBox = visitor.visitWall(wall);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitBox method of the MoveBoxVisitor class
  boolean testVisitBoxMoveBoxVisitorleft(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "left");
    Box otherBox = new Box(new Posn(1, 0));

    ICell newBox = visitor.visitBox(otherBox);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitPlayer method of the MoveBoxVisitor class
  boolean testVisitPlayerMoveBoxVisitorleft(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "left");
    Player player = new Player(new Posn(1, 0));

    ICell newBox = visitor.visitPlayer(player);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitTarget method of the MoveBoxVisitor class
  boolean testVisitTargetMoveBoxVisitorleft(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "left");
    Target target = new Target(new Posn(1, 0), new Color(1));

    ICell newBox = visitor.visitTarget(target);

    return t.checkExpect(newBox.findCoords(), new Posn(0, 1));
  }

  // Tests the visitTrophy method of the MoveBoxVisitor class
  boolean testVisitTrophyMoveBoxVisitorleft(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "left");
    Trophy trophy = new Trophy(new Posn(1, 0), new Color(1));

    ICell newBox = visitor.visitTrophy(trophy);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitHole method of the MoveBoxVisitor class
  boolean testVisitHoleMoveBoxVisitorleft(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "left");
    Hole hole = new Hole(new Posn(1, 0));

    ICell newCell = visitor.visitHole(hole);

    return t.checkExpect(newCell, new Blank(new Posn(1, 0)));
  }

  // Tests the visitBlank method of the MoveBoxVisitor class for up motion
  boolean testVisitBlankMoveBoxVisitorUp(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "up");
    Blank blank = new Blank(new Posn(0, 1));

    ICell newBox = visitor.visitBlank(blank);

    return t.checkExpect(newBox.findCoords(), new Posn(1, 0));
  }

  // Tests the visitWall method of the MoveBoxVisitor class for up motion
  boolean testVisitWallMoveBoxVisitorUp(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "up");
    Wall wall = new Wall(new Posn(0, 1));

    ICell newBox = visitor.visitWall(wall);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitBox method of the MoveBoxVisitor class for up motion
  boolean testVisitBoxMoveBoxVisitorUp(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "up");
    Box otherBox = new Box(new Posn(0, 1));

    ICell newBox = visitor.visitBox(otherBox);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitPlayer method of the MoveBoxVisitor class for up motion
  boolean testVisitPlayerMoveBoxVisitorUp(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "up");
    Player player = new Player(new Posn(0, 1));

    ICell newBox = visitor.visitPlayer(player);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitTarget method of the MoveBoxVisitor class for up motion
  boolean testVisitTargetMoveBoxVisitorUp(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "up");
    Target target = new Target(new Posn(0, 1), new Color(1));

    ICell newBox = visitor.visitTarget(target);

    return t.checkExpect(newBox.findCoords(), new Posn(1, 0));
  }

  // Tests the visitTrophy method of the MoveBoxVisitor class for up motion
  boolean testVisitTrophyMoveBoxVisitorUp(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "up");
    Trophy trophy = new Trophy(new Posn(0, 1), new Color(1));

    ICell newBox = visitor.visitTrophy(trophy);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitHole method of the MoveBoxVisitor class for up motion
  boolean testVisitHoleMoveBoxVisitorUp(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "up");
    Hole hole = new Hole(new Posn(0, 1));

    ICell newCell = visitor.visitHole(hole);

    return t.checkExpect(newCell, new Blank(new Posn(0, 1)));
  }

  // Tests the visitBlank method of the MoveBoxVisitor class
  boolean testVisitBlankMoveBoxVisitorDown(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "down");
    Blank blank = new Blank(new Posn(2, 1));

    ICell newBox = visitor.visitBlank(blank);

    return t.checkExpect(newBox.findCoords(), new Posn(1, 2));
  }

  // Tests the visitWall method of the MoveBoxVisitor class
  boolean testVisitWallMoveBoxVisitorDown(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "down");
    Wall wall = new Wall(new Posn(2, 1));

    ICell newBox = visitor.visitWall(wall);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitBox method of the MoveBoxVisitor class
  boolean testVisitBoxMoveBoxVisitorDown(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "down");
    Box otherBox = new Box(new Posn(2, 1));

    ICell newBox = visitor.visitBox(otherBox);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitPlayer method of the MoveBoxVisitor class
  boolean testVisitPlayerMoveBoxVisitorDown(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "down");
    Player player = new Player(new Posn(2, 1));

    ICell newBox = visitor.visitPlayer(player);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitTarget method of the MoveBoxVisitor class
  boolean testVisitTargetMoveBoxVisitorDown(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "down");
    Target target = new Target(new Posn(2, 1), new Color(1));

    ICell newBox = visitor.visitTarget(target);

    return t.checkExpect(newBox.findCoords(), new Posn(1, 2));
  }

  // Tests the visitTrophy method of the MoveBoxVisitor class
  boolean testVisitTrophyMoveBoxVisitorDown(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "down");
    Trophy trophy = new Trophy(new Posn(2, 1), new Color(1));

    ICell newBox = visitor.visitTrophy(trophy);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitHole method of the MoveBoxVisitor class
  boolean testVisitHoleMoveBoxVisitorDown(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "down");
    Hole hole = new Hole(new Posn(2, 1));

    ICell newCell = visitor.visitHole(hole);

    return t.checkExpect(newCell, new Blank(new Posn(2, 1)));
  }

  // Tests the visitBlank method of the MoveBoxVisitor class
  boolean testVisitBlankMoveBoxVisitorRight(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "right");
    Blank blank = new Blank(new Posn(1, 2));

    ICell newBox = visitor.visitBlank(blank);

    return t.checkExpect(newBox.findCoords(), new Posn(2, 1));
  }

  // Tests the visitWall method of the MoveBoxVisitor class
  boolean testVisitWallMoveBoxVisitorRight(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "right");
    Wall wall = new Wall(new Posn(1, 2));

    ICell newBox = visitor.visitWall(wall);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitBox method of the MoveBoxVisitor class
  boolean testVisitBoxMoveBoxVisitorRight(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "right");
    Box otherBox = new Box(new Posn(1, 2));

    ICell newBox = visitor.visitBox(otherBox);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitPlayer method of the MoveBoxVisitor class
  boolean testVisitPlayerMoveBoxVisitorRight(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "right");
    Player player = new Player(new Posn(1, 2));

    ICell newBox = visitor.visitPlayer(player);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitTarget method of the MoveBoxVisitor class
  boolean testVisitTargetMoveBoxVisitorRight(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "right");
    Target target = new Target(new Posn(1, 2), new Color(1));

    ICell newBox = visitor.visitTarget(target);

    return t.checkExpect(newBox.findCoords(), new Posn(2, 1));
  }

  // Tests the visitTrophy method of the MoveBoxVisitor class
  boolean testVisitTrophyMoveBoxVisitorRight(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "right");
    Trophy trophy = new Trophy(new Posn(1, 2), new Color(1));

    ICell newBox = visitor.visitTrophy(trophy);

    return t.checkExpect(newBox, box);
  }

  // Tests the visitHole method of the MoveBoxVisitor class
  boolean testVisitHoleMoveBoxVisitorRight(Tester t) {
    Box box = new Box(new Posn(1, 1));
    MoveBoxVisitor visitor = new MoveBoxVisitor(box, "right");
    Hole hole = new Hole(new Posn(1, 2));

    ICell newCell = visitor.visitHole(hole);

    return t.checkExpect(newCell, new Blank(new Posn(1, 2)));
  }

  // Tests the visitBlank method of the MoveTrophyVisitor class
  boolean testVisitBlankMoveTrophyVisitorleft(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "left");
    Blank blank = new Blank(new Posn(1, 0));

    ICell newTrophy = visitor.visitBlank(blank);

    return t.checkExpect(newTrophy.findCoords(), new Posn(0, 1));
  }

  // Tests the visitWall method of the MoveTrophyVisitor class
  boolean testVisitWallMoveTrophyVisitorleft(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "left");
    Wall wall = new Wall(new Posn(1, 0));

    ICell newTrophy = visitor.visitWall(wall);

    return t.checkExpect(newTrophy, trophy);
  }

  // Tests the visitBox method of the MoveTrophyVisitor class
  boolean testVisitBoxMoveTrophyVisitorleft(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "left");
    Box box = new Box(new Posn(1, 0));

    ICell newTrophy = visitor.visitBox(box);

    return t.checkExpect(newTrophy, trophy);
  }

  // Tests the visitPlayer method of the MoveTrophyVisitor class
  boolean testVisitPlayerMoveTrophyVisitorleft(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "left");
    Player player = new Player(new Posn(1, 0));

    ICell newTrophy = visitor.visitPlayer(player);

    return t.checkExpect(newTrophy, trophy);
  }

  // Tests the visitTarget method of the MoveTrophyVisitor class
  boolean testVisitTargetMoveTrophyVisitorleft(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "left");
    Target target = new Target(new Posn(1, 0), new Color(1));

    ICell newTrophy = visitor.visitTarget(target);

    return t.checkExpect(newTrophy.findCoords(), new Posn(0, 1));
  }

  // Tests the visitTrophy method of the MoveTrophyVisitor class
  boolean testVisitTrophyMoveTrophyVisitorleft(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "left");
    Trophy otherTrophy = new Trophy(new Posn(1, 0), new Color(2));

    ICell newTrophy = visitor.visitTrophy(otherTrophy);

    return t.checkExpect(newTrophy, trophy);
  }

  // Tests the visitHole method of the MoveTrophyVisitor class
  boolean testVisitHoleMoveTrophyVisitorleft(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "left");
    Hole hole = new Hole(new Posn(1, 0));

    ICell newCell = visitor.visitHole(hole);

    return t.checkExpect(newCell, new Blank(new Posn(1, 0)));
  }

//Tests the visitBlank method of the MoveTrophyVisitor class
  boolean testVisitBlankMoveTrophyVisitorRight(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "right");
    Blank blank = new Blank(new Posn(1, 2));

    ICell newTrophy = visitor.visitBlank(blank);

    return t.checkExpect(newTrophy.findCoords(), new Posn(2, 1));
  }

//Tests the visitWall method of the MoveTrophyVisitor class
  boolean testVisitWallMoveTrophyVisitorRight(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "right");
    Wall wall = new Wall(new Posn(1, 2));

    ICell newTrophy = visitor.visitWall(wall);

    return t.checkExpect(newTrophy, trophy);
  }

//Tests the visitBox method of the MoveTrophyVisitor class
  boolean testVisitBoxMoveTrophyVisitorRight(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "right");
    Box box = new Box(new Posn(1, 2));

    ICell newTrophy = visitor.visitBox(box);

    return t.checkExpect(newTrophy, trophy);
  }

//Tests the visitPlayer method of the MoveTrophyVisitor class
  boolean testVisitPlayerMoveTrophyVisitorRight(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "right");
    Player player = new Player(new Posn(1, 2));

    ICell newTrophy = visitor.visitPlayer(player);

    return t.checkExpect(newTrophy, trophy);
  }

//Tests the visitTarget method of the MoveTrophyVisitor class
  boolean testVisitTargetMoveTrophyVisitorRight(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "right");
    Target target = new Target(new Posn(1, 2), new Color(1));

    ICell newTrophy = visitor.visitTarget(target);

    return t.checkExpect(newTrophy.findCoords(), new Posn(2, 1));
  }

//Tests the visitTrophy method of the MoveTrophyVisitor class
  boolean testVisitTrophyMoveTrophyVisitorRight(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "right");
    Trophy otherTrophy = new Trophy(new Posn(1, 2), new Color(2));

    ICell newTrophy = visitor.visitTrophy(otherTrophy);

    return t.checkExpect(newTrophy, trophy);
  }

//Tests the visitHole method of the MoveTrophyVisitor class
  boolean testVisitHoleMoveTrophyVisitorRight(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "right");
    Hole hole = new Hole(new Posn(1, 2));

    ICell newCell = visitor.visitHole(hole);

    return t.checkExpect(newCell, new Blank(new Posn(1, 2)));
  }

  // Tests the visitBlank method of the MoveTrophyVisitor class for up motion
  boolean testVisitBlankMoveTrophyVisitorUp(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "up");
    Blank blank = new Blank(new Posn(0, 1));

    ICell newBox = visitor.visitBlank(blank);

    return t.checkExpect(newBox.findCoords(), new Posn(1, 0));
  }

  // Tests the visitWall method of the MoveTrophyVisitor class for up motion
  boolean testVisitWallMoveTrophyVisitorUp(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "up");
    Wall wall = new Wall(new Posn(0, 1));

    ICell newBox = visitor.visitWall(wall);

    return t.checkExpect(newBox, trophy);
  }

  // Tests the visitBox method of the MoveTrophyVisitor class for up motion
  boolean testVisitBoxMoveTrophyVisitorUp(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "up");
    Box otherBox = new Box(new Posn(0, 1));

    ICell newBox = visitor.visitBox(otherBox);

    return t.checkExpect(newBox, trophy);
  }

  // Tests the visitPlayer method of the MoveTrophyVisitor class for up motion
  boolean testVisitPlayerMoveTrophyVisitorUp(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "up");
    Player player = new Player(new Posn(0, 1));

    ICell newBox = visitor.visitPlayer(player);

    return t.checkExpect(newBox, trophy);
  }

  // Tests the visitTarget method of the MoveTrophyVisitor class for up motion
  boolean testVisitTargetMoveTrophyVisitorUp(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "up");
    Target target = new Target(new Posn(0, 1), new Color(1));

    ICell newBox = visitor.visitTarget(target);

    return t.checkExpect(newBox.findCoords(), new Posn(1, 0));
  }

  // Tests the visitTrophy method of the MoveTrophyVisitor class for up motion
  boolean testVisitTrophyMoveTrophyVisitorUp(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "up");

    ICell newBox = visitor.visitTrophy(trophy);

    return t.checkExpect(newBox, trophy);
  }

  // Tests the visitHole method of the MoveTrophyVisitor class for up motion
  boolean testVisitHoleMoveTrophyVisitorUp(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "up");
    Hole hole = new Hole(new Posn(0, 1));

    ICell newCell = visitor.visitHole(hole);

    return t.checkExpect(newCell, new Blank(new Posn(0, 1)));
  }

  // Tests the visitBlank method of the MoveTrophyVisitor class
  boolean testVisitBlankMoveTrophyVisitorDown(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "down");
    Blank blank = new Blank(new Posn(2, 1));

    ICell newBox = visitor.visitBlank(blank);

    return t.checkExpect(newBox.findCoords(), new Posn(1, 2));
  }

  // Tests the visitWall method of the MoveTrophyVisitor class
  boolean testVisitWallMoveTrophyVisitorDown(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "down");
    Wall wall = new Wall(new Posn(2, 1));

    ICell newBox = visitor.visitWall(wall);

    return t.checkExpect(newBox, trophy);
  }

  // Tests the visitBox method of the MoveTrophyVisitor class
  boolean testVisitBoxMoveTrophyVisitorDown(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "down");
    Box otherBox = new Box(new Posn(2, 1));

    ICell newBox = visitor.visitBox(otherBox);

    return t.checkExpect(newBox, trophy);
  }

  // Tests the visitPlayer method of the MoveTrophyVisitor class
  boolean testVisitPlayerMoveTrophyVisitorDown(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "down");
    Player player = new Player(new Posn(2, 1));

    ICell newBox = visitor.visitPlayer(player);

    return t.checkExpect(newBox, trophy);
  }

  // Tests the visitTarget method of the MoveTrophyVisitor class
  boolean testVisitTargetMoveTrophyVisitorDown(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "down");
    Target target = new Target(new Posn(2, 1), new Color(1));

    ICell newBox = visitor.visitTarget(target);

    return t.checkExpect(newBox.findCoords(), new Posn(1, 2));
  }

  // Tests the visitTrophy method of the MoveTrophyVisitor class
  boolean testVisitTrophyMoveTrophyVisitorDown(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "down");
    Trophy trophy2 = new Trophy(new Posn(2, 1), new Color(1));

    ICell newBox = visitor.visitTrophy(trophy);

    return t.checkExpect(newBox, trophy);
  }

  // Tests the visitHole method of the MoveTrophyVisitor class
  boolean testVisitHoleMoveTrophyVisitorrDown(Tester t) {
    Trophy trophy = new Trophy(new Posn(1, 1), new Color(1));
    MoveTrophyVisitor visitor = new MoveTrophyVisitor(trophy, "down");
    Hole hole = new Hole(new Posn(2, 1));

    ICell newCell = visitor.visitHole(hole);

    return t.checkExpect(newCell, new Blank(new Posn(2, 1)));
  }

  // tests for visitBlank method of the CanMoveToVisitor
  boolean testVisitBlankCanMoveToVisitor(Tester t) {
    CanMoveToVisitor visitor = new CanMoveToVisitor();
    Blank blank = new Blank(new Posn(0, 0));
    return t.checkExpect(blank.accept(visitor), true);
  }

  // tests for visitWall method of the CanMoveToVisitor
  boolean testVisitWallCanMoveToVisitor(Tester t) {
    CanMoveToVisitor visitor = new CanMoveToVisitor();
    Wall wall = new Wall(new Posn(0, 0));
    return t.checkExpect(wall.accept(visitor), false);
  }

  // tests for visitBox method of the CanMoveToVisitor
  boolean testVisitBoxCanMoveToVisitor(Tester t) {
    CanMoveToVisitor visitor = new CanMoveToVisitor();
    Box box = new Box(new Posn(0, 0));
    return t.checkExpect(box.accept(visitor), false);
  }

  // tests for visitPlayer method of the CanMoveToVisitor
  boolean testVisitPlayerCanMoveToVisitor(Tester t) {
    CanMoveToVisitor visitor = new CanMoveToVisitor();
    Player player = new Player(new Posn(0, 0));
    return t.checkExpect(player.accept(visitor), false);
  }

  // tests for visitTarget method of the CanMoveToVisitor
  boolean testVisitTargetCanMoveToVisitor(Tester t) {
    CanMoveToVisitor visitor = new CanMoveToVisitor();
    Target target = new Target(new Posn(0, 0), new Color(1));
    return t.checkExpect(target.accept(visitor), true);
  }

  // tests for visitTrophy method of the CanMoveToVisitor
  boolean testVisitTrophyCanMoveToVisitor(Tester t) {
    CanMoveToVisitor visitor = new CanMoveToVisitor();
    Trophy trophy = new Trophy(new Posn(0, 0), new Color(1));
    return t.checkExpect(trophy.accept(visitor), false);
  }

  // tests for visitHole method of the CanMoveToVisitor
  boolean testVisitHoleCanMoveToVisitor(Tester t) {
    CanMoveToVisitor visitor = new CanMoveToVisitor();
    Hole hole = new Hole(new Posn(0, 0));
    return t.checkExpect(hole.accept(visitor), true);
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