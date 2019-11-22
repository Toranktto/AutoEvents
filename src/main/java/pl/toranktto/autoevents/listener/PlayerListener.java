package pl.toranktto.autoevents.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.toranktto.autoevents.AutoEvent;
import pl.toranktto.autoevents.AutoEventsPlugin;

import java.util.Objects;

public final class PlayerListener implements Listener {

    private final AutoEventsPlugin plugin;

    public PlayerListener(AutoEventsPlugin plugin) {
        Objects.requireNonNull(plugin);

        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        AutoEvent lastEvent = plugin.getAutoEventManager().getLastEvent();
        if (lastEvent != null && lastEvent.getState() != AutoEvent.State.FINISHED && lastEvent.getPlayers().contains(player)) {
            lastEvent.leave(player, AutoEvent.LeaveCause.QUIT);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() != null && event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            return;
        }

        Player player = event.getPlayer();
        AutoEvent lastEvent = plugin.getAutoEventManager().getLastEvent();
        if (lastEvent != null && lastEvent.getState() == AutoEvent.State.STARTED && lastEvent.getPlayers().contains(player)) {
            lastEvent.leave(player, AutoEvent.LeaveCause.TELEPORT);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("autoevents.admin")) {
            return;
        }

        AutoEvent lastEvent = plugin.getAutoEventManager().getLastEvent();
        if (lastEvent != null && lastEvent.getState() == AutoEvent.State.STARTED && lastEvent.getPlayers().contains(player)) {
            if (!event.getMessage().startsWith("/event")) {
                plugin.getMessageConfig().getMessage("commands-disabled").forEach(player::sendMessage);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        AutoEvent lastEvent = plugin.getAutoEventManager().getLastEvent();
        if (lastEvent != null && lastEvent.getState() == AutoEvent.State.STARTED && lastEvent.getPlayers().contains(player)) {
            lastEvent.leave(player, AutoEvent.LeaveCause.DEATH);
        }
    }
}
