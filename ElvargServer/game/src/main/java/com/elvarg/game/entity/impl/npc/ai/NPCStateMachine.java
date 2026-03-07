package com.elvarg.game.entity.impl.npc.ai;

import com.elvarg.game.entity.impl.npc.NPC;

import java.util.EnumMap;
import java.util.Map;

/**
 * A lightweight finite state machine for NPC AI. Each NPC subclass creates
 * its own enum of states and registers {@link NPCState} handlers for each.
 * <p>
 * Call {@link #update(NPC)} from the NPC's {@code process()} method every
 * game tick. The FSM handles state transitions automatically.
 *
 * @param <S> the enum type representing all possible states
 */
public class NPCStateMachine<S extends Enum<S>> {

    private final Map<S, NPCState<S>> states;
    private S currentStateKey;
    private int ticksInState;

    /**
     * Creates a new state machine.
     *
     * @param stateType    the enum class for this NPC's states
     * @param initialState the state the NPC starts in
     */
    public NPCStateMachine(Class<S> stateType, S initialState) {
        this.states = new EnumMap<>(stateType);
        this.currentStateKey = initialState;
        this.ticksInState = 0;
    }

    /**
     * Registers a state handler for the given state key.
     *
     * @param key   the state enum value
     * @param state the handler defining behavior for this state
     * @return this machine, for chaining
     */
    public NPCStateMachine<S> addState(S key, NPCState<S> state) {
        states.put(key, state);
        return this;
    }

    /**
     * Called once after all states are registered to run onEnter for the
     * initial state. Must be called before the first {@link #update}.
     *
     * @param npc the NPC to initialize
     */
    public void start(NPC npc) {
        NPCState<S> initial = states.get(currentStateKey);
        if (initial != null) {
            initial.onEnter(npc);
        }
    }

    /**
     * Processes one tick of the state machine. Checks for transitions first,
     * then runs the current state's update. This should be called from
     * {@code NPC.process()} every game tick.
     *
     * @param npc the NPC to update
     */
    public void update(NPC npc) {
        NPCState<S> current = states.get(currentStateKey);
        if (current == null) {
            return;
        }

        // Check for state transition
        S nextState = current.shouldTransition(npc);
        if (nextState != null && nextState != currentStateKey) {
            transitionTo(npc, nextState);
            current = states.get(currentStateKey);
            if (current == null) {
                return;
            }
        }

        current.onUpdate(npc);
        ticksInState++;
    }

    /**
     * Forces a transition to the given state, running exit/enter callbacks.
     *
     * @param npc      the NPC transitioning
     * @param newState the state to transition to
     */
    public void transitionTo(NPC npc, S newState) {
        if (newState == currentStateKey) {
            return;
        }

        NPCState<S> oldState = states.get(currentStateKey);
        if (oldState != null) {
            oldState.onExit(npc);
        }

        currentStateKey = newState;
        ticksInState = 0;

        NPCState<S> entering = states.get(currentStateKey);
        if (entering != null) {
            entering.onEnter(npc);
        }
    }

    /**
     * @return the current state enum value
     */
    public S getCurrentState() {
        return currentStateKey;
    }

    /**
     * @return how many ticks the NPC has been in the current state
     */
    public int getTicksInState() {
        return ticksInState;
    }
}
