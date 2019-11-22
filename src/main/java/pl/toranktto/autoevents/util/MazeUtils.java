package pl.toranktto.autoevents.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.inventory.ItemStack;
import pl.toranktto.autoevents.data.PluginConfig;
import pl.toranktto.autoevents.maze.Maze;

import java.util.*;

public final class MazeUtils {

    private MazeUtils() {
    }

    public static void buildMaze(PluginConfig pluginConfig, Maze maze, CuboidRegion region) {
        Objects.requireNonNull(pluginConfig);
        Objects.requireNonNull(maze);
        Objects.requireNonNull(region);

        World world = BukkitAdapter.adapt(Objects.requireNonNull(region.getWorld()));
        char[][] mazeCharMatrix = maze.toCharMatrix('*', ' ');
        ItemStack[] emptyItemStackArray = new ItemStack[0];

        for (BlockVector2 i : region.getChunks()) {
            Chunk chunk = world.getChunkAt(i.getX(), i.getZ());
            if (!chunk.isLoaded()) {
                chunk.load();
            }
        }

        for (int i = 0; i < region.getHeight(); i++) {
            int realY = i + region.getMinimumPoint().getY();
            for (int j = 0; j < mazeCharMatrix[0].length; j++) {
                int realX = j + region.getMinimumPoint().getX();
                for (int k = 0; k < mazeCharMatrix.length; k++) {
                    int realZ = k + region.getMinimumPoint().getZ();

                    Block block = world.getBlockAt(realX, realY, realZ);
                    if (block.getState() instanceof Container) {
                        Container container = (Container) block.getState();
                        container.getInventory().setContents(emptyItemStackArray);
                    }

                    if (i == 0) {
                        block.setType(pluginConfig.getMazeFloorBlock());
                        continue;
                    }

                    if (mazeCharMatrix[k][j] == '*') {
                        block.setType(pluginConfig.getMazeWallBlock());
                        if (block.getBlockData() instanceof Leaves) {
                            Leaves leaves = (Leaves) block.getBlockData();
                            leaves.setPersistent(true);
                            block.setBlockData(leaves);
                        }
                    } else /* if (mazeMatrix[k][j] == ' ') */ {
                        if (i == 1 && RandomUtils.chanceOf(pluginConfig.getMazeTreasureChance())) {
                            block.setType(pluginConfig.getMazeTreasureBlock());
                            if (block.getState() instanceof Container) {
                                Container container = (Container) block.getState();

                                List<Integer> slots = new ArrayList<>();
                                for (int l = 0; l < container.getInventory().getSize(); l++) {
                                    slots.add(l);
                                }

                                Collections.shuffle(slots);

                                Map<TreasureItem, Double> chances = new HashMap<>();
                                for (TreasureItem item : pluginConfig.getMazeTreasureItems()) {
                                    chances.put(item, item.getChance());
                                }

                                int treasureAmount = pluginConfig.getMazeTreasureAmount();
                                if (treasureAmount > container.getInventory().getSize()) {
                                    treasureAmount = container.getInventory().getSize();
                                }

                                for (int l = 0; l < treasureAmount; l++) {
                                    TreasureItem treasureItem = RandomUtils.getRandomItem(chances);
                                    if (treasureItem != null) {
                                        container.getInventory().setItem(slots.get(l), treasureItem.getItemStack());
                                    }
                                }
                            }
                        } else {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
