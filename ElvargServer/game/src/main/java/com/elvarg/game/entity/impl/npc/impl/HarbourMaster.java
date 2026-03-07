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

import static com.elvarg.util.NpcIdentifiers.LUMBRIDGE_GUIDE;

/**
 * Harbour Master — new player orientation NPC for Ironhaven.
 * Provides server commands, area overview, and a maritime welcome.
 */
@Ids({LUMBRIDGE_GUIDE})
public class HarbourMaster extends NPC implements NPCInteraction {

    private static final int NPC_ID = LUMBRIDGE_GUIDE;

    public HarbourMaster(int id, Location position) {
        super(id, position);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new HarbourMasterDialogue());
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
     * Branching dialogue tree for the Harbour Master.
     */
    private static class HarbourMasterDialogue extends DynamicDialogueBuilder {

        @Override
        public void build(Player player) {
            add(new NpcDialogue(0, NPC_ID,
                    "Welcome to Ironhaven. You look like you've just made port."));

            add(new NpcDialogue(1, NPC_ID,
                    "Let me show you around. This harbour has everything you need."));

            add(new OptionDialogue(2, (option) -> {
                switch (option) {
                    case FIRST_OPTION:
                        player.getDialogueManager().start(3);
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
            }, "What commands can I use?",
               "What is there to do here?",
               "I'm fine, thanks."));

            // Branch A: Commands
            add(new NpcDialogue(3, NPC_ID,
                    "Try ::home, ::train, ::bosses, ::vote, ::store, and ::discord."));
            add(new NpcDialogue(4, NPC_ID,
                    "Type ::commands anytime for the full list."));
            add(new EndDialogue(5));

            // Branch B: Activities
            add(new NpcDialogue(6, NPC_ID,
                    "Train your skills in the western district."));
            add(new NpcDialogue(7, NPC_ID,
                    "Fight in the grounds, or seek the hooded figure east of here."));
            add(new NpcDialogue(8, NPC_ID,
                    "The figure deals in something... dangerous."));
            add(new EndDialogue(9));

            // Branch C: Dismiss
            add(new NpcDialogue(10, NPC_ID,
                    "Aye. The sea doesn't wait. Good luck out there."));
            add(new EndDialogue(11));
        }
    }
}
