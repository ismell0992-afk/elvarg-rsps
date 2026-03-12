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
 * broadcast announcements, auto-scheduling, and cleanup.
 */
public class TidecallerEvent {

    private static final Location SPAWN_LOCATION = new Location(3420, 3440);
    private static final int DEFAULT_INTERVAL = 12000; // 2 hours in ticks

    private static TidecallerZharvek activeBoss;
    private static Task schedulerTask;

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
     * Called when the boss dies or an admin force-stops the event.
     * Cleans up and broadcasts the result.
     */
    public static void endEvent() {
        if (activeBoss != null) {
            if (activeBoss.isRegistered()) {
                activeBoss.forceRemove();
            }
            activeBoss = null;
        }
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

    // ── Auto-Scheduler ──

    /**
     * Starts a recurring scheduler that triggers the event every {@code intervalTicks} ticks.
     * If a scheduler is already running, it is stopped first.
     */
    public static void startAutoScheduler(int intervalTicks) {
        stopAutoScheduler();
        schedulerTask = new Task(intervalTicks) {
            @Override
            protected void execute() {
                if (!isActive()) {
                    startEvent();
                }
            }
        };
        TaskManager.submit(schedulerTask);
        System.out.println("[TidecallerEvent] Auto-scheduler started (every " + (intervalTicks * 600 / 60000) + " minutes).");
    }

    /**
     * Stops the recurring auto-scheduler if one is running.
     */
    public static void stopAutoScheduler() {
        if (schedulerTask != null) {
            schedulerTask.stop();
            schedulerTask = null;
            System.out.println("[TidecallerEvent] Auto-scheduler stopped.");
        }
    }

    /**
     * Returns whether the auto-scheduler is currently running.
     */
    public static boolean isSchedulerRunning() {
        return schedulerTask != null && schedulerTask.isRunning();
    }

    /**
     * Returns the scheduler interval in ticks, or -1 if no scheduler is active.
     */
    public static int getSchedulerInterval() {
        return schedulerTask != null ? schedulerTask.getDelay() : -1;
    }

    /**
     * Called on server boot to start the default auto-scheduler.
     */
    public static void initDefaultScheduler() {
        startAutoScheduler(DEFAULT_INTERVAL);
    }
}
