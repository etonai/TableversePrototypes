# DevCycle 002: Camera Zoom and Direct-Path Movement

**Status:** VERIFIED
**Start Date:** 2026-09-22
**Target Completion:** TBD
**Focus:** Add world-camera zoom that leaves the HUD/status panel unaffected, and replace axis-aligned grid movement with direct movement toward the ordered destination.

---

## Goal

Prototype 02 inherits Prototype 01's libGDX implementation as-is. Two gaps stand out when using it: the world view cannot be zoomed, and characters only ever move along the grid's four cardinal directions (a consequence of `Pathfinder`'s 4-neighbor BFS and `SimCharacter` walking waypoint-to-waypoint), producing blocky, staircase-like movement instead of traveling straight toward where they were ordered to go. This cycle addresses both.

## Desired Outcome

- The player can zoom the world view in and out (e.g. mouse scroll wheel, or +/- keys) while the HUD status panel and its text stay a fixed on-screen size.
- Ordered characters move in a straight line toward their destination whenever the direct path is unobstructed, instead of only moving in cardinal-direction segments. Obstacle-aware routing is preserved — movement should still go around obstacles when a straight line isn't walkable.
- The player can pan the world view left/right/up/down (right-click-drag) instead of the camera always staying centered on the room.

---

## Tasks

### Phase 1: Camera Zoom

**Status:** Work Complete

- [x] Add zoom input handling to the world view (mouse scroll wheel at minimum; keyboard +/- optional) that adjusts `worldCamera.zoom` on the existing `OrthographicCamera`/`FitViewport`.
- [x] Clamp zoom to a sensible min/max range so the room can't be zoomed out past its bounds or in to the point of losing context.
- [x] Verify the HUD (`GameHud`'s `Stage`/`ScreenViewport`) is unaffected by world-camera zoom — it already uses a separate viewport from the world, so this should hold, but confirm no shared camera/viewport state leaks between them.
- [x] Verify zoom does not break screen-to-world coordinate mapping used for character selection and move orders (`WorldInputProcessor`'s `Viewport.unproject`).

**Technical Notes:**
`WorldInputProcessor.scrolled(float amountX, float amountY)` adjusts `worldCamera.zoom` by `ZOOM_STEP = 0.1f` per scroll notch, clamped to `[MIN_ZOOM = 0.5f, MAX_ZOOM = 2f]`, and calls `worldCamera.update()` immediately so the projection is current for the next `touchDown`'s `worldViewport.unproject` (rather than waiting a frame for `worldViewport.apply()`). `WorldInputProcessor` now takes the `OrthographicCamera` as a constructor parameter (`Prototype01Game.create()` passes `worldCamera`). `GameHud` was not touched — it owns its own `Stage`/`ScreenViewport` and never references `worldCamera`, so the HUD's on-screen size is unaffected by construction, confirmed by code inspection (no shared camera/viewport state exists between the two). Screen-to-world mapping stays correct because `Viewport.unproject` reads the camera's current `combined` matrix, which reflects the post-scroll zoom once `camera.update()` has run.

### Phase 2: Direct-Path Movement

**Status:** Work Complete

- [x] Replace or extend `Pathfinder.findPath` (currently 4-directional BFS, `Pathfinder.java`) so a character can move in a straight line to the destination when unobstructed, rather than only stepping through cardinal-adjacent grid cells.
- [x] Preserve obstacle-aware routing for cases where a straight line to the destination is blocked (the room still has walls/obstacles to route around).
- [x] Update `SimCharacter.update`'s waypoint-following (currently walks grid-cell centers one at a time) as needed to support non-cardinal, non-grid-aligned motion.
- [x] Re-verify existing pathing/movement behavior covered by `SimulationTest` (reachable-order-around-obstacles, unreachable-leaves-order-unchanged, repeated-orders-replace-pending-path) still holds with the new movement approach.

**Technical Notes:**
Went with the recommended line-of-sight shortcutting approach ("string pulling"), kept the existing 4-directional BFS (`Pathfinder`, unchanged) purely for reachability/routing, and added a smoothing pass on top:
- `GameMap.hasLineOfSight(x0, y0, x1, y1)` samples points along a straight line at `CELL_SIZE / 4` intervals and returns false if any sampled point falls in a blocked cell.
- New `PathSmoother.smooth(map, startX, startY, gridPath)` greedily walks the BFS grid path from the character's actual position, at each step jumping to the furthest grid-cell center still in a clear line of sight, producing a much shorter list of world-space `Waypoint`s instead of one waypoint per grid cell.
- New `Waypoint` (immutable `float x, y`) replaces `GridPoint` as `SimCharacter`'s path representation; `SimCharacter.setPath(GameMap, List<GridPoint>)` became `setWaypoints(List<Waypoint>)`, and `update(GameMap, float)` became `update(float)` (no longer needs the map — it just walks straight toward the next waypoint, exactly as before but toward an arbitrary point instead of a grid-cell center).
- `Simulation.issueMoveOrder` now runs `Pathfinder.findPath` (BFS, for reachability and obstacle routing) then `PathSmoother.smooth` (for the actual movement waypoints) before calling `selected.setWaypoints(...)`.
- All 8 existing `SimulationTest` cases pass unchanged (`.\gradlew.bat test`, `BUILD SUCCESSFUL`). Added a new test, `unobstructedOrderMovesDiagonallyInsteadOfAxisAlignedSegments`, which orders Beta along a clear diagonal and asserts it arrives in roughly the straight-line time (`192*sqrt(2)/SPEED`) rather than the ~41% longer axis-aligned (Manhattan) time a cardinal-only path would take — this fails under the old implementation and passes under the new one, directly demonstrating the fix.

### Phase 3: Refine Zoom Range

**Status:** Work Complete

- [x] Widen the zoom range for more usable zoom headroom: `MIN_ZOOM` 0.5f → 0.2f, `MAX_ZOOM` 2f → 4f (`WorldInputProcessor`).
- [ ] User to playtest the new range, particularly at the extremes, and report anything that needs further tuning (e.g. `ZOOM_STEP` feel, character/HUD legibility when zoomed far out or in).

**Technical Notes:**
Straight constant change in `WorldInputProcessor` (`MIN_ZOOM`/`MAX_ZOOM`); `ZOOM_STEP` (0.1f per scroll notch) was left as-is, so reaching the new extremes takes more scroll notches than before (was 15 notches edge-to-edge, now 38). Worth watching whether that step size still feels right across the wider range once playtested.

One thing to note for zoom refinement generally: `WorldRenderer.PICK_RADIUS` (character click/select radius) is a fixed number of *world* units, not screen pixels. At low zoom values (zoomed in) it doesn't change apparent-size behavior, but at high zoom values (zoomed out toward `MAX_ZOOM = 4f`) the same world-unit pick radius covers proportionally fewer screen pixels, so selecting a character precisely may get harder the further out you zoom. Flagging this now in case it's part of what "refine zoom" ends up covering.

### Phase 4: Map Panning

**Status:** Work Complete

- [x] Add camera panning driven by right-click-and-drag: while the right mouse button is held and the pointer moves, translate `worldCamera.position` opposite the drag delta (converted from screen to world units, accounting for current zoom) instead of leaving the camera centered on the room.
- [x] Resolve the input conflict this creates: right-click currently issues a move order immediately on `touchDown` (`WorldInputProcessor.touchDown`, `Input.Buttons.RIGHT`). A drag-to-pan gesture also starts with a right-button press, so the two must be distinguished — most likely by treating a right-click as a *pan* once the pointer has moved more than a small screen-pixel threshold after `touchDown`, and only issuing the move order on `touchUp` if the total drag distance stayed under that threshold (i.e. it was a tap, not a drag).
- [x] Decide whether panning should be bounded (e.g. clamped so the room can't be dragged entirely off-screen) or fully free, per the user's "I don't want the map to always be centered" — default assumption is free panning unless the user says otherwise.
- [x] Verify panning composes correctly with zoom (drag distance in screen pixels should translate to more world units when zoomed out, fewer when zoomed in) and doesn't break `Viewport.unproject`-based selection/move-order accuracy.
- [x] Verify the HUD is unaffected by panning, same as it already is for zoom (separate `ScreenViewport`/`Stage`, never touches `worldCamera`).

**Technical Notes:**
Implemented per the recommended tap-vs-drag approach (Open Question 3) with fully free panning (Open Question 4). `WorldInputProcessor` right-click handling was restructured across `touchDown`/`touchDragged`/`touchUp`:
- `touchDown` (RIGHT) no longer issues a move order. It just records `rightButtonDown = true`, the down position, and resets `rightDragging = false` — no camera or simulation change yet.
- `touchDragged` only acts while `rightButtonDown`. It sets `rightDragging = true` once cumulative screen-pixel distance from the down position reaches `DRAG_THRESHOLD_PX = 8f`. Once dragging, each call unprojects the previous and current screen points to world space (via `worldViewport.unproject`, so it already accounts for zoom) and subtracts that world-space delta from `worldCamera.position` — the classic "grab and drag" convention, so the point under the cursor stays under the cursor. `worldCamera.update()` runs immediately after, same pattern as `scrolled()`, so `Viewport.unproject` stays correct for the very next event rather than lagging a frame.
- `touchUp` (RIGHT) only issues the move order if `rightDragging` never became true — i.e. it was a genuine click, not a drag — using the up-position (equal to the down-position within the 8px threshold). This is a from-scratch behavior change to the existing move-order trigger (previously fired on `touchDown`); re-verified manually that a plain right-click still selects-and-orders correctly.
- Left-click handling (`touchDown` LEFT) is untouched — selection still fires immediately on click, as before; `touchDragged` is a no-op unless `rightButtonDown`, so left-drag does nothing new.

**Known limitation (not addressed this phase):** `Prototype01Game.resize()` calls `worldViewport.update(width, height, true)`; the `true` (`centerCamera`) argument recenters `worldCamera.position` on every window resize, which will silently discard any panned offset if the user resizes the window mid-play. This pre-existing behavior wasn't part of what was asked for here, so it was left as-is — flagging it in case it's worth a future fix.

### Phase 5: Reactive (Recalculate-on-Obstruction) Movement

**Status:** Work Complete

- [x] Replace order-time path precomputation (was: `Simulation.issueMoveOrder` ran `Pathfinder.findPath` + `PathSmoother.smooth` once, up front, producing a full `Waypoint` list that `SimCharacter` then consumed) with a per-tick reactive check instead.
- [x] Each check: test direct line-of-sight (`GameMap.hasLineOfSight`) from the character's *current* position to its *final* destination.
  - If clear, move straight toward the destination — no stored path needed for that tick.
  - If blocked, recalculate: run `Pathfinder.findPath` + `PathSmoother.smooth` from the character's current position to the destination, and move toward the resulting first aim point.
- [x] Decide and implement the recalculation cadence (every tick vs. every N ticks) — expose as a tunable constant either way (see Open Question 5).
- [x] Keep an initial reachability check at order-issue time (a `Pathfinder.findPath` call whose result is used only for accept/reject, not for movement) so an unreachable click is still rejected immediately with feedback, rather than only discovered once the character starts walking and hits a dead end (see Open Question 6).
- [x] Preserve existing acceptance behavior: pause still freezes movement; repeated orders still replace the pending one; switching selection mid-move still doesn't affect the other character.
- [x] Re-verify `SimulationTest`, including `unobstructedOrderMovesDiagonallyInsteadOfAxisAlignedSegments` from Phase 2, still passes. Add a test that specifically exercises "moving directly, hits an obstacle, recalculates, goes around it" to cover the new reactive behavior Phase 2's test suite doesn't touch.

**Technical Notes:**
Implemented as scoped: every tick (per Open Question 5's recommendation), if `map.hasLineOfSight(x, y, destinationX, destinationY)` is true the character heads straight at the destination; if not, `SimCharacter.update` runs `Pathfinder.findPath` from the character's *current* position and takes the first element of `PathSmoother.smooth(...)` as that tick's aim point. No waypoint list is stored between ticks at all — `SimCharacter` dropped its `List<Waypoint> waypoints`/`nextWaypointIndex` fields entirely; `Simulation.issueMoveOrder` still runs one `Pathfinder.findPath` purely for the accept/reject decision (Open Question 6), then just calls `selected.startMoveOrder(worldX, worldY)` to set the destination and `MOVING` state.

**Bug found and fixed during implementation:** the first version of this got a character permanently stuck against an obstacle. Root cause was `GameMap.hasLineOfSight`'s original implementation — fixed-interval point sampling every `CELL_SIZE / 4` units — which could miss a thin corner-clip of a blocked cell when the sampled points happened to straddle it. This exact risk was already flagged as a theoretical, unobserved limitation in Phase 2/3's Notes and Risks; recomputing line-of-sight every tick from the character's continuously-changing floating-point position (rather than once, from a fixed order-issue position) made it dramatically more likely to actually occur, since the position sweeps through exactly the geometries that trigger it. Confirmed via a standalone debug harness (see below) tracing the character walking directly into the center of a blocked cell and stalling there permanently (its own position resolved to that blocked cell, so every subsequent line-of-sight check and pathfind treated its own location as the "start," producing a zero-distance "aim point" at itself, forever).

Fixed by replacing the point-sampling `hasLineOfSight` with an exact grid/voxel traversal (Amanatides–Woo-style DDA): it walks every grid cell a segment actually passes through, cell by cell, based on which grid line (x or y) is crossed next, so no cell along the path can be skipped regardless of segment length or angle. An exact corner crossing (passing precisely through a grid-line intersection) is treated as blocked if either flanking cell is blocked, matching the conservative choice a non-zero-radius character would need anyway. This fully closes the corner-clip gap noted as a risk since Phase 2 — that risk note has been superseded and can be considered resolved.

Debugging method: a temporary same-package standalone harness (`Debug.java`, `Debug2.java`, compiled directly against `core/build/classes/java/main` and run outside Gradle) was used to trace exact per-tick position/cell/line-of-sight/aim-point values and pinpoint the corner-clip; both files were scratch-only and were not added to the project.

All 11 `SimulationTest` cases pass (`.\gradlew.bat test`, `BUILD SUCCESSFUL`), including the new `characterNeverEntersBlockedCellWhileReactivelyRoutingAroundObstacle`, which asserts the character's grid cell is walkable after every single simulated tick while routing around the standalone obstacle at grid (2,1) — the same scenario that surfaced the bug above.

---

## Open Questions

1. **Which direct-movement approach: line-of-sight shortcutting, 8-directional grid pathing, or continuous movement with obstacle checks?**
   Recommendation: Start with line-of-sight shortcutting on top of the existing BFS path — smallest change, keeps the grid model and existing tests mostly intact, and directly fixes the "moves in cardinal segments" complaint without a bigger movement-model rewrite.

2. **Zoom input: scroll wheel only, or also keyboard/UI controls?**
   Recommendation: Scroll wheel only for this cycle; it's the standard, minimal-effort interaction, and keyboard/UI zoom controls can be added later if needed.

3. **Tap-vs-drag threshold for right-click: how many screen pixels of movement should count as "dragging" (pan) rather than "clicking" (move order)?**
   Recommendation: A small fixed threshold (e.g. 8–10 screen pixels), checked in `touchUp` against total distance from the `touchDown` origin. Large enough to absorb incidental mouse jitter on a genuine click, small enough that an intentional pan is never mistaken for a click.

4. **Should panning be bounded to the room, or fully free?**
   Recommendation: Fully free for this prototype phase — matches the user's explicit "I don't want the map to always be centered," and bounding can be added later if free panning proves disorienting in practice.

5. **Recalculation cadence for Phase 5: every tick, or every few ticks?**
   Recommendation: Check line-of-sight every tick — it's a cheap sampled raycast (`hasLineOfSight`), and checking it every tick means the character reacts to an obstruction the instant it appears rather than up to a few ticks late. The comparatively more expensive BFS re-path only runs on the (rarer) tick where that check actually fails, so per-tick cost stays low in the common unobstructed case; a "few ticks" cadence would only be worth it if profiling showed the per-tick check itself was a problem, which is unlikely at this room's size.

6. **Should destination reachability still be validated up front at order-issue time, even though movement itself becomes reactive?**
   Recommendation: Yes — keep a `Pathfinder.findPath` reachability check purely for the accept/reject decision when an order is issued, so an unreachable click is still rejected immediately with feedback (current acceptance behavior), rather than only discovered once the character starts walking toward a destination it can never actually reach.

---

## Notes and Risks

- Prototype 02 was copied directly from Prototype 01 and has not yet been renamed (package is still `com.tableverse.prototype01`, Gradle `appName` is still `TableverseRPGPrototype01`). Renaming is out of scope for this cycle unless the user asks for it.
- Per `AGENTS.md`, no git operations without explicit user permission for each command — none have been run.
- Per `AGENTS.md`, this document was created and the agent stopped before implementation; implementation then proceeded because the user explicitly requested it ("Please implement DC 2").
- ~~`hasLineOfSight`'s point-sampling (every `CELL_SIZE / 4` = 16 world units) can in theory let a diagonal line clip a corner between two diagonally-touching blocked cells without detecting it...~~ **Resolved in Phase 5:** this theoretical risk turned out to be a real, reproducible bug once movement became reactive (see Phase 5's Technical Notes) — fixed by replacing point-sampling with an exact grid-traversal algorithm.
- The user was asked to visually confirm scroll-wheel zoom and diagonal movement by running the game themselves (`.\gradlew.bat lwjgl3:run`, `JAVA_HOME` set to a JDK 17–21 install) — the same screenshot-reliability limitation noted in DevCycle 001 applies here.

---

## Completion Summary

*Fill in when the cycle closes. Move this document to `doc/planning/completed/` afterward.*

**Completion Date:** [YYYY-MM-DD]
**Phases Completed:** [List or "All"]
**Work Deferred:** [What was not done and why, or "None"]

**Accomplishments:**
-

**Metrics:**
- Files modified: [N]

**Lessons / Notes:**
Not yet closed — all five phases are now Work Complete, but the cycle stays open pending the user's playtest and decision to close it (they may want further phases, e.g. more zoom/movement tuning). Not Verified — per `DevelopmentProcess.md`, only the user can grant `Verified`. Biggest lesson from Phase 5: a movement/pathing algorithm that's provably correct when computed once from a fixed position can behave very differently once it's recomputed continuously from a moving, non-grid-aligned position — the reactive redesign surfaced a real bug (`hasLineOfSight` corner-clipping) that had been sitting as a documented-but-dismissed theoretical risk since Phase 2.
