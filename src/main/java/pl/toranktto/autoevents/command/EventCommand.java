package pl.toranktto.autoevents.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import pl.toranktto.autoevents.AutoEvent;
import pl.toranktto.autoevents.AutoEventManager;
import pl.toranktto.autoevents.AutoEventsPlugin;

import java.util.*;

public class EventCommand implements TabExecutor {

    private final AutoEventsPlugin plugin;

    public EventCommand(AutoEventsPlugin plugin) {
        Objects.requireNonNull(plugin);

        this.plugin = plugin;
    }

    private void usage(CommandSender sender) {
        if (sender.hasPermission("autoevents.admin")) {
            plugin.getMessageConfig().getMessage("event-usage-admin").forEach(sender::sendMessage);
        } else if (sender.hasPermission("autoevents.player")) {
            plugin.getMessageConfig().getMessage("event-usage").forEach(sender::sendMessage);
        } else {
            plugin.getMessageConfig().getMessage("no-permission").forEach(sender::sendMessage);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            usage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join": {
                if (!sender.hasPermission("autoevents.player")) {
                    plugin.getMessageConfig().getMessage("no-permission").forEach(sender::sendMessage);
                    return true;
                }

                if (!(sender instanceof Player)) {
                    plugin.getMessageConfig().getMessage("event-console").forEach(sender::sendMessage);
                    return true;
                }

                Player player = (Player) sender;

                AutoEventManager eventManager = plugin.getAutoEventManager();
                if (eventManager.getLastEvent() == null || eventManager.getLastEvent().getState() != AutoEvent.State.STARTING) {
                    plugin.getMessageConfig().getMessage("event-join-no-event").forEach(sender::sendMessage);
                    return true;
                }

                if (eventManager.getLastEvent().getPlayers().contains(player)) {
                    plugin.getMessageConfig().getMessage("event-join-already-joined").forEach(sender::sendMessage);
                    return true;
                }

                if (!eventManager.getLastEvent().canJoin(player)) {
                    plugin.getMessageConfig().getMessage("event-cant-join").forEach(sender::sendMessage);
                    return true;
                }

                plugin.getMessageConfig().getMessage("event-join").forEach(sender::sendMessage);
                eventManager.getLastEvent().join(player);
                return true;
            }
            case "leave": {
                if (!sender.hasPermission("autoevents.player")) {
                    plugin.getMessageConfig().getMessage("no-permission").forEach(sender::sendMessage);
                    return true;
                }

                if (!(sender instanceof Player)) {
                    plugin.getMessageConfig().getMessage("event-console").forEach(sender::sendMessage);
                    return true;
                }

                Player player = (Player) sender;

                AutoEventManager eventManager = plugin.getAutoEventManager();
                if (eventManager.getLastEvent() == null || !eventManager.getLastEvent().getPlayers().contains(player)) {
                    plugin.getMessageConfig().getMessage("event-leave-no-event").forEach(sender::sendMessage);
                    return true;
                }

                plugin.getMessageConfig().getMessage("event-leave").forEach(sender::sendMessage);
                eventManager.getLastEvent().leave(player, AutoEvent.LeaveCause.COMMAND);
                return true;
            }
            case "setloc": {
                if (!sender.hasPermission("autoevents.admin")) {
                    plugin.getMessageConfig().getMessage("no-permission").forEach(sender::sendMessage);
                    return true;
                }

                if (args.length < 2) {
                    usage(sender);
                    return true;
                }

                if (!(sender instanceof Player)) {
                    plugin.getMessageConfig().getMessage("event-console").forEach(sender::sendMessage);
                    return true;
                }

                Player player = (Player) sender;

                plugin.getLocationStorage().set(args[1], player.getLocation());
                plugin.getMessageConfig().getMessage("event-setloc").forEach(sender::sendMessage);

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> plugin.getLocationStorage().save());
                return true;
            }
            case "start": {
                if (!sender.hasPermission("autoevents.admin")) {
                    plugin.getMessageConfig().getMessage("no-permission").forEach(sender::sendMessage);
                    return true;
                }

                if (args.length < 2) {
                    usage(sender);
                    return true;
                }

                AutoEventManager eventManager = plugin.getAutoEventManager();
                if (eventManager.getLastEvent() != null && eventManager.getLastEvent().getState() != AutoEvent.State.FINISHED) {
                    plugin.getMessageConfig().getMessage("event-start-not-finished").forEach(sender::sendMessage);
                    return true;
                }

                if (!eventManager.getRegisteredAutoEvents().containsKey(args[1])) {
                    plugin.getMessageConfig().getMessage("event-start-invalid-event").forEach(sender::sendMessage);
                    return true;
                }

                eventManager.startEvent(args[1]);
                return true;
            }
            case "stop": {
                if (!sender.hasPermission("autoevents.admin")) {
                    plugin.getMessageConfig().getMessage("no-permission").forEach(sender::sendMessage);
                    return true;
                }

                AutoEventManager eventManager = plugin.getAutoEventManager();
                if (eventManager.getLastEvent() == null || eventManager.getLastEvent().getState() == AutoEvent.State.FINISHED) {
                    plugin.getMessageConfig().getMessage("event-stop-no-event").forEach(sender::sendMessage);
                    return true;
                }

                eventManager.stopEvent();
                return true;
            }
            case "list": {
                if (!sender.hasPermission("autoevents.admin")) {
                    plugin.getMessageConfig().getMessage("no-permission").forEach(sender::sendMessage);
                    return true;
                }

                plugin.getMessageConfig().getMessage("event-list-header").forEach(sender::sendMessage);
                plugin.getAutoEventManager().getRegisteredAutoEvents().forEach((name, clazz) -> {
                    plugin.getMessageConfig().getMessage("event-list-row").forEach(s -> {
                        sender.sendMessage(s
                                .replace("%name%", name)
                                .replace("%localized%", plugin.getAutoEventManager().getLocalizedName(name))
                                .replace("%class%", clazz.getName())
                        );
                    });
                });
                return true;
            }
            case "reload": {
                if (!sender.hasPermission("autoevents.admin")) {
                    plugin.getMessageConfig().getMessage("no-permission").forEach(sender::sendMessage);
                    return true;
                }

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    plugin.getPluginConfig().load();
                    plugin.getMessageConfig().load();
                    plugin.getLocationStorage().load();
                    plugin.getAutoEventManager().reload();

                    plugin.getMessageConfig().getMessage("event-reload").forEach(sender::sendMessage);
                });
                return true;
            }
            case "version": {
                sender.sendMessage(ChatColor.BLUE + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion());
                return true;
            }
            default: {
                usage(sender);
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        if (!sender.hasPermission("autoevents.admin") && !sender.hasPermission("autoevents.player")) {
            return Collections.emptyList();
        }

        List<String> completion = new ArrayList<>();

        if (sender.hasPermission("autoevents.admin")) {
            switch (args.length) {
                case 1: {
                    completion.addAll(Arrays.asList("help", "join", "leave", "setloc", "start", "stop", "reload", "list", "version"));
                    break;
                }
                case 2: {
                    switch (args[0].toLowerCase()) {
                        case "start": {
                            completion.addAll(plugin.getAutoEventManager().getRegisteredAutoEvents().keySet());
                            break;
                        }
                        case "setloc": {
                            completion.add("maze-hub");
                            break;
                        }
                    }
                }
            }
        } else if (sender.hasPermission("autoevents.player")) {
            switch (args.length) {
                case 1: {
                    completion.addAll(Arrays.asList("help", "join", "leave", "version"));
                    break;
                }
            }
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], completion, new ArrayList<>());
    }
}
