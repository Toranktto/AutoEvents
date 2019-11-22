package pl.toranktto.autoevents.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ChatUtils {

    private ChatUtils() {
    }

    public static String color(String message) {
        Objects.requireNonNull(message);

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> color(List<String> list) {
        Objects.requireNonNull(list);

        List<String> newList = new ArrayList<>();
        list.forEach(s -> newList.add(color(s)));
        return newList;
    }
}
