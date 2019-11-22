package pl.toranktto.autoevents.hook;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.command.PluginCommand;
import pl.toranktto.autoevents.AutoEventsPlugin;
import pl.toranktto.autoevents.skript.SkriptCommandHook;

import java.util.Objects;

public final class SkriptHook {

    private final AutoEventsPlugin plugin;
    private final SkriptAddon skriptAddon;
    private boolean initialized;

    public SkriptHook(AutoEventsPlugin plugin) {
        Objects.requireNonNull(plugin);

        initialized = false;
        this.plugin = plugin;
        skriptAddon = Skript.registerAddon(this.plugin);
    }

    public SkriptAddon getAddonInstance() {
        return skriptAddon;
    }

    public synchronized void hook() {
        if (initialized) {
            throw new RuntimeException("SkriptHook is already initialized.");
        }

        initialized = true;

        try {
            skriptAddon.loadClasses("online.wildwest.autoevents.skript", "effect", "expression");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        PluginCommand skriptCommand = Objects.requireNonNull(Skript.getInstance().getCommand("skript"));
        skriptCommand.setExecutor(new SkriptCommandHook(plugin, skriptCommand.getExecutor()));

        plugin.getLogger().info("Hooked into Skript without errors.");
    }
}
