package pl.toranktto.autoevents;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.*;

public abstract class AutoEvent {

    private final String name;
    private final Set<Player> players;
    private State state;

    public AutoEvent(String name) {
        Objects.requireNonNull(name);

        this.name = name;
        players = new HashSet<>();
        state = State.STARTING;
    }

    private void publishMqtt0(String messageId, int seconds) {
        AutoEventsPlugin plugin = AutoEventsPlugin.getInstance();
        MqttAsyncClient mqttClient = plugin.getMqttClient();
        if (mqttClient == null || !mqttClient.isConnected()) {
            return;
        }

        List<String> messages = plugin.getMessageConfig().getMessage(messageId);
        messages.forEach(s -> {
            try {
                mqttClient.publish(plugin.getPluginConfig().getMqttTopic(), new MqttMessage(s
                        .replace("%name%", getLocalizedName())
                        .replace("%seconds%", String.valueOf(seconds)).getBytes()));
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void publishMqtt(String messageId, int seconds, boolean blocking) {
        Objects.requireNonNull(messageId);

        if (blocking) {
            publishMqtt0(messageId, seconds);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(AutoEventsPlugin.getInstance(), () -> {
                publishMqtt0(messageId, seconds);
            });
        }
    }

    public String getLocalizedName() {
        return AutoEventsPlugin.getInstance().getMessageConfig().getSingleMessage(name);
    }

    public final String getName() {
        return name;
    }

    protected synchronized final void start() {
        if (state != State.STARTING) {
            throw new RuntimeException("AutoEvent is already started.");
        }

        state = State.STARTED;
        Bukkit.getScheduler().runTask(AutoEventsPlugin.getInstance(), () -> {
            try {
                onStart();
            } catch (Exception ex) {
                ex.printStackTrace();
                finish(true);
            }

            if (state == State.STARTED) {
                AutoEventsPlugin.getInstance().getMessageConfig().getMessage("event-broadcast-started").forEach((s) -> {
                    Bukkit.broadcastMessage(s.replace("%name%", getLocalizedName()));
                });
            }
        });
        publishMqtt("mqtt-event-started", -1, false);
    }

    private synchronized void finish0(boolean blocking) {
        AutoEventsPlugin.getInstance().getMessageConfig().getMessage("event-broadcast-finished").forEach((s) -> {
            Bukkit.broadcastMessage(s.replace("%name%", getLocalizedName()));
        });

        try {
            onFinish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        players.forEach(p -> leave(p, LeaveCause.FINISH, true));
        publishMqtt("mqtt-event-finished", -1, blocking);
    }

    protected synchronized final void finish(boolean blocking) {
        if (state == State.FINISHED) {
            return;
        }

        state = State.FINISHED;
        if (blocking) {
            finish0(true);
        } else {
            Bukkit.getScheduler().runTask(AutoEventsPlugin.getInstance(), () -> {
                finish0(false);
            });
        }
    }

    protected synchronized final void finish() {
        finish(false);
    }

    public final State getState() {
        return state;
    }

    public synchronized final void join(Player player) {
        Objects.requireNonNull(player);

        if (state != State.STARTING) {
            throw new RuntimeException("AutoEvent is already started.");
        }

        if (players.contains(player)) {
            throw new RuntimeException("'" + player.getName() + "' is already joined to this event.");
        }

        if (!canJoin(player)) {
            throw new RuntimeException("'" + player.getName() + "' can't join to this event.");
        }

        players.add(player);
        Bukkit.getScheduler().runTask(AutoEventsPlugin.getInstance(), () -> {
            try {
                onJoin(player);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private synchronized void leave(Player player, LeaveCause cause, boolean blocking) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(cause);

        if (!players.contains(player)) {
            throw new RuntimeException("'" + player.getName() + "' is not joined to this event.");
        }

        players.remove(player);
        if (blocking) {
            try {
                onLeave(player, cause);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Bukkit.getScheduler().runTask(AutoEventsPlugin.getInstance(), () -> {
                try {
                    onLeave(player, cause);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    public synchronized final void leave(Player player, LeaveCause cause) {
        leave(player, cause, false);
    }

    public synchronized final Set<Player> getPlayers() {
        return Collections.unmodifiableSet(new HashSet<>(players));
    }

    protected final void broadcast(int seconds) {
        if (seconds % 30 == 0) {
            AutoEventsPlugin.getInstance().getMessageConfig().getMessage("event-broadcast-start").forEach(s -> {
                Bukkit.broadcastMessage(s
                        .replace("%name%", getLocalizedName())
                        .replace("%seconds%", String.valueOf(seconds)));
            });
            publishMqtt("mqtt-event-broadcast-start", seconds, false);
        }
    }

    public boolean canJoin(Player player) {
        return true;
    }

    public abstract long getTimeout();

    public abstract void onStart();

    public abstract void onLeave(Player player, LeaveCause cause);

    public abstract void onJoin(Player player);

    public abstract void onFinish();

    public enum State {
        STARTING,
        STARTED,
        FINISHED
    }

    public enum LeaveCause {
        COMMAND,
        TELEPORT,
        DEATH,
        QUIT,
        FINISH,
        OTHER
    }
}
