package com.elvarg.game.content;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.impl.TidecallerZharvek;
import com.elvarg.game.model.Location;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

import static com.elvarg.util.NpcIdentifiers.TIDECALLER_ZHARVEK;

/**
 * Manages the Tidecaller Zharvek world boss event. Handles spawning,
 * broadcast announcements, and cleanup.
 */
public class TidecallerEvent {

    private static final Location SPAWN_LOCATION = new Location(3420, 3440);
    private static TidecallerZharvek activeBoss;

    /**
     * Starts the world boss event. Broadcasts a warning, then spawns the
     * boss after a short delay.
     */
    public static void startEvent() {
        if (isActive()) {
            return;
        }

        World.sendMessage("[World Boss] The seas churn violently... Tidecaller Zharvek rises from the depths!");
        World.sendMessage("[World Boss] Head to the Tidal Isle to face the Tidecaller!");

        TaskManager.submit(new Task(5) {
            @Override
            protected void execute() {
                activeBoss = (TidecallerZharvek) NPC.create(TIDECALLER_ZHARVEK, SPAWN_LOCATION);
                World.getAddNPCQueue().add(activeBoss);
                activeBoss.forceChat("The tide answers to no mortal!");
                stop();
            }
        });
    }

    /**
     * Called when the boss dies. Cleans up and broadcasts the result.
     */
    public static void endEvent() {
        activeBoss = null;
        World.sendMessage("[World Boss] Tidecaller Zharvek has been defeated! The seas grow calm once more.");
    }

    /**
     * Returns whether the event is currently active.
     */
    public static boolean isActive() {
        return activeBoss != null;
    }

    /**
     * Returns the active boss instance, or null if the event is not running.
     */
    public static TidecallerZharvek getBoss() {
        return activeBoss;
    }
}
