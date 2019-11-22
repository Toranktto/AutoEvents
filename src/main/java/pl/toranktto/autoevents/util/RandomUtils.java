package pl.toranktto.autoevents.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtils {

    private RandomUtils() {
    }

    public static boolean chanceOf(double chance) {
        return chance >= 100.0D || chance >= ThreadLocalRandom.current().nextDouble() * (100D - 0D) + 0D;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getRandomItem(Collection<T> collection) {
        Objects.requireNonNull(collection);
        return (T) collection.toArray()[ThreadLocalRandom.current().nextInt(collection.size())];
    }

    public static <T> T getRandomItem(Map<T, Double> chances) {
        Objects.requireNonNull(chances);

        double chance = ThreadLocalRandom.current().nextDouble() * 100.0;
        double cumulative = 0.0;
        for (T i : chances.keySet()) {
            cumulative += chances.get(i);
            if (chance < cumulative)
                return i;
        }

        return null;
    }
}