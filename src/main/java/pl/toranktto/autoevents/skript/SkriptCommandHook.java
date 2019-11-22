package pl.toranktto.autoevents.skript;

import com.google.common.io.Files;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pl.toranktto.autoevents.AutoEvent;
import pl.toranktto.autoevents.AutoEventManager;
import pl.toranktto.autoevents.AutoEventsPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SkriptCommandHook implements CommandExecutor {

    private final AutoEventsPlugin plugin;
    private final CommandExecutor commandExecutor;

    public SkriptCommandHook(AutoEventsPlugin plugin, CommandExecutor commandExecutor) {
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(commandExecutor);

        this.plugin = plugin;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("skript.admin")) {
            if (args.length < 2) {
                return commandExecutor.onCommand(sender, command, label, args);
            }

            if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("disable")) {
                AutoEventManager eventManager = plugin.getAutoEventManager();
                if (eventManager.getLastEvent() != null
                        && eventManager.getLastEvent() instanceof SkriptAutoEvent
                        && eventManager.getLastEvent().getState() == AutoEvent.State.STARTED) {
                    plugin.getMessageConfig().getMessage("skript-event-is-started").forEach(sender::sendMessage);
                    return true;
                }

                if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("scripts")) {
                    for (Map.Entry<String, Class<? extends AutoEvent>> i : new HashMap<>(eventManager.getRegisteredAutoEvents()).entrySet()) {
                        if (i.getValue().equals(SkriptAutoEvent.class)) {
                            eventManager.unregister(i.getKey());
                        }
                    }
                } else {
                    String scriptName = Files.getNameWithoutExtension(args[1]);
                    if (eventManager.getRegisteredAutoEvents().containsKey(scriptName)) {
                        eventManager.unregister(scriptName);
                    }
                }
            }
        }

        return commandExecutor.onCommand(sender, command, label, args);
    }
}
