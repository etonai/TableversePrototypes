package com.tableverse.prototype01.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the simulation as ordinary Java code, with no libGDX window,
 * covering acceptance check 6 and the core select/order/pause behaviors.
 */
class SimulationTest {

    @Test
    void instantiatesAndStartsWithTwoIdleCharacters() {
        Simulation sim = new Simulation();
        assertEquals(2, sim.getCharacters().size());
        for (SimCharacter c : sim.getCharacters()) {
            assertEquals(CharacterState.IDLE, c.getState());
        }
    }

    @Test
    void clickOnCharacterSelectsIt_clickOnEmptySpaceClearsSelection() {
        Simulation sim = new Simulation();
        SimCharacter alpha = sim.getCharacter("alpha");

        boolean selected = sim.selectAt(alpha.getX(), alpha.getY(), 22f);
        assertTrue(selected);
        assertEquals("alpha", sim.getSelectedCharacterId());

        boolean selectedEmpty = sim.selectAt(-1000, -1000, 22f);
        assertFalse(selectedEmpty);
        assertNull(sim.getSelectedCharacterId());
    }

    @Test
    void rightClickWithNoSelectionDoesNothing() {
        Simulation sim = new Simulation();
        boolean accepted = sim.issueMoveOrder(300, 300);
        assertFalse(accepted);
        for (SimCharacter c : sim.getCharacters()) {
            assertEquals(CharacterState.IDLE, c.getState());
        }
    }

    @Test
    void reachableOrderMovesCharacterAroundObstacles() {
        Simulation sim = new Simulation();
        SimCharacter alpha = sim.getCharacter("alpha");
        sim.selectAt(alpha.getX(), alpha.getY(), 22f);

        // Destination is on the far side of the obstacle wall (blocked column at grid x=5).
        float destX = sim.getMap().gridCenterX(9);
        float destY = sim.getMap().gridCenterY(1);
        boolean accepted = sim.issueMoveOrder(destX, destY);
        assertTrue(accepted);
        assertEquals(CharacterState.MOVING, alpha.getState());

        for (int i = 0; i < 10000 && alpha.getState() == CharacterState.MOVING; i++) {
            sim.update(Simulation.FIXED_STEP);
        }

        assertEquals(CharacterState.IDLE, alpha.getState());
        assertEquals(destX, alpha.getX(), 0.5f);
        assertEquals(destY, alpha.getY(), 0.5f);
    }

    @Test
    void unreachableOrderLeavesCurrentOrderUnchanged() {
        Simulation sim = new Simulation();
        SimCharacter alpha = sim.getCharacter("alpha");
        sim.selectAt(alpha.getX(), alpha.getY(), 22f);

        // blocked[8][6] and blocked[8][7] are solid obstacle cells with no walkable interior.
        float blockedX = sim.getMap().gridCenterX(8);
        float blockedY = sim.getMap().gridCenterY(6);
        boolean accepted = sim.issueMoveOrder(blockedX, blockedY);

        assertFalse(accepted);
        assertEquals(CharacterState.IDLE, alpha.getState());
        assertFalse(alpha.hasDestination());
    }

    @Test
    void pauseStopsUpdatesAndResumeContinuesPendingOrder() {
        Simulation sim = new Simulation();
        SimCharacter alpha = sim.getCharacter("alpha");
        sim.selectAt(alpha.getX(), alpha.getY(), 22f);
        sim.issueMoveOrder(sim.getMap().gridCenterX(3), sim.getMap().gridCenterY(1));

        sim.setPaused(true);
        float xBefore = alpha.getX();
        float yBefore = alpha.getY();
        for (int i = 0; i < 30; i++) {
            sim.update(Simulation.FIXED_STEP);
        }
        assertEquals(xBefore, alpha.getX(), 0.001f);
        assertEquals(yBefore, alpha.getY(), 0.001f);
        assertEquals(CharacterState.MOVING, alpha.getState());

        sim.setPaused(false);
        for (int i = 0; i < 10000 && alpha.getState() == CharacterState.MOVING; i++) {
            sim.update(Simulation.FIXED_STEP);
        }
        assertEquals(CharacterState.IDLE, alpha.getState());
    }

    @Test
    void rapidSelectionChangesLeaveExactlyOneOrNoCharacterSelected() {
        Simulation sim = new Simulation();
        SimCharacter alpha = sim.getCharacter("alpha");
        SimCharacter beta = sim.getCharacter("beta");

        for (int i = 0; i < 50; i++) {
            sim.selectAt(alpha.getX(), alpha.getY(), 22f);
            assertEquals("alpha", sim.getSelectedCharacterId());
            sim.selectAt(beta.getX(), beta.getY(), 22f);
            assertEquals("beta", sim.getSelectedCharacterId());
            sim.selectAt(-1000, -1000, 22f);
            assertNull(sim.getSelectedCharacterId());
        }
    }

    @Test
    void repeatedOrdersReplacePreviousPendingPath() {
        Simulation sim = new Simulation();
        SimCharacter alpha = sim.getCharacter("alpha");
        sim.selectAt(alpha.getX(), alpha.getY(), 22f);

        assertTrue(sim.issueMoveOrder(sim.getMap().gridCenterX(9), sim.getMap().gridCenterY(1)));
        sim.update(Simulation.FIXED_STEP * 5);
        assertEquals(CharacterState.MOVING, alpha.getState());

        float finalDestX = sim.getMap().gridCenterX(1);
        float finalDestY = sim.getMap().gridCenterY(1);
        assertTrue(sim.issueMoveOrder(finalDestX, finalDestY));

        for (int i = 0; i < 10000 && alpha.getState() == CharacterState.MOVING; i++) {
            sim.update(Simulation.FIXED_STEP);
        }
        assertEquals(CharacterState.IDLE, alpha.getState());
        assertEquals(finalDestX, alpha.getX(), 0.5f);
        assertEquals(finalDestY, alpha.getY(), 0.5f);
    }

    @Test
    void switchingSelectionWhileOtherCharacterMovesDoesNotAffectIt() {
        Simulation sim = new Simulation();
        SimCharacter alpha = sim.getCharacter("alpha");
        SimCharacter beta = sim.getCharacter("beta");

        sim.selectAt(alpha.getX(), alpha.getY(), 22f);
        sim.issueMoveOrder(sim.getMap().gridCenterX(3), sim.getMap().gridCenterY(1));
        assertEquals(CharacterState.MOVING, alpha.getState());

        sim.selectAt(beta.getX(), beta.getY(), 22f);
        assertEquals("beta", sim.getSelectedCharacterId());
        assertNotNull(sim.getSelectedCharacter());

        for (int i = 0; i < 10000 && alpha.getState() == CharacterState.MOVING; i++) {
            sim.update(Simulation.FIXED_STEP);
        }
        assertEquals(CharacterState.IDLE, alpha.getState());
        assertEquals(CharacterState.IDLE, beta.getState());
    }
}
