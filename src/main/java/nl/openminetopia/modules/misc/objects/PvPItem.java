package nl.openminetopia.modules.misc.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Data
@Accessors(fluent = true)
public class PvPItem {
    private final ItemStack item;
    private final String attackerMessage;
    private final String victimMessage;

    public String attackerMessage() {
        return attackerMessage.replace("<item>", item.getType().name());
    }

    public String victimMessage() {
        return victimMessage.replace("<item>", item.getType().name());
    }

    public boolean isSimilar(ItemStack itemToCheck) {
        if (item == null || itemToCheck == null) return false;
        if (item.getType() != itemToCheck.getType()) return false;

        if (item.hasItemMeta() != itemToCheck.hasItemMeta()) return false;

        if (!item.hasItemMeta()) return true; // Both have no meta, so they match at this point

        var meta = item.getItemMeta();
        var checkMeta = itemToCheck.getItemMeta();
        if (meta == null || checkMeta == null) return false;

        if (meta.hasCustomModelData() != checkMeta.hasCustomModelData()) return false;
        if (meta.hasCustomModelData() && checkMeta.getCustomModelData() != meta.getCustomModelData()) return false;

        if (Bukkit.getVersion().contains("1.21.4") && meta.hasItemModel() && checkMeta.hasItemModel()) {
            return meta.getItemModel() == checkMeta.getItemModel();
        }

        return true;
    }
}
