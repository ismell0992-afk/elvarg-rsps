package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.EndDialogue;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.util.Misc;

import static com.elvarg.util.NpcIdentifiers.FISHERMAN;

/**
 * Harbour Fisherman — ambient NPC on the north docks.
 * Delivers a random line of maritime flavour text on each click.
 */
@Ids({FISHERMAN})
public class HarbourFisherman extends NPC implements NPCInteraction {

    private static final String[] DIALOGUE_POOL = {
            "Caught three sharks this morning. Good omen for Ironhaven.",
            "The water's cold but the coin's good. Can't complain.",
            "You're not from the harbour, are you? I can tell by the boots.",
            "Storm's coming in from the east. Isle looks dark tonight.",
    };

    public HarbourFisherman(int id, Location position) {
        super(id, position);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        String line = DIALOGUE_POOL[Misc.getRandom(DIALOGUE_POOL.length - 1)];
        player.getDialogueManager().start(new FishermanDialogue(line));
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
     * Single-line random dialogue for the Fisherman.
     */
    private static class FishermanDialogue extends DynamicDialogueBuilder {

        private final String line;

        FishermanDialogue(String line) {
            this.line = line;
        }

        @Override
        public void build(Player player) {
            add(new NpcDialogue(0, FISHERMAN, line));
            add(new EndDialogue(1));
        }
    }
}
