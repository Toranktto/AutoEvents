package pl.toranktto.autoevents;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import pl.toranktto.autoevents.command.EventCommand;
import pl.toranktto.autoevents.data.LocationStorage;
import pl.toranktto.autoevents.data.MessageConfig;
import pl.toranktto.autoevents.data.PluginConfig;
import pl.toranktto.autoevents.hook.SkriptHook;
import pl.toranktto.autoevents.listener.PlayerListener;
import pl.toranktto.autoevents.maze.MazeEvent;
import pl.toranktto.autoevents.maze.MazeListener;
import pl.toranktto.autoevents.maze.MazeRandom;

import java.util.Objects;

public final class AutoEventsPlugin extends JavaPlugin {

    public static final MazeRandom MAZE_RANDOM = new MazeRandom();

    private PluginConfig pluginConfig;
    private MessageConfig messageConfig;
    private LocationStorage locationStorage;
    private AutoEventManager autoEventManager;
    private MqttAsyncClient mqttClient;
    private MqttConnectOptions mqttOptions;
    private SkriptHook skriptHook;

    public static AutoEventsPlugin getInstance() {
        return AutoEventsPlugin.getPlugin(AutoEventsPlugin.class);
    }

    private <T extends TabExecutor> void registerCommand(String name, T command) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(command);

        PluginCommand pluginCommand = Objects.requireNonNull(getCommand(name));
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }

    private void registerListener(Listener listener) {
        Objects.requireNonNull(listener);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onEnable() {
        pluginConfig = new PluginConfig(this);
        pluginConfig.load();

        messageConfig = new MessageConfig(this);
        messageConfig.load();

        locationStorage = new LocationStorage(this);
        locationStorage.load();

        autoEventManager = new AutoEventManager(this);
        autoEventManager.register("maze", MazeEvent.class);

        autoEventManager.init();

        registerCommand("event", new EventCommand(this));
        registerListener(new PlayerListener(this));
        registerListener(new MazeListener(this));

        if (!pluginConfig.getMqttUri().isEmpty() && !pluginConfig.getMqttTopic().isEmpty()) {
            try {
                mqttClient = new MqttAsyncClient(pluginConfig.getMqttUri(), MqttAsyncClient.generateClientId(), new MemoryPersistence());
                mqttOptions = new MqttConnectOptions();
                mqttOptions.setAutomaticReconnect(true);
                if (!pluginConfig.getMqttUsername().isEmpty()) {
                    mqttOptions.setUserName(pluginConfig.getMqttUsername());
                    mqttOptions.setPassword(pluginConfig.getMqttPassword().toCharArray());
                }

                mqttClient.connect(mqttOptions);
                getLogger().info("Connected to MQTT broker without errors.");
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
        }

        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("Skript") != null) {
            skriptHook = new SkriptHook(this);
            skriptHook.hook();
        }
    }

    @Override
    public void onDisable() {
        if (autoEventManager.getLastEvent() != null && autoEventManager.getLastEvent().getState() != AutoEvent.State.FINISHED) {
            autoEventManager.stopEvent(true);
        }

        getServer().getScheduler().cancelTasks(this);
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttOptions.setAutomaticReconnect(false);
                mqttClient.disconnect();
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
        }
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public LocationStorage getLocationStorage() {
        return locationStorage;
    }

    public AutoEventManager getAutoEventManager() {
        return autoEventManager;
    }

    public MqttAsyncClient getMqttClient() {
        return mqttClient;
    }

    public SkriptHook getSkriptHook() {
        return skriptHook;
    }
}
