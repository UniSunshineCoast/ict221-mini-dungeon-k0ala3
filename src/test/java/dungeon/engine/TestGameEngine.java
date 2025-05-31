/*
 * TestGameEngine.java
 * Author: Max Faulks
 * 30 / 05 / 25
 * Portions of this script were generated or assisted by OpenAI's ChatGPT.
 */

package dungeon.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestGameEngine {

    @Test
    void testGetSize() {
        GameEngine ge = new GameEngine(10);
        assertEquals(10, ge.getSize());
    }

    @Test
    void testInitialPlayerPosition() {
        GameEngine ge = new GameEngine(5);
        Player p = ge.getPlayer();
        assertEquals(9, p.getX());
        assertEquals(1, p.getY());
    }

    @Test
    void testMoveIntoWall() {
        GameEngine ge = new GameEngine(5);
        // Player starts at (9,1), moving down hits wall
        boolean moved = ge.movePlayer(Direction.DOWN);
        assertFalse(moved);
    }

    @Test
    void testMoveValidDirection() {
        GameEngine ge = new GameEngine(5);
        // Move up from (9,1) to (8,1)
        boolean moved = ge.movePlayer(Direction.UP);
        assertTrue(moved);
        assertEquals(8, ge.getPlayer().getX());
        assertEquals(1, ge.getPlayer().getY());
    }

    @Test
    void testScoreIncreasesAfterPickingGold() {
        GameEngine ge = new GameEngine(5);

        // Place gold manually at (8,1)
        ge.getMap()[8][1].setItem(new dungeon.engine.Gold());

        // Move player to (8,1)
        ge.movePlayer(Direction.UP);

        assertTrue(ge.getPlayer().getScore() >= 2); // Gold adds 2
    }

    @Test
    void testHealthPotionHealsPlayer() {
        GameEngine ge = new GameEngine(5);
        Player player = ge.getPlayer();
        player.takeDamage(4); // HP goes from 10 to 6

        // Place potion at (8,1)
        ge.getMap()[8][1].setItem(new dungeon.engine.HealthPotion());

        ge.movePlayer(Direction.UP); // Pick up potion
        assertTrue(player.getHp() > 6);
    }

    @Test
    void testTrapDamagesPlayer() {
        GameEngine ge = new GameEngine(5);
        Player player = ge.getPlayer();
        int initialHp = player.getHp();

        // Place trap at (8,1)
        ge.getMap()[8][1].setItem(new dungeon.engine.Trap());

        ge.movePlayer(Direction.UP); // Step on trap
        assertEquals(initialHp - 2, player.getHp());
    }

    @Test
    void testAdvanceLevel() {
        GameEngine ge = new GameEngine(3);
        Player p = ge.getPlayer();

        // Force ladder at (8,1)
        ge.getMap()[8][1].setItem(new dungeon.engine.Ladder());

        ge.movePlayer(Direction.UP); // Triggers level advancement

        assertEquals(2, ge.getCurrentLevel());
    }

    @Test
    void testGameWinCondition() {
        GameEngine ge = new GameEngine(3);

        // Simulate reaching ladder in level 1
        ge.getMap()[8][1].setItem(new dungeon.engine.Ladder());
        ge.movePlayer(Direction.UP);

        // Simulate reaching ladder in level 2
        ge.getMap()[8][8].setItem(new dungeon.engine.Ladder());
        ge.getPlayer().setPosition(7, 8); // Move player near the ladder
        ge.movePlayer(Direction.DOWN); // Should win

        assertEquals(GameState.WON, ge.getGameState());
    }

    @Test
    void testGameLoseByDeath() {
        GameEngine ge = new GameEngine(5);
        Player p = ge.getPlayer();
        p.takeDamage(10);
        ge.movePlayer(Direction.UP); // Trigger checkGameEnd()

        assertEquals(GameState.LOST, ge.getGameState());
    }

    @Test
    void testGameLoseBySteps() {
        GameEngine ge = new GameEngine(5);

        // Exhaust max steps
        for (int i = 0; i < ge.getMaxSteps(); i++) {
            // Move up and down repeatedly
            ge.movePlayer(Direction.UP);
            ge.movePlayer(Direction.DOWN);
        }

        assertEquals(GameState.LOST, ge.getGameState());
    }
}
