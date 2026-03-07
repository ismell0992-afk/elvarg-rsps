package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.npc.ai.NPCState;
import com.elvarg.game.entity.impl.npc.ai.NPCStateMachine;
import com.elvarg.game.entity.impl.npc.ai.PatrolRoute;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.EndDialogue;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;

import java.util.List;

import static com.elvarg.util.NpcIdentifiers.TOWN_CRIER;

/**
 * Town Crier — patrols the town square, broadcasts server events every
 * 30 minutes, and delivers context-sensitive dialogue to players.
 * Uses the FSM framework and PatrolRoute for state management.
 */
@Ids({TOWN_CRIER})
public class TownCrier extends NPC implements NPCInteraction {

    /** Broadcast interval: 3000 ticks = 30 minutes at 600ms/tick. */
    private static final int BROADCAST_INTERVAL = 3000;

    private final NPCStateMachine<State> fsm;
    private final PatrolRoute patrol;
    private int ticksSinceBroadcast = 0;

    public TownCrier(int id, Location position) {
        super(id, position);

        patrol = new PatrolRoute(List.of(
                new Location(3089, 3524),
                new Location(3093, 3524),
                new Location(3093, 3528),
                new Location(3089, 3528),
                new Location(3085, 3528),
                new Location(3085, 3524)
        ));

        fsm = new NPCStateMachine<>(State.class, State.PATROLLING);

        fsm.addState(State.PATROLLING, new NPCState<>() {
            @Override
            public void onEnter(NPC npc) {
                patrol.resume(npc);
            }

            @Override
            public void onUpdate(NPC npc) {
                ticksSinceBroadcast++;
                patrol.process(npc);
            }

            @Override
            public State shouldTransition(NPC npc) {
                if (ticksSinceBroadcast >= BROADCAST_INTERVAL) {
                    return State.BROADCASTING;
                }
                return null;
            }
        });

        fsm.addState(State.BROADCASTING, new NPCState<>() {
            @Override
            public void onEnter(NPC npc) {
                patrol.pause();
                npc.getMovementQueue().reset();
                broadcastMessage();
                ticksSinceBroadcast = 0;
            }

            @Override
            public void onUpdate(NPC npc) {
                // Brief pause during broadcast — 5 ticks (3 seconds)
            }

            @Override
            public State shouldTransition(NPC npc) {
                NPCStateMachine<State> machine = ((TownCrier) npc).fsm;
                if (machine.getTicksInState() >= 5) {
                    return State.PATROLLING;
                }
                return null;
            }
        });

        fsm.addState(State.RESPONDING_TO_PLAYER, new NPCState<>() {
            @Override
            public void onEnter(NPC npc) {
                patrol.pause();
                npc.getMovementQueue().reset();
            }

            @Override
            public void onUpdate(NPC npc) {
                ticksSinceBroadcast++;
            }

            @Override
            public State shouldTransition(NPC npc) {
                // Return to patrol after 10 ticks (6 seconds)
                NPCStateMachine<State> machine = ((TownCrier) npc).fsm;
                if (machine.getTicksInState() >= 10) {
                    return State.PATROLLING;
                }
                return null;
            }
        });

        fsm.start(this);
    }

    @Override
    public void process() {
        super.process();
        fsm.update(this);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        fsm.transitionTo(this, State.RESPONDING_TO_PLAYER);
        player.getDialogueManager().start(new TownCrierDialogue());
    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {}

    @Override
    public void thirdOptionClick(Player player, NPC npc) {}

    @Override
    public void forthOptionClick(Player player, NPC npc) {}

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {}

    /**
     * Sends a server-wide broadcast message. Content varies based on
     * server state (world boss timers, events, etc.).
     */
    private void broadcastMessage() {
        // Default broadcast — can be extended with event checks
        String message = "[Town Crier] Hear ye! Ironhaven stands strong. "
                + "No events at present.";
        World.sendMessage(message);
    }

    /** FSM states for the Town Crier. */
    private enum State {
        PATROLLING,
        BROADCASTING,
        RESPONDING_TO_PLAYER
    }

    /**
     * Context-sensitive dialogue for the Town Crier.
     */
    private static class TownCrierDialogue extends DynamicDialogueBuilder {

        @Override
        public void build(Player player) {
            add(new NpcDialogue(0, TOWN_CRIER,
                    "Hear ye, hear ye! Ironhaven stands strong today."));
            add(new NpcDialogue(1, TOWN_CRIER,
                    "No events at present. Check back soon, traveller."));
            add(new EndDialogue(2));
        }
    }
}
