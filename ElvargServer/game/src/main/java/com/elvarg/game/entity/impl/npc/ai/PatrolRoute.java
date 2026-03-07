package com.elvarg.game.entity.impl.npc.ai;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.movement.path.PathFinder;

import java.util.List;

/**
 * Moves an NPC along a fixed loop of waypoints using the existing
 * pathfinding system. Designed for patrols, guard routes, and
 * ambient NPC wandering.
 * <p>
 * Call {@link #process(NPC)} every game tick. The route can be
 * paused and resumed — on resume, the NPC walks to the nearest
 * waypoint before continuing the loop.
 */
public class PatrolRoute {

    /** Distance (Chebyshev) at which a waypoint is considered "reached". */
    private static final int ARRIVAL_DISTANCE = 1;

    private final List<Location> waypoints;
    private int currentIndex;
    private boolean paused;

    /**
     * Creates a patrol route from an ordered list of waypoints.
     *
     * @param waypoints the tile positions to visit in order (loops)
     * @throws IllegalArgumentException if fewer than 2 waypoints
     */
    public PatrolRoute(List<Location> waypoints) {
        if (waypoints.size() < 2) {
            throw new IllegalArgumentException("Patrol route needs at least 2 waypoints");
        }
        this.waypoints = List.copyOf(waypoints);
        this.currentIndex = 0;
        this.paused = false;
    }

    /**
     * Processes one tick of patrol movement. If the NPC has reached
     * the current waypoint, advances to the next. If the NPC is not
     * moving, pathfinds to the current waypoint.
     *
     * @param npc the NPC following this patrol route
     */
    public void process(NPC npc) {
        if (paused) {
            return;
        }

        Location target = waypoints.get(currentIndex);
        int distance = npc.getLocation().getDistance(target);

        if (distance <= ARRIVAL_DISTANCE) {
            // Reached waypoint — advance to next
            currentIndex = (currentIndex + 1) % waypoints.size();
            target = waypoints.get(currentIndex);
        }

        // Walk toward current target if not already moving
        if (!npc.getMovementQueue().isMoving()) {
            PathFinder.calculateWalkRoute(npc, target.getX(), target.getY());
        }
    }

    /**
     * Pauses the patrol. The NPC stops walking but remembers its
     * position in the route.
     */
    public void pause() {
        this.paused = true;
    }

    /**
     * Resumes the patrol from the nearest waypoint to the NPC's
     * current position. This prevents long backtracks after events.
     *
     * @param npc the NPC resuming its patrol
     */
    public void resume(NPC npc) {
        this.paused = false;
        this.currentIndex = findNearestWaypoint(npc.getLocation());
    }

    /**
     * @return true if the patrol is currently paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Finds the waypoint index closest to the given location.
     *
     * @param location the position to search from
     * @return the index of the nearest waypoint
     */
    private int findNearestWaypoint(Location location) {
        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < waypoints.size(); i++) {
            int dist = location.getDistance(waypoints.get(i));
            if (dist < bestDistance) {
                bestDistance = dist;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
