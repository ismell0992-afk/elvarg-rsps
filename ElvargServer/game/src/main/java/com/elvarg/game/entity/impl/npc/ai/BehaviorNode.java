package com.elvarg.game.entity.impl.npc.ai;

import com.elvarg.game.entity.impl.npc.NPC;

/**
 * A single node in a behavior tree. Each node returns a {@link Status}
 * indicating whether it succeeded, failed, or is still running.
 * <p>
 * Composite nodes (Sequence, Selector) combine children.
 * Leaf nodes (Condition, Action) perform checks or actions.
 * Decorator nodes (Inverter, Repeater) modify a single child's result.
 */
public interface BehaviorNode {

    /**
     * Evaluates this node for the given NPC.
     *
     * @param npc the NPC executing this behavior tree
     * @return the result of this tick's evaluation
     */
    Status tick(NPC npc);

    /**
     * Resets any internal state for a fresh evaluation pass.
     * Called at the start of each tree tick to clear RUNNING states.
     */
    default void reset() {}

    /** Result of a single node evaluation. */
    enum Status {
        /** Node completed its work successfully. */
        SUCCESS,
        /** Node could not complete — try alternatives. */
        FAILURE,
        /** Node is still working — re-evaluate next tick. */
        RUNNING
    }
}
