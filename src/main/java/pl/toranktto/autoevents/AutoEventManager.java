package pl.toranktto.autoevents;

import pl.toranktto.autoevents.util.RandomUtils;

import java.lang.reflect.Constructor;
import java.util.*;

public class AutoEventManager {

    private final Map<String, Class<? extends AutoEvent>> autoEvents;
    private final Map<String, Object[]> autoEventsArgs;

    private final AutoEventsPlugin plugin;
    private boolean initialized;
    private AutoEvent lastEvent;
    private int taskId;

    public AutoEventManager(AutoEventsPlugin plugin) {
        Objects.requireNonNull(plugin);

        initialized = false;
        autoEvents = new HashMap<>();
        autoEventsArgs = new HashMap<>();
        this.plugin = plugin;
        taskId = -1;
    }

    private Class<?>[] convertObjectArrayToClassArray(Object[] array) {
        Class<?>[] clazzArray = new Class<?>[array.length];
        for (int i = 0; i < array.length; i++) {
            clazzArray[i] = array[i].getClass();
        }

        return clazzArray;
    }

    private synchronized AutoEvent newEventInstance(String name) {
        Class<? extends AutoEvent> clazz = autoEvents.get(name);
        if (clazz == null) {
            throw new RuntimeException("'" + name + "' event is not registered.");
        }

        try {
            if (autoEventsArgs.containsKey(name)) {
                Constructor<? extends AutoEvent> constructor = clazz.getConstructor(convertObjectArrayToClassArray(autoEventsArgs.get(name)));
                return constructor.newInstance(autoEventsArgs.get(name));
            } else {
                Constructor<? extends AutoEvent> constructor = clazz.getConstructor(String.class);
                return constructor.newInstance(name);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getLocalizedName(String name) {
        return newEventInstance(name).getLocalizedName();
    }

    public synchronized void register(String name, Class<? extends AutoEvent> clazz, Object... constructorArgs) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(constructorArgs);

        if (autoEvents.containsKey(name)) {
            throw new RuntimeException("'" + name + "' is already registered.");
        }

        autoEvents.put(name, clazz);
        if (constructorArgs.length > 0) {
            autoEventsArgs.put(name, constructorArgs.clone());
        }

        plugin.getLogger().info("Loaded '" + name + "' event.");
    }

    public synchronized void unregister(String name) {
        Objects.requireNonNull(name);

        Class<? extends AutoEvent> clazz = autoEvents.remove(name);
        if (clazz == null) {
            throw new RuntimeException("'" + name + "' is not registered.");
        }

        autoEventsArgs.remove(name);
        plugin.getLogger().info("Unloaded '" + name + "' event.");
    }

    public synchronized Map<String, Class<? extends AutoEvent>> getRegisteredAutoEvents() {
        return Collections.unmodifiableMap(new HashMap<>(autoEvents));
    }

    public AutoEvent getLastEvent() {
        return lastEvent;
    }

    protected synchronized void stopEvent(boolean blocking) {
        if (lastEvent == null || lastEvent.getState() == AutoEvent.State.FINISHED) {
            throw new RuntimeException("There is no active event at the moment.");
        }

        lastEvent.finish(blocking);
    }

    public synchronized void stopEvent() {
        stopEvent(false);
    }

    public synchronized void startEvent(String name) {
        Objects.requireNonNull(name);

        if (lastEvent != null && lastEvent.getState() != AutoEvent.State.FINISHED) {
            throw new RuntimeException("Another event is not finished.");
        }

        AutoEvent event = newEventInstance(name);

        lastEvent = event;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for (int i = plugin.getPluginConfig().getSecondsToStart(); i >= 1; i--) {
                if (event.getState() == AutoEvent.State.FINISHED) {
                    return;
                }

                event.broadcast(i);

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ignored) {
                }
            }

            event.start();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, event::finish, event.getTimeout() * 20L);
        });
    }

    public synchronized void reload() {
        if (!initialized) {
            throw new RuntimeException("AutoEventManager is not initialized.");
        }

        plugin.getServer().getScheduler().cancelTask(taskId);
        initialized = false;

        init();
    }

    protected synchronized void init() {
        if (initialized) {
            throw new RuntimeException("AutoEventManager is already initialized.");
        }

        initialized = true;
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                return;
            }

            if (lastEvent != null && lastEvent.getState() != AutoEvent.State.FINISHED) {
                return;
            }

            Collection<String> autoStartEvents = autoEvents.keySet();
            autoStartEvents.removeAll(plugin.getPluginConfig().getDisabledAutoStartEvents());

            if (!autoStartEvents.isEmpty()) {
                startEvent(RandomUtils.getRandomItem(autoStartEvents));
            }
        }, plugin.getPluginConfig().getEventEvery() * 20L, plugin.getPluginConfig().getEventEvery() * 20L);
    }
}
