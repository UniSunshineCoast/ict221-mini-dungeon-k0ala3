/*
 * GameEngine.java
 * Author: Max Faulks
 * 30 / 05 / 25
 * Portions of this script were generated or assisted by OpenAI's ChatGPT.
*/

package dungeon.engine;
import java.util.*;


// Base Item interface
interface Item {
    String getSymbol();
    String getName();
    void interact(Player player, GameEngine engine);
}

// Player class
class Player implements Item {
    private int hp;
    private int score;
    private int x, y;
    private final int maxHp = 10;

    public Player(int x, int y) {
        this.hp = 10;
        this.score = 0;
        this.x = x;
        this.y = y;
    }

    @Override
    public String getSymbol() { return "P"; }

    @Override
    public String getName() { return "Player"; }

    @Override
    public void interact(Player player, GameEngine engine) {
        // Player doesn't interact with itself
    }

    // Getters and setters
    public int getHp() { return hp; }
    public int getScore() { return score; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getMaxHp() { return maxHp; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    public void heal(int amount) {
        this.hp = Math.min(maxHp, this.hp + amount);
    }
}

// Entry point
class Entry implements Item {
    @Override
    public String getSymbol() { return "E"; }

    @Override
    public String getName() { return "Entry"; }

    @Override
    public void interact(Player player, GameEngine engine) {
        // Entry point - no interaction needed
    }
}

// Ladder
class Ladder implements Item {
    @Override
    public String getSymbol() { return "L"; }

    @Override
    public String getName() { return "Ladder"; }

    @Override
    public void interact(Player player, GameEngine engine) {
        engine.advanceLevel();
    }
}

// Gold
class Gold implements Item {
    @Override
    public String getSymbol() { return "G"; }

    @Override
    public String getName() { return "Gold"; }

    @Override
    public void interact(Player player, GameEngine engine) {
        player.addScore(2);
        engine.addMessage("You picked up gold! (+2 score)");
    }
}

// Health Potion
class HealthPotion implements Item {
    @Override
    public String getSymbol() { return "H"; }

    @Override
    public String getName() { return "Health Potion"; }

    @Override
    public void interact(Player player, GameEngine engine) {
        int oldHp = player.getHp();
        player.heal(4);
        int healedAmount = player.getHp() - oldHp;
        engine.addMessage("You drank a health potion! (+" + healedAmount + " HP)");
    }
}

// Trap
class Trap implements Item {
    @Override
    public String getSymbol() { return "T"; }

    @Override
    public String getName() { return "Trap"; }

    @Override
    public void interact(Player player, GameEngine engine) {
        player.takeDamage(2);
        engine.addMessage("You fell into a trap! (-2 HP)");
    }
}

// Melee Mutant
class MeleeMutant implements Item {
    @Override
    public String getSymbol() { return "M"; }

    @Override
    public String getName() { return "Melee Mutant"; }

    @Override
    public void interact(Player player, GameEngine engine) {
        player.takeDamage(2);
        player.addScore(2);
        engine.addMessage("You fought a melee mutant! (-2 HP, +2 score)");
    }
}

// Ranged Mutant
class RangedMutant implements Item {
    @Override
    public String getSymbol() { return "R"; }

    @Override
    public String getName() { return "Ranged Mutant"; }

    @Override
    public void interact(Player player, GameEngine engine) {
        player.addScore(2);
        engine.addMessage("You defeated a ranged mutant! (+2 score)");
    }
}

// Game state enum
enum GameState {
    PLAYING, WON, LOST
}

// Direction enum
enum Direction {
    UP(-1, 0), DOWN(1, 0), LEFT(0, -1), RIGHT(0, 1);

    private final int dx, dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
}

// Main Game Engine
public class GameEngine {
    protected Cell[][] map;
    protected Player player;
    protected int currentLevel;
    protected int difficulty;
    protected int steps;
    protected int maxSteps;
    protected GameState gameState;
    protected Random random;
    protected List<String> messages;

    // Entry positions for each level
    private int entryX, entryY;

    public GameEngine(int difficulty) {
        this.difficulty = Math.max(0, Math.min(10, difficulty)); // Clamp between 0-10
        this.maxSteps = 100;
        this.steps = 0;
        this.currentLevel = 1;
        this.gameState = GameState.PLAYING;
        this.random = new Random();
        this.messages = new ArrayList<>();

        initializeLevel();
    }

    private void initializeLevel() {
        map = new Cell[10][10];

        // Initialize all cells
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                map[i][j] = new Cell();
            }
        }

        // Create walls around the perimeter
        for (int i = 0; i < 10; i++) {
            map[0][i].setWall(true);
            map[9][i].setWall(true);
            map[i][0].setWall(true);
            map[i][9].setWall(true);
        }

        // Set entry position
        if (currentLevel == 1) {
            entryX = 9; // Bottom left (but accounting for walls, it's 8,1)
            entryY = 1;
        } else {
            // Level 2 entry is same as Level 1 ladder position
            entryX = 8;
            entryY = 8;
        }

        // Place entry
        map[entryX][entryY].setItem(new Entry());

        // Create and place player
        if (currentLevel == 1) {
            player = new Player(entryX, entryY);
        } else {
            player.setPosition(entryX, entryY);
        }

        // Place items randomly
        placeItemsRandomly();

        addMessage("Level " + currentLevel + " started! Difficulty: " +
                (difficulty + (currentLevel == 2 ? 2 : 0)));
    }

    private void placeItemsRandomly() {
        List<int[]> availablePositions = new ArrayList<>();

        // Find all available positions (not walls, not entry)
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                if (!(i == entryX && j == entryY)) {
                    availablePositions.add(new int[]{i, j});
                }
            }
        }

        Collections.shuffle(availablePositions, random);
        int index = 0;

        // Place ladder
        int[] pos = availablePositions.get(index++);
        map[pos[0]][pos[1]].setItem(new Ladder());

        // Place items based on difficulty
        int actualDifficulty = difficulty + (currentLevel == 2 ? 2 : 0);

        // Place traps (5)
        for (int i = 0; i < 5 && index < availablePositions.size(); i++) {
            pos = availablePositions.get(index++);
            map[pos[0]][pos[1]].setItem(new Trap());
        }

        // Place gold (5)
        for (int i = 0; i < 5 && index < availablePositions.size(); i++) {
            pos = availablePositions.get(index++);
            map[pos[0]][pos[1]].setItem(new Gold());
        }

        // Place melee mutants (3)
        for (int i = 0; i < 3 && index < availablePositions.size(); i++) {
            pos = availablePositions.get(index++);
            map[pos[0]][pos[1]].setItem(new MeleeMutant());
        }

        // Place ranged mutants (difficulty based)
        int rangedMutants = Math.min(actualDifficulty, availablePositions.size() - index);
        for (int i = 0; i < rangedMutants && index < availablePositions.size(); i++) {
            pos = availablePositions.get(index++);
            map[pos[0]][pos[1]].setItem(new RangedMutant());
        }

        // Place health potions (2)
        for (int i = 0; i < 2 && index < availablePositions.size(); i++) {
            pos = availablePositions.get(index++);
            map[pos[0]][pos[1]].setItem(new HealthPotion());
        }
    }

    public boolean movePlayer(Direction direction) {
        if (gameState != GameState.PLAYING) {
            return false;
        }

        int newX = player.getX() + direction.getDx();
        int newY = player.getY() + direction.getDy();

        // Check bounds
        if (newX < 0 || newX >= 10 || newY < 0 || newY >= 10) {
            addMessage("You tried to move " + direction.name().toLowerCase() +
                    " but hit the boundary.");
            return false;
        }

        // Check wall
        if (map[newX][newY].isWall()) {
            addMessage("You tried to move " + direction.name().toLowerCase() +
                    " but hit a wall.");
            return false;
        }

        // Move player
        player.setPosition(newX, newY);
        steps++;

        // Check for ranged mutant attacks
        checkRangedMutantAttacks();

        // Interact with item at new position
        Item item = map[newX][newY].getItem();
        if (item != null && !(item instanceof Entry)) {
            item.interact(player, this);

            // Remove item if it should be consumed
            if (item instanceof Gold || item instanceof HealthPotion ||
                    item instanceof MeleeMutant || item instanceof RangedMutant) {
                map[newX][newY].setItem(null);
            }
        }

        addMessage("You moved " + direction.name().toLowerCase() + " one step.");

        // Check game end conditions
        checkGameEnd();

        return true;
    }

    private void checkRangedMutantAttacks() {
        int px = player.getX();
        int py = player.getY();

        // Check for ranged mutants within 2 tiles horizontally or vertically
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Item item = map[i][j].getItem();
                if (item instanceof RangedMutant) {
                    int distance = Math.abs(i - px) + Math.abs(j - py);
                    boolean inRange = (Math.abs(i - px) <= 2 && j == py) ||
                            (Math.abs(j - py) <= 2 && i == px);

                    if (inRange && distance <= 2 && distance > 0) {
                        // 50% chance to attack
                        if (random.nextBoolean()) {
                            player.takeDamage(2);
                            addMessage("A ranged mutant attacked and you lost 2 HP!");
                        } else {
                            addMessage("A ranged mutant attacked, but missed!");
                        }
                    }
                }
            }
        }
    }

    private void checkGameEnd() {
        // Check if player died
        if (player.getHp() <= 0) {
            gameState = GameState.LOST;
            addMessage("You died! Game Over.");
            return;
        }

        // Check if max steps reached
        if (steps >= maxSteps) {
            gameState = GameState.LOST;
            addMessage("You ran out of steps! Game Over.");
            return;
        }
    }

    public void advanceLevel() {
        if (currentLevel == 1) {
            currentLevel = 2;
            difficulty += 2;
            addMessage("Advancing to Level 2!");
            initializeLevel();
        } else {
            // Won the game
            gameState = GameState.WON;
            addMessage("Congratulations! You escaped the dungeon!");
        }
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    // Getters
    public Cell[][] getMap() { return map; }
    public Player getPlayer() { return player; }
    public int getCurrentLevel() { return currentLevel; }
    public int getDifficulty() { return difficulty; }
    public int getSteps() { return steps; }
    public int getMaxSteps() { return maxSteps; }
    public GameState getGameState() { return gameState; }
    public List<String> getMessages() { return new ArrayList<>(messages); }
    public int getSize() { return 10; }

    // For testing purposes
    public static void main(String[] args) {
        GameEngine engine = new GameEngine(3);
        Scanner scanner = new Scanner(System.in);

        System.out.println("MiniDungeon Text Interface");
        System.out.println("Commands: u (up), d (down), l (left), r (right), q (quit)");

        // Display initial state
        displayGameState(engine);

        while (engine.getGameState() == GameState.PLAYING) {
            System.out.print("Enter command: ");
            String input = scanner.nextLine().toLowerCase().trim();

            // Clear previous messages for this turn
            List<String> messagesBefore = new ArrayList<>(engine.getMessages());

            switch (input) {
                case "u":
                    engine.movePlayer(Direction.UP);
                    break;
                case "d":
                    engine.movePlayer(Direction.DOWN);
                    break;
                case "l":
                    engine.movePlayer(Direction.LEFT);
                    break;
                case "r":
                    engine.movePlayer(Direction.RIGHT);
                    break;
                case "q":
                    System.out.println("Thanks for playing!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid command. Use u/d/l/r or q to quit.");
                    continue;
            }

            // Display new messages
            List<String> messagesAfter = engine.getMessages();
            for (int i = messagesBefore.size(); i < messagesAfter.size(); i++) {
                System.out.println(">>> " + messagesAfter.get(i));
            }

            // Display updated game state
            displayGameState(engine);
        }

        // Game ended
        if (engine.getGameState() == GameState.WON) {
            System.out.println("\n🎉 YOU WON! Final score: " + engine.getPlayer().getScore());
        } else {
            System.out.println("\n💀 GAME OVER! Final score: " + engine.getPlayer().getScore());
        }

        scanner.close();
    }

    private static void displayGameState(GameEngine engine) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("Level " + engine.getCurrentLevel() +
                " | HP: " + engine.getPlayer().getHp() + "/" + engine.getPlayer().getMaxHp() +
                " | Score: " + engine.getPlayer().getScore() +
                " | Steps: " + engine.getSteps() + "/" + engine.getMaxSteps());
        System.out.println("Player position: (" + engine.getPlayer().getX() + "," + engine.getPlayer().getY() + ")");
        System.out.println("=".repeat(40));
    }
}
