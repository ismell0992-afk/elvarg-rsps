package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.impl.TidecallerZharvek;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

import java.util.List;

/**
 * Custom combat method for Tidecaller Zharvek. The behavior tree
 * drives phase selection; this method handles the projectile visuals
 * and basic attack delivery per phase.
 */
public class TidecallerCombatMethod extends CombatMethod {

    // Phase 1: Water bolt
    private static final Projectile WATER_BOLT = new Projectile(136, 31, 43, 40, 80);
    private static final Graphic WATER_SPLASH = new Graphic(137);

    // Phase 2: Lightning bolt
    private static final Projectile LIGHTNING_BOLT = new Projectile(165, 31, 43, 30, 70);
    private static final Graphic LIGHTNING_STRIKE = new Graphic(166);

    // Phase 3: Whirlpool
    private static final Graphic WHIRLPOOL_GFX = new Graphic(281);

    @Override
    public boolean canAttack(Mobile character, Mobile target) {
        return true;
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[]{new PendingHit(character, target, this, 2)};
    }

    @Override
    public void start(Mobile character, Mobile target) {
        if (!character.isNpc() || !target.isPlayer()) return;

        character.performAnimation(new Animation(character.getAttackAnim()));

        TidecallerZharvek boss = (TidecallerZharvek) character.getAsNpc();
        int phase = boss.getCurrentPhase();

        if (phase == 1) {
            Projectile.sendProjectile(character, target, WATER_BOLT);
        } else if (phase == 2) {
            Projectile.sendProjectile(character, target, LIGHTNING_BOLT);
        } else {
            Projectile.sendProjectile(character, target, WATER_BOLT);
        }

        character.getTimers().register(TimerKey.COMBAT_ATTACK, 5);
    }

    @Override
    public int attackDistance(Mobile character) {
        return 8;
    }

    @Override
    public CombatType type() {
        return CombatType.MAGIC;
    }

    // ── AoE methods called from the behavior tree action nodes ──

    /**
     * Phase 1 — Tidal Wave: AoE centered on the boss, hits all players within radius.
     */
    public static void tidalWaveAoE(TidecallerZharvek boss, int radius, int maxDamage) {
        boss.forceChat("The tide swells!");
        List<Player> nearby = boss.getPlayersWithinDistance(radius);
        Location center = boss.getLocation();

        for (Player player : nearby) {
            player.getPacketSender().sendGlobalGraphic(WATER_SPLASH, player.getLocation());
            player.getCombat().getHitQueue()
                    .addPendingDamage(new HitDamage(Misc.getRandom(maxDamage), HitMask.RED));
        }
    }

    /**
     * Phase 2 — Lightning Strike: delayed AoE on random tiles near the target.
     */
    public static void lightningAoE(TidecallerZharvek boss, int radius, int maxDamage) {
        boss.forceChat("Feel the storm's wrath!");
        Mobile target = boss.getCombat().getTarget();
        if (target == null) return;

        Location targetPos = target.getLocation();

        TaskManager.submit(new Task(3) {
            @Override
            protected void execute() {
                for (int i = 0; i < 3; i++) {
                    Location strikePos = new Location(
                            (targetPos.getX() - 2) + Misc.getRandom(4),
                            (targetPos.getY() - 2) + Misc.getRandom(4)
                    );
                    for (Player player : boss.getPlayersWithinDistance(radius)) {
                        player.getPacketSender().sendGlobalGraphic(LIGHTNING_STRIKE, strikePos);
                        if (player.getLocation().getDistance(strikePos) <= 1) {
                            player.getCombat().getHitQueue()
                                    .addPendingDamage(new HitDamage(Misc.getRandom(maxDamage), HitMask.RED));
                        }
                    }
                }
                stop();
            }
        });
    }

    /**
     * Phase 3 — Whirlpool: AoE centered on the boss. Damages all nearby players
     * and heals the boss per player hit.
     */
    public static void whirlpoolAoE(TidecallerZharvek boss, int radius, int maxDamage, int healPerHit) {
        boss.forceChat("Drown in the maelstrom!");
        List<Player> nearby = boss.getPlayersWithinDistance(radius);

        for (Player player : nearby) {
            player.getPacketSender().sendGlobalGraphic(WHIRLPOOL_GFX, player.getLocation());
            player.getCombat().getHitQueue()
                    .addPendingDamage(new HitDamage(Misc.getRandom(maxDamage), HitMask.RED));
            boss.setHitpoints(boss.getHitpoints() + healPerHit);
        }
    }
}
