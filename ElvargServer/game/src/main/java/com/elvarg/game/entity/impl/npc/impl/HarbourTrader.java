package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.container.shop.Shop;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;

import static com.elvarg.util.NpcIdentifiers.TRADER_STAN;

/**
 * Harbour Trader — basic supply vendor on the north docks.
 * Sells fishing bait, cooked food, rope, and torches for coin.
 */
@Ids({TRADER_STAN})
public class HarbourTrader extends NPC implements NPCInteraction {

    /** Shop ID — must not conflict with existing shop IDs (0-12 taken). */
    private static final int SHOP_ID = 13;

    private static final Shop HARBOUR_SUPPLY_SHOP = new Shop(
            SHOP_ID,
            "Harbour Supplies",
            new Item[]{
                    new Item(313, Shop.INFINITY),   // Fishing bait
                    new Item(314, Shop.INFINITY),   // Feather
                    new Item(379, Shop.INFINITY),   // Cooked lobster
                    new Item(385, Shop.INFINITY),   // Cooked shark
                    new Item(954, Shop.INFINITY),   // Rope
                    new Item(590, Shop.INFINITY),   // Tinderbox
                    new Item(1351, Shop.INFINITY),  // Bronze axe
                    new Item(307, Shop.INFINITY),   // Fishing rod
            }
    );

    static {
        ShopManager.shops.put(SHOP_ID, HARBOUR_SUPPLY_SHOP);
    }

    public HarbourTrader(int id, Location position) {
        super(id, position);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new TraderDialogue());
    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {
        ShopManager.open(player, SHOP_ID);
    }

    @Override
    public void thirdOptionClick(Player player, NPC npc) {}

    @Override
    public void forthOptionClick(Player player, NPC npc) {}

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {}

    /**
     * Short dialogue that leads into the shop.
     */
    private static class TraderDialogue extends DynamicDialogueBuilder {

        @Override
        public void build(Player player) {
            add(new NpcDialogue(0, TRADER_STAN,
                    "Need supplies before you head out? I've got what the harbour has."));

            add(new OptionDialogue(1, (option) -> {
                switch (option) {
                    case FIRST_OPTION:
                        ShopManager.open(player, SHOP_ID);
                        break;
                    default:
                        player.getPacketSender().sendInterfaceRemoval();
                        break;
                }
            }, "Show me what you have.", "No thanks."));
        }
    }
}
