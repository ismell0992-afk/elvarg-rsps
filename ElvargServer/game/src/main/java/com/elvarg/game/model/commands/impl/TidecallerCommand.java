package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.TidecallerEvent;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;

public class TidecallerCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (parts.length < 2) {
            sendUsage(player);
            return;
        }

        String sub = parts[1].toLowerCase();
        switch (sub) {
            case "start":
                if (TidecallerEvent.isActive()) {
                    player.getPacketSender().sendMessage("The Tidecaller event is already active.");
                } else {
                    TidecallerEvent.startEvent();
                    player.getPacketSender().sendMessage("Tidecaller event started.");
                }
                break;

            case "stop":
                if (!TidecallerEvent.isActive()) {
                    player.getPacketSender().sendMessage("No Tidecaller event is currently active.");
                } else {
                    TidecallerEvent.endEvent();
                    player.getPacketSender().sendMessage("Tidecaller event force-stopped.");
                }
                break;

            case "status":
                String active = TidecallerEvent.isActive() ? "active" : "not active";
                String scheduler = TidecallerEvent.isSchedulerRunning()
                        ? "running (every " + (TidecallerEvent.getSchedulerInterval() * 600 / 60000) + " min)"
                        : "stopped";
                player.getPacketSender().sendMessage("Tidecaller: " + active + ", scheduler: " + scheduler);
                break;

            case "schedule":
                if (parts.length < 3) {
                    player.getPacketSender().sendMessage("Usage: ::tidecaller schedule <minutes>");
                    return;
                }
                try {
                    int minutes = Integer.parseInt(parts[2]);
                    int ticks = minutes * 100; // 1 minute = 100 ticks (600ms each)
                    TidecallerEvent.startAutoScheduler(ticks);
                    player.getPacketSender().sendMessage("Scheduler set to every " + minutes + " minutes.");
                } catch (NumberFormatException e) {
                    player.getPacketSender().sendMessage("Invalid number: " + parts[2]);
                }
                break;

            case "unschedule":
                TidecallerEvent.stopAutoScheduler();
                player.getPacketSender().sendMessage("Tidecaller auto-scheduler stopped.");
                break;

            case "tele":
                TeleportHandler.teleport(player, new Location(3420, 3440), TeleportType.NORMAL, false);
                player.getPacketSender().sendMessage("Teleporting to Tidecaller Zharvek...");
                break;

            default:
                sendUsage(player);
                break;
        }
    }

    private void sendUsage(Player player) {
        player.getPacketSender().sendMessage("Usage: ::tidecaller [start|stop|status|schedule <min>|unschedule|tele]");
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }
}
