package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.TIDE_REAVER;

/**
 * Phase 2 minion for Tidecaller Zharvek. Aggressive melee NPC
 * that targets the boss's current combat target. Despawns when
 * the boss dies or after a timeout.
 */
@Ids({TIDE_REAVER})
public class TideReaver extends NPC {

    private TidecallerZharvek boss;
    private int timer = 0;

    public TideReaver(int id, Location position) {
        super(id, position);
    }

    @Override
    public void process() {
        super.process();

        if (boss != null) {
            Mobile target = boss.getCombat().getTarget();
            if (target == null) {
                target = boss.getCombat().getAttacker();
            }
            if (target != null) {
                if (getCombat().getTarget() != target) {
                    getCombat().attack(target);
                }
                return;
            }
        }

        if (timer >= 500) {
            appendDeath();
        }
        timer++;
    }

    @Override
    public void appendDeath() {
        super.appendDeath();
        if (boss != null) {
            boss.despawnMinion(this);
        }
    }

    public void setBoss(TidecallerZharvek boss) {
        this.boss = boss;
    }
}
