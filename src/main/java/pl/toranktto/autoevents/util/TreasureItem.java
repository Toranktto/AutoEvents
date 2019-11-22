package pl.toranktto.autoevents.util;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class TreasureItem {

    private final ItemStack itemStack;
    private final double chance;

    public TreasureItem(ItemStack itemStack, double chance) {
        Objects.requireNonNull(itemStack);

        this.itemStack = itemStack;
        this.chance = chance;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getChance() {
        return chance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreasureItem that = (TreasureItem) o;
        return Double.compare(that.chance, chance) == 0 &&
                Objects.equals(itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemStack, chance);
    }
}
