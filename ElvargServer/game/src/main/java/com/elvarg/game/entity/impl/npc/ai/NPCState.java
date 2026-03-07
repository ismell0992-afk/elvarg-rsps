package com.elvarg.game.entity.impl.npc.ai;

import com.elvarg.game.entity.impl.npc.NPC;

/**
 * Represents a single state in an NPC's finite state machine.
 * Each state defines behavior for entering, updating per-tick, and exiting.
 *
 * @param <S> the enum type representing all possible states
 */
public interface NPCState<S extends Enum<S>> {

    /**
     * Called once when the NPC transitions into this state.
     *
     * @param npc the NPC entering this state
     */
    default void onEnter(NPC npc) {}

    /**
     * Called every game tick (600ms) while the NPC remains in this state.
     *
     * @param npc the NPC being updated
     */
    void onUpdate(NPC npc);

    /**
     * Called once when the NPC transitions out of this state.
     *
     * @param npc the NPC leaving this state
     */
    default void onExit(NPC npc) {}

    /**
     * Determines whether this state should transition to a different state.
     * Return null to remain in the current state.
     *
     * @param npc the NPC to evaluate
     * @return the next state to transition to, or null to stay
     */
    default S shouldTransition(NPC npc) {
        return null;
    }
}
