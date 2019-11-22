package pl.toranktto.autoevents.data;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.toranktto.autoevents.AutoEventsPlugin;
import pl.toranktto.autoevents.util.ChatUtils;
import pl.toranktto.autoevents.util.TimeUtils;
import pl.toranktto.autoevents.util.TreasureItem;

import java.io.File;
import java.util.*;

public class PluginConfig {

    private final AutoEventsPlugin plugin;
    private final File file;

    private int secondsToStart;
    private long eventEvery;
    private String mqttUri;
    private String mqttTopic;
    private String mqttUsername;
    private String mqttPassword;
    private Collection<String> disabledAutoStartEvents;

    private int mazeMinPlayers;
    private String mazeRegionName;
    private String mazeRegionWorld;
    private Material mazeWallBlock;
    private Material mazeFloorBlock;
    private Material mazeTreasureBlock;
    private double mazeTreasureChance;
    private int mazeTreasureAmount;
    private Collection<TreasureItem> mazeTreasureItems;
    private long mazeTimeout;

    public PluginConfig(AutoEventsPlugin plugin) {
        Objects.requireNonNull(plugin);

        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "config.yml");
    }

    private Map<Enchantment, Integer> parseEnchants(ConfigurationSection section) {
        Map<Enchantment, Integer> enchants = new HashMap<>();

        for (String i : section.getStringList("enchants")) {
            String[] token = i.split(" ");
            Enchantment enchant;
            int level;

            if (token.length == 0) {
                continue;
            }

            if (token.length >= 2) {
                try {
                    level = Integer.parseInt(token[1]);
                } catch (NumberFormatException ex) {
                    continue;
                }
            } else {
                level = 1;
            }

            try {
                enchant = Enchantment.getByKey(NamespacedKey.minecraft(token[0].toLowerCase()));
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
                continue;
            }

            if (enchant == null) {
                continue;
            }

            enchants.put(enchant, level);
        }

        return enchants;
    }

    private ItemStack parseItem(ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("id", "minecraft:air"));
        String itemName = ChatUtils.color(section.getString("name", ""));
        List<String> itemLore = ChatUtils.color(section.getStringList("lore"));
        Map<Enchantment, Integer> enchants = parseEnchants(section);

        ItemStack itemStack = new ItemStack(material);
        if (!itemName.isEmpty() && !itemStack.getType().isAir()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(itemName);
            itemStack.setItemMeta(itemMeta);
        }

        if (!itemLore.isEmpty() && !itemStack.getType().isAir()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setLore(itemLore);
            itemStack.setItemMeta(itemMeta);
        }

        itemStack.addUnsafeEnchantments(enchants);
        return itemStack;
    }

    private TreasureItem parseTreasureItem(ConfigurationSection section) {
        ItemStack itemStack = parseItem(section);
        double chance = section.getDouble("chance", 0);

        return new TreasureItem(itemStack, chance);
    }

    public synchronized void load() {
        if (!file.exists()) {
            try {
                plugin.saveResource(file.getName(), true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        secondsToStart = yaml.getInt("config.seconds-to-start", 60);
        eventEvery = TimeUtils.parseTime(yaml.getString("config.event-every"));
        mqttUri = yaml.getString("config.mqtt-uri", "");
        mqttTopic = yaml.getString("config.mqtt-topic", "event");
        mqttUsername = yaml.getString("config.mqtt-username", "");
        mqttPassword = yaml.getString("config.mqtt-password", "");
        disabledAutoStartEvents = yaml.getStringList("config.disabled-autostart-events");

        mazeRegionName = yaml.getString("config.maze.region-name");
        mazeRegionWorld = yaml.getString("config.maze.region-world");
        mazeMinPlayers = yaml.getInt("config.maze.min-players");
        mazeWallBlock = Material.matchMaterial(yaml.getString("config.maze.wall-block", "minecraft:air"));
        mazeFloorBlock = Material.matchMaterial(yaml.getString("config.maze.floor-block", "minecraft:air"));
        mazeTreasureBlock = Material.matchMaterial(yaml.getString("config.maze.treasure-block", "minecraft:air"));
        mazeTreasureChance = yaml.getDouble("config.maze.treasure-chance", 0);
        mazeTreasureAmount = yaml.getInt("config.maze.treasure-amount", 1);
        mazeTreasureItems = new HashSet<>();
        if (yaml.getConfigurationSection("config.maze.treasure-items") != null) {
            yaml.getConfigurationSection("config.maze.treasure-items").getKeys(false).forEach(key -> {
                mazeTreasureItems.add(parseTreasureItem(yaml.getConfigurationSection("config.maze.treasure-items." + key)));
            });
        }
        mazeTimeout = TimeUtils.parseTime(yaml.getString("config.maze.timeout", ""));
    }


    public String getMazeRegionName() {
        return mazeRegionName;
    }

    public String getMazeRegionWorld() {
        return mazeRegionWorld;
    }

    public Material getMazeWallBlock() {
        return mazeWallBlock;
    }

    public Material getMazeFloorBlock() {
        return mazeFloorBlock;
    }

    public Material getMazeTreasureBlock() {
        return mazeTreasureBlock;
    }

    public synchronized Collection<TreasureItem> getMazeTreasureItems() {
        return Collections.unmodifiableSet(new HashSet<>(mazeTreasureItems));
    }

    public double getMazeTreasureChance() {
        return mazeTreasureChance;
    }

    public int getMazeTreasureAmount() {
        return mazeTreasureAmount;
    }

    public long getMazeTimeout() {
        return mazeTimeout;
    }

    public int getSecondsToStart() {
        return secondsToStart;
    }

    public int getMazeMinPlayers() {
        return mazeMinPlayers;
    }

    public long getEventEvery() {
        return eventEvery;
    }

    public String getMqttUri() {
        return mqttUri;
    }

    public String getMqttTopic() {
        return mqttTopic;
    }

    public String getMqttUsername() {
        return mqttUsername;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public synchronized Collection<String> getDisabledAutoStartEvents() {
        return Collections.unmodifiableSet(new HashSet<>(disabledAutoStartEvents));
    }
}