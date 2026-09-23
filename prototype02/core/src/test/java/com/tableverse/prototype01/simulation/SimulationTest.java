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
    void characterNeverEntersBlockedCellWhileReactivelyRoutingAroundObstacle() {
        Simulation sim = new Simulation();
        SimCharacter alpha = sim.getCharacter("alpha");
        sim.selectAt(alpha.getX(), alpha.getY(), 22f);

        // Straight line from Alpha's start to this destination is blocked by the wall at grid x=5
        // (rows 2,3,5 are blocked; row 4 is the only gap), so reaching it requires the per-tick
        // reactive logic (SimCharacter.update) to detect the blocked line of sight and recalculate
        // a route through the gap, tick after tick, rather than clipping straight through the wall.
        float destX = sim.getMap().gridCenterX(9);
        float destY = sim.getMap().gridCenterY(1);
        assertTrue(sim.issueMoveOrder(destX, destY));

        for (int i = 0; i < 10000 && alpha.getState() == CharacterState.MOVING; i++) {
            sim.update(Simulation.FIXED_STEP);
            GridPoint cell = sim.getMap().worldToGrid(alpha.getX(), alpha.getY());
            assertTrue(sim.getMap().isWalkable(cell.x, cell.y),
                    "character entered blocked cell " + cell + " at (" + alpha.getX() + ", " + alpha.getY() + ")");
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
    void unobstructedOrderMovesDiagonallyInsteadOfAxisAlignedSegments() {
        Simulation sim = new Simulation();
        SimCharacter beta = sim.getCharacter("beta");
        sim.selectAt(beta.getX(), beta.getY(), 22f);

        // (10,7) -> (7,4) is a clear diagonal (no obstacles on that line); straight-line distance
        // is 192*sqrt(2) world units. A cardinal-only path would instead cover 192+192 = 384 units.
        float destX = sim.getMap().gridCenterX(7);
        float destY = sim.getMap().gridCenterY(4);
        assertTrue(sim.issueMoveOrder(destX, destY));

        float straightLineSeconds = (float) (192 * Math.sqrt(2)) / SimCharacter.SPEED;
        int straightLineSteps = (int) Math.ceil(straightLineSeconds / Simulation.FIXED_STEP);

        // Give it a little headroom over the direct-path time, but nowhere near the ~2x time an
        // axis-aligned (up/down/left/right only) path would take.
        int stepBudget = straightLineSteps + 5;
        for (int i = 0; i < stepBudget && beta.getState() == CharacterState.MOVING; i++) {
            sim.update(Simulation.FIXED_STEP);
        }

        assertEquals(CharacterState.IDLE, beta.getState());
        assertEquals(destX, beta.getX(), 0.5f);
        assertEquals(destY, beta.getY(), 0.5f);
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
