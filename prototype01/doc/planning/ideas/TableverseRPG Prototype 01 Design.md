# TableverseRPG — Prototype 01

## Purpose

Determine whether libGDX offers a better foundation for TableverseRPG's game-world display, mouse controls, and on-screen information than the approach used in Open Fields 2. This prototype is an interaction experiment, not a rewrite of the RPG. Its result should be a small playable build and a decision about whether to continue with libGDX.

## Questions to answer

1. Is selecting a character and giving a movement order clear and responsive?
2. Can the map, characters, selection feedback, and a character status panel coexist without awkward input conflicts?
3. Is the code for rendering and input easier to change than the corresponding code in Open Fields 2?
4. Can game state stay independent of libGDX so that it can run and be inspected without the UI?

## Player experience

Launch into one small, fixed room viewed from above. Two friendly characters are shown as distinct, labeled circles. Walls or obstacles make movement visible. Left-click a character to select it; the selected character gains a visible outline, and a panel displays its name, position, and current order. Right-click a reachable point in the room to order the selected character to move there. As it moves, its status changes from *Idle* to *Moving* and back to *Idle* on arrival. The player can switch selection while either character moves. A pause control freezes simulation updates while leaving the interface responsive.

For this prototype, a click on the panel never selects a character or issues a world order. A click on empty world space clears selection; a right-click with no selection does nothing. An unreachable destination leaves the character's current order unchanged and provides brief visual feedback.

## Scope

### Required

- One fixed, single-screen map with a walkable area and a few blocking obstacles.
- Two selectable, independently moving characters, rendered with simple shapes and labels. No art pipeline is needed.
- Left-click selection, right-click movement orders, visible destination marker and selection feedback.
- Obstacle-aware movement. A simple grid and breadth-first search or A* is sufficient; movement may be cell-based or interpolated between cells.
- A status panel showing the selected character's name, current position, and current order, plus a pause/resume control.
- Window resizing that preserves usable map and panel layouts and correct click targeting.
- A simulation model that has no libGDX imports, with UI input translated into explicit select/order/pause actions and rendered from a read-only view of state.

### Explicitly deferred

- Combat, inventory, dialogue, quests, persistence, procedural maps, multiplayer, polished art, sound, and character creation.
- A scenario editor or web frontend. Prototype 01 tests the playable interface first.
- General-purpose entity systems or plugin architecture. Add abstractions only when the experiment needs them.

## Proposed implementation

Use a desktop-only libGDX project generated with the current official setup tool. Keep three conceptual parts:

| Part | Responsibility |
| --- | --- |
| Simulation | Map, character state, orders, pathfinding, and fixed-step updates; plain Java with no libGDX dependency. |
| World view and controls | Draw the map and characters, convert screen clicks to world coordinates, and translate clicks into simulation actions. |
| HUD | A Scene2D `Stage` for the status panel and pause control. It reads state but does not own character state. |

Give the HUD first opportunity to consume pointer input, then send unhandled events to the world controls. Keep a separate world camera/viewport from the HUD viewport. Recalculate both on resize and verify that selection and movement clicks still land in the correct place.

Use a fixed simulation step so movement speed does not depend on rendering frame rate. The rendering code may interpolate positions for smooth motion, but the simulation owns positions and orders. Avoid making Scene2D `Actor` instances the simulation's characters.

## Build sequence

1. **Window and map:** Draw the room, obstacles, and two labeled circles. Resize the window and check coordinates.
2. **Selection and panel:** Add click selection, outline, status display, and clearing selection. Check that clicking the panel does not select the world beneath it.
3. **Orders and movement:** Add right-click orders, obstacle-aware paths, destination feedback, and current-order display.
4. **Pause and polish:** Add pause/resume, unreachable-target feedback, and readable visual states. Try rapid selection changes and repeated orders.
5. **Evaluate:** Compare the experience of implementing and modifying this screen with Open Fields 2; record concrete friction points and decide what Prototype 02 should test.

## Acceptance checks

- A user can launch the desktop build and operate it with a mouse without reading instructions beyond a short on-screen hint: “Left-click select · Right-click move.”
- Both characters can be selected and ordered independently; orders to accessible destinations complete without passing through blocked cells.
- Selection, destination, and current order are visibly distinguishable; idle, moving, and paused states are clear.
- UI clicks do not produce map actions. Map clicks continue to target correctly after resizing and while the camera/viewport is active.
- Pausing stops movement but does not freeze buttons or selection. Resuming continues the pending order.
- The simulation can be instantiated and advanced from ordinary Java code without opening a libGDX window.

## Decision after Prototype 01

Write a short retrospective with three observations: what libGDX simplified, what remained awkward, and what UI behavior should change. Continue with libGDX if the map and panel are straightforward to evolve and input boundaries stay reliable. If the main difficulty is hand-building screens or visual layouts, compare a small version of the same interaction in an editor-oriented engine before committing further. The next prototype should follow the result of this experiment rather than a predetermined feature roadmap.
