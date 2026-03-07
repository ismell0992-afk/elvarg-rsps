package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.EndDialogue;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;

import static com.elvarg.util.NpcIdentifiers.ETHEREAL_GUIDE;

/**
 * Expedition Guide — a hooded figure east of the home area.
 * Entry point for the upcoming expedition system. Delivers cryptic
 * lore about instanced dungeon content beneath the harbour.
 * <p>
 * NPC ID 782 (Ethereal Guide) used as the hooded figure model.
 */
@Ids({ETHEREAL_GUIDE})
public class ExpeditionGuide extends NPC implements NPCInteraction {

    private static final int NPC_ID = ETHEREAL_GUIDE;

    public ExpeditionGuide(int id, Location position) {
        super(id, position);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new ExpeditionDialogue());
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
     * Expedition Guide dialogue with lore and placeholder content.
     */
    private static class ExpeditionDialogue extends DynamicDialogueBuilder {

        @Override
        public void build(Player player) {
            add(new NpcDialogue(0, NPC_ID, "..."));
            add(new NpcDialogue(1, NPC_ID,
                    "You feel watched. The figure turns slowly to face you."));
            add(new NpcDialogue(2, NPC_ID,
                    "The depths beneath Ironhaven hold things unseen for centuries."));

            add(new OptionDialogue(3, (option) -> {
                switch (option) {
                    case FIRST_OPTION:
                        player.getDialogueManager().start(4);
                        break;
                    case SECOND_OPTION:
                        player.getDialogueManager().start(6);
                        break;
                    case THIRD_OPTION:
                        player.getDialogueManager().start(10);
                        break;
                    default:
                        player.getPacketSender().sendInterfaceRemoval();
                        break;
                }
            }, "I want to run an expedition.",
               "What are expeditions?",
               "Never mind."));

            // Branch A: Run expedition (placeholder)
            add(new NpcDialogue(4, NPC_ID,
                    "Solo or with a group?"));
            add(new NpcDialogue(5, NPC_ID,
                    "Expeditions are not yet available."));
            add(new EndDialogue(50));

            // Branch B: Lore explanation
            add(new NpcDialogue(6, NPC_ID,
                    "Instanced dungeons beneath the harbour. Random rooms."));
            add(new NpcDialogue(7, NPC_ID,
                    "Resource rooms, puzzles, and something guarding the end."));
            add(new NpcDialogue(8, NPC_ID,
                    "You keep what you find. If you make it back."));
            add(new EndDialogue(9));

            // Branch C: Dismiss
            add(new NpcDialogue(10, NPC_ID,
                    "The depths will still be there when you're ready."));
            add(new EndDialogue(11));
        }
    }
}
