package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.World;
import com.elvarg.game.content.TidecallerEvent;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.TidecallerCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.npc.ai.BehaviorNode.Status;
import com.elvarg.game.entity.impl.npc.ai.BehaviorTree;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.EndDialogue;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;

import java.util.ArrayList;
import java.util.List;

import static com.elvarg.util.NpcIdentifiers.TIDECALLER_ZHARVEK;
import static com.elvarg.util.NpcIdentifiers.TIDE_REAVER;

/**
 * Tidecaller Zharvek — a three-phase maritime world boss.
 * <p>
 * Uses a {@link BehaviorTree} to drive per-tick combat decisions.
 * The tree selects the correct phase logic based on HP thresholds:
 * <ul>
 *   <li>Phase 1 (Rising Tide): 100%–60% HP — water magic, tidal wave AoE</li>
 *   <li>Phase 2 (Tempest):     60%–25% HP  — lightning AoE, spawns Tide Reavers</li>
 *   <li>Phase 3 (Maelstrom):   25%–0% HP   — whirlpool AoE, damage reduction, life drain</li>
 * </ul>
 */
@Ids({TIDECALLER_ZHARVEK})
public class TidecallerZharvek extends NPC implements NPCInteraction {

    private static final CombatMethod COMBAT_METHOD = new TidecallerCombatMethod();

    private final BehaviorTree tree;
    private final List<TideReaver> activeMinions = new ArrayList<>();

    private int currentPhase = 1;
    private int ticksSinceAoe = 0;
    private boolean spawnedMinions = false;

    // ── Tuning constants (stubs for user contribution) ──

    // TODO: Tune these values — they control boss difficulty.
    // Phase 1: Tidal Wave
    private static final int PHASE1_AOE_INTERVAL = 12;
    private static final int PHASE1_AOE_RADIUS = 5;
    private static final int PHASE1_AOE_MAX_DAMAGE = 20;

    // Phase 2: Lightning Strike
    private static final int PHASE2_AOE_INTERVAL = 10;
    private static final int PHASE2_AOE_RADIUS = 6;
    private static final int PHASE2_AOE_MAX_DAMAGE = 28;

    // Phase 3: Whirlpool
    private static final int PHASE3_AOE_INTERVAL = 8;
    private static final int PHASE3_AOE_RADIUS = 4;
    private static final int PHASE3_AOE_MAX_DAMAGE = 35;
    private static final int PHASE3_HEAL_PER_HIT = 1;

    public TidecallerZharvek(int id, Location position) {
        super(id, position);

        /*
         * Behavior tree structure:
         *   selector(
         *     sequence(condition(hp < 25%), action(phase3)),   // Maelstrom
         *     sequence(condition(hp < 60%), action(phase2)),   // Tempest
         *     action(phase1)                                   // Rising Tide
         *   )
         */
        tree = new BehaviorTree(
                BehaviorTree.selector(
                        BehaviorTree.sequence(
                                BehaviorTree.condition(npc -> hpPercent(npc) < 25),
                                BehaviorTree.action(this::phase3Combat)
                        ),
                        BehaviorTree.sequence(
                                BehaviorTree.condition(npc -> hpPercent(npc) < 60),
                                BehaviorTree.action(this::phase2Combat)
                        ),
                        BehaviorTree.action(this::phase1Combat)
                )
        );
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }

    @Override
    public void process() {
        super.process();

        if (getCombat().getTarget() != null) {
            tree.tick(this);
        }
    }

    // ── Phase action methods (called by the behavior tree) ──

    private Status phase1Combat(NPC npc) {
        transitionToPhase(1);
        ticksSinceAoe++;

        if (ticksSinceAoe >= PHASE1_AOE_INTERVAL) {
            TidecallerCombatMethod.tidalWaveAoE(this, PHASE1_AOE_RADIUS, PHASE1_AOE_MAX_DAMAGE);
            ticksSinceAoe = 0;
        }

        return Status.SUCCESS;
    }

    private Status phase2Combat(NPC npc) {
        transitionToPhase(2);
        ticksSinceAoe++;

        if (!spawnedMinions) {
            spawnTideReavers();
            spawnedMinions = true;
        }

        if (ticksSinceAoe >= PHASE2_AOE_INTERVAL) {
            TidecallerCombatMethod.lightningAoE(this, PHASE2_AOE_RADIUS, PHASE2_AOE_MAX_DAMAGE);
            ticksSinceAoe = 0;
        }

        return Status.SUCCESS;
    }

    private Status phase3Combat(NPC npc) {
        transitionToPhase(3);
        ticksSinceAoe++;

        if (ticksSinceAoe >= PHASE3_AOE_INTERVAL) {
            TidecallerCombatMethod.whirlpoolAoE(this, PHASE3_AOE_RADIUS, PHASE3_AOE_MAX_DAMAGE, PHASE3_HEAL_PER_HIT);
            ticksSinceAoe = 0;
        }

        return Status.SUCCESS;
    }

    // ── Phase transitions ──

    private void transitionToPhase(int newPhase) {
        if (currentPhase == newPhase) return;

        int oldPhase = currentPhase;
        currentPhase = newPhase;
        ticksSinceAoe = 0;

        switch (newPhase) {
            case 2:
                forceChat("The tempest awakens!");
                break;
            case 3:
                forceChat("You face the Maelstrom now... there is no escape!");
                break;
        }
    }

    // ── Minion management ──

    private void spawnTideReavers() {
        for (int i = 0; i < 2; i++) {
            TideReaver reaver = (TideReaver) NPC.create(TIDE_REAVER, getLocation());
            reaver.setBoss(this);
            activeMinions.add(reaver);
            World.getAddNPCQueue().add(reaver);
        }
        forceChat("Rise, servants of the deep!");
    }

    public void despawnMinion(TideReaver reaver) {
        activeMinions.remove(reaver);
    }

    private void despawnAllMinions() {
        for (TideReaver reaver : activeMinions) {
            World.getRemoveNPCQueue().add(reaver);
        }
        activeMinions.clear();
    }

    // ── Damage reduction (Phase 3) ──

    @Override
    public PendingHit manipulateHit(PendingHit hit) {
        if (currentPhase == 3) {
            int reduced = (int) (hit.getTotalDamage() * 0.70);
            hit.setTotalDamage(reduced);
        }
        return hit;
    }

    // ── Death handling ──

    @Override
    public void appendDeath() {
        despawnAllMinions();
        super.appendDeath();
        TidecallerEvent.endEvent();
    }

    // ── NPCInteraction ──

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new TidecallerDialogue());
    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {}

    @Override
    public void thirdOptionClick(Player player, NPC npc) {}

    @Override
    public void forthOptionClick(Player player, NPC npc) {}

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {}

    // ── Getters ──

    public int getCurrentPhase() {
        return currentPhase;
    }

    // ── Helpers ──

    private static int hpPercent(NPC npc) {
        return (npc.getHitpoints() * 100) / npc.getDefinition().getHitpoints();
    }

    // ── Dialogue ──

    private static class TidecallerDialogue extends DynamicDialogueBuilder {
        @Override
        public void build(Player player) {
            add(new NpcDialogue(0, TIDECALLER_ZHARVEK,
                    "I am Zharvek, caller of tides. These waters bend to my will."));
            add(new NpcDialogue(1, TIDECALLER_ZHARVEK,
                    "Flee now, or be consumed by the deep."));
            add(new EndDialogue(2));
        }
    }
}
