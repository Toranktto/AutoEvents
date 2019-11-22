package pl.toranktto.autoevents.data;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.toranktto.autoevents.AutoEventsPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class LocationStorage {

    private final AutoEventsPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    private Map<String, Location> locationMap;

    public LocationStorage(AutoEventsPlugin plugin) {
        Objects.requireNonNull(plugin);

        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "locations.yml");
    }

    public synchronized void load() {
        if (!file.exists()) {
            try {
                plugin.saveResource(file.getName(), true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        yaml = YamlConfiguration.loadConfiguration(file);

        locationMap = new ConcurrentHashMap<>();
        if (yaml.getConfigurationSection("locations") != null) {
            yaml.getConfigurationSection("locations").getKeys(false).forEach(key -> {
                locationMap.put(key, yaml.getLocation("locations." + key));
            });
        }
    }

    public synchronized void save() {
        for (Map.Entry<String, Location> i : locationMap.entrySet()) {
            yaml.set("locations." + i.getKey(), i.getValue());
        }

        try {
            yaml.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Location get(String id) {
        Objects.requireNonNull(id);
        return locationMap.containsKey(id) ? locationMap.get(id).clone() : null;
    }

    public void set(String id, Location location) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(location);

        locationMap.put(id, location.clone());
    }
}