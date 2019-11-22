package pl.toranktto.autoevents.maze;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.toranktto.autoevents.AutoEvent;
import pl.toranktto.autoevents.AutoEventsPlugin;
import pl.toranktto.autoevents.util.RandomUtils;
import pl.toranktto.autoevents.util.TreasureItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MazeListener implements Listener {

    private final AutoEventsPlugin plugin;

    public MazeListener(AutoEventsPlugin plugin) {
        Objects.requireNonNull(plugin);

        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        AutoEvent lastEvent = plugin.getAutoEventManager().getLastEvent();
        if (lastEvent instanceof MazeEvent && lastEvent.getState() == AutoEvent.State.STARTED) {
            Block clickedBlock = event.getClickedBlock();

            World world = plugin.getServer().getWorld(plugin.getPluginConfig().getMazeRegionWorld());
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            ProtectedRegion protectedRegion = Objects.requireNonNull(regionManager).getRegion(plugin.getPluginConfig().getMazeRegionName());
            if (protectedRegion == null) {
                return;
            }

            if (!protectedRegion.contains(clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ())) {
                return;
            }

            if (clickedBlock.getType() == plugin.getPluginConfig().getMazeTreasureBlock()) {
                ((MazeEvent) lastEvent).incrementPlayerTreasures(event.getPlayer(), clickedBlock);
                if (clickedBlock.getState() instanceof Container) {
                    return;
                }

                clickedBlock.setType(Material.AIR);

                Map<TreasureItem, Double> chances = new HashMap<>();
                for (TreasureItem item : plugin.getPluginConfig().getMazeTreasureItems()) {
                    chances.put(item, item.getChance());
                }

                for (int i = 0; i < plugin.getPluginConfig().getMazeTreasureAmount(); i++) {
                    TreasureItem treasureItem = RandomUtils.getRandomItem(chances);
                    if (treasureItem != null) {
                        clickedBlock.getLocation().getWorld().dropItemNaturally(clickedBlock.getLocation(), treasureItem.getItemStack());
                    }
                }
            }
        }
    }
}
