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

import static com.elvarg.util.NpcIdentifiers.GULLUCK;

/**
 * Ironhaven Blacksmith — placeholder for a future gear upgrade system.
 * Stationed at the forge near the Trading Hall. Offers cryptic hints
 * about materials found in deep expeditions.
 * <p>
 * NPC ID 15 (Gulluck) used as a blacksmith model.
 */
@Ids({GULLUCK})
public class IronhavenBlacksmith extends NPC implements NPCInteraction {

    private static final int NPC_ID = GULLUCK;

    public IronhavenBlacksmith(int id, Location position) {
        super(id, position);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new BlacksmithDialogue());
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
     * Blacksmith dialogue with upgrade teaser and material hints.
     */
    private static class BlacksmithDialogue extends DynamicDialogueBuilder {

        @Override
        public void build(Player player) {
            add(new NpcDialogue(0, NPC_ID,
                    "Bring me something worth improving and I'll make it worth fearing."));
            add(new NpcDialogue(1, NPC_ID,
                    "I've upgraded blades that felled things older than this harbour."));

            add(new OptionDialogue(2, (option) -> {
                switch (option) {
                    case FIRST_OPTION:
                        player.getDialogueManager().start(3);
                        break;
                    case SECOND_OPTION:
                        player.getDialogueManager().start(7);
                        break;
                    default:
                        player.getPacketSender().sendInterfaceRemoval();
                        break;
                }
            }, "Can you upgrade my gear?",
               "What materials do you need?"));

            // Branch A: Upgrade request
            add(new NpcDialogue(3, NPC_ID,
                    "Not yet. The forge needs materials I haven't sourced."));
            add(new NpcDialogue(4, NPC_ID,
                    "Venture into the deep expeditions. Bring me what you find."));
            add(new NpcDialogue(5, NPC_ID,
                    "(The upgrade system will be available soon.)"));
            add(new EndDialogue(6));

            // Branch B: Materials
            add(new NpcDialogue(7, NPC_ID,
                    "Ironhaven ore, deep sea alloy, and something with magic in it."));
            add(new NpcDialogue(8, NPC_ID,
                    "You'll know it when you find it. Most don't come back with it."));
            add(new EndDialogue(9));
        }
    }
}
