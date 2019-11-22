package pl.toranktto.autoevents.skript;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.toranktto.autoevents.AutoEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class SkriptAutoEvent extends AutoEvent {

    private final String localizedName;
    private final String command;
    private final long timeout;

    public SkriptAutoEvent(String name, String localizedName, String command, Long timeout) {
        super(name);
        Objects.requireNonNull(localizedName);
        Objects.requireNonNull(command);

        this.localizedName = localizedName;
        this.command = command;
        this.timeout = timeout;
    }

    private void dispatchCommand(String subCommand, String... args) {
        List<String> commandArgs = new ArrayList<>(Arrays.asList(command, subCommand));
        commandArgs.addAll(Arrays.asList(args));

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.join(" ", commandArgs));
    }

    @Override
    public String getLocalizedName() {
        return localizedName;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void onStart() {
        dispatchCommand("start");
    }

    @Override
    public void onLeave(Player player, LeaveCause cause) {
        dispatchCommand("leave", player.getName(), cause.name());
    }

    @Override
    public void onJoin(Player player) {
        dispatchCommand("join", player.getName());
    }

    @Override
    public void onFinish() {
        dispatchCommand("finish");
    }
}