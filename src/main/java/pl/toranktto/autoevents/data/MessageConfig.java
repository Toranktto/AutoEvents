package pl.toranktto.autoevents.data;

import org.bukkit.configuration.file.YamlConfiguration;
import pl.toranktto.autoevents.AutoEventsPlugin;
import pl.toranktto.autoevents.util.ChatUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageConfig {

    private final AutoEventsPlugin plugin;
    private final File file;

    private Map<String, List<String>> messageMap;

    public MessageConfig(AutoEventsPlugin plugin) {
        Objects.requireNonNull(plugin);

        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "messages.yml");
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

        messageMap = new ConcurrentHashMap<>();
        if (yaml.getConfigurationSection("messages") != null) {
            yaml.getConfigurationSection("messages").getKeys(false).forEach(key -> {
                if (yaml.isList("messages." + key)) {
                    messageMap.put(key, ChatUtils.color(yaml.getStringList("messages." + key)));
                } else {
                    String message = ChatUtils.color(yaml.getString("messages." + key));
                    messageMap.put(key, message.isEmpty() ? Collections.emptyList() : Collections.singletonList(message));
                }
            });
        }
    }

    public List<String> getMessage(String id) {
        Objects.requireNonNull(id);
        return new ArrayList<>(messageMap.getOrDefault(id, Collections.singletonList(id)));
    }

    public String getSingleMessage(String id) {
        return getMessage(id).get(0);
    }
}