package pl.toranktto.autoevents.maze;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import pl.toranktto.autoevents.AutoEvent;
import pl.toranktto.autoevents.AutoEventsPlugin;
import pl.toranktto.autoevents.data.PluginConfig;
import pl.toranktto.autoevents.util.MazeUtils;

import java.util.*;

public final class MazeEvent extends AutoEvent {

    private final AutoEventsPlugin plugin;
    private final Map<Player, Location> playerLocations;
    private final Map<Player, Integer> playerTreasures;
    private final List<Block> openedTreasures;

    public MazeEvent(String name) {
        super(name);
        plugin = AutoEventsPlugin.getInstance();
        playerLocations = new HashMap<>();
        playerTreasures = new HashMap<>();
        openedTreasures = new ArrayList<>();
    }

    public synchronized void incrementPlayerTreasures(Player player, Block block) {
        if (openedTreasures.contains(block)) {
            return;
        }

        openedTreasures.add(block);
        playerTreasures.put(player, playerTreasures.getOrDefault(player, 0) + 1);
    }

    public synchronized Player getWinner() {
        if (playerTreasures.isEmpty()) {
            return null;
        }

        List<Player> players = new ArrayList<>(playerTreasures.keySet());
        players.sort(Comparator.<Player>comparingInt(playerTreasures::get).reversed());
        return players.get(0);
    }

    @Override
    public long getTimeout() {
        return plugin.getPluginConfig().getMazeTimeout();
    }

    @Override
    public void onLeave(Player player, LeaveCause cause) {
        if (getState() == State.STARTED && getPlayers().isEmpty()) {
            finish();
        }

        if (playerLocations.containsKey(player)) {
            Location location = playerLocations.remove(player);
            if (cause != LeaveCause.TELEPORT && cause != LeaveCause.DEATH) {
                player.teleport(location);
            }
        }
    }

    @Override
    public void onJoin(Player player) {
    }

    @Override
    public void onFinish() {
        if (!playerTreasures.isEmpty()) {
            Player winner = Objects.requireNonNull(getWinner());
            plugin.getMessageConfig().getMessage("maze-broadcast-winner").forEach(s -> {
                plugin.getServer().broadcastMessage(s
                        .replace("%name%", getLocalizedName())
                        .replace("%player%", winner.getName())
                        .replace("%number%", String.valueOf(playerTreasures.get(winner)))
                );
            });
        }

        playerTreasures.clear();
        openedTreasures.clear();
    }

    @Override
    public void onStart() {
        if (getPlayers().size() < plugin.getPluginConfig().getMazeMinPlayers()) {
            plugin.getMessageConfig().getMessage("maze-broadcast-no-players").forEach(s -> {
                plugin.getServer().broadcastMessage(s
                        .replace("%name%", getLocalizedName())
                );
            });
            finish();
            return;
        }

        PluginConfig pluginConfig = plugin.getPluginConfig();
        World world = plugin.getServer().getWorld(pluginConfig.getMazeRegionWorld());
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));

        ProtectedRegion protectedRegion = Objects.requireNonNull(regionManager).getRegion(pluginConfig.getMazeRegionName());
        if (!(protectedRegion instanceof ProtectedCuboidRegion)) {
            throw new RuntimeException("'" + pluginConfig.getMazeRegionName() + "' is not cuboid region.");
        }

        ProtectedCuboidRegion protectedCuboidRegion = (ProtectedCuboidRegion) protectedRegion;
        CuboidRegion cuboidRegion = new CuboidRegion(BukkitAdapter.adapt(world), protectedCuboidRegion.getMinimumPoint(), protectedCuboidRegion.getMaximumPoint());

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int x = (int) Math.ceil((cuboidRegion.getLength() - 1) / 2D);
            int y = (int) Math.ceil((cuboidRegion.getWidth() - 1) / 2D);
            Maze maze = AutoEventsPlugin.MAZE_RANDOM.nextMaze(x, y);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Location location = plugin.getLocationStorage().get("maze-hub");
                if (location == null) {
                    finish();
                    throw new RuntimeException("'maze-hub' is not defined. See /event setloc command.");
                }

                MazeUtils.buildMaze(pluginConfig, maze, cuboidRegion);

                for (Player i : getPlayers()) {
                    playerLocations.put(i, i.getLocation().clone());
                    i.teleport(location);
                }
            });
        });
    }
}
