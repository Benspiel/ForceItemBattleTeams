package ben.spiel.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class SkipManager {

    private final HashMap<UUID, Integer> skips = new HashMap<>();

    private final String PREFIX = "§8[§6Force Item Battle§8] §r";

    // =========================
    // GIVE SKIP ITEM
    // =========================

    public void giveSkipItem(Player p) {
        skips.put(p.getUniqueId(), 3);
        giveItem(p);
    }

    // =========================
    // USE SKIP
    // =========================

    public boolean useSkip(Player p) {

        int left = skips.getOrDefault(p.getUniqueId(), 0);

        if (left <= 0) return false;

        left--;
        skips.put(p.getUniqueId(), left);

        giveItem(p);

        // 📢 Erfolg Nachricht
        p.sendMessage(PREFIX + "§eSkip benutzt! §7(" + left + " übrig)");

        return true;
    }

    // =========================
    // ITEM UPDATE
    // =========================

    private void giveItem(Player p) {

        // alte entfernen
        p.getInventory().remove(Material.BARRIER);

        int left = skips.getOrDefault(p.getUniqueId(), 0);

        ItemStack item = new ItemStack(Material.BARRIER);
        var meta = item.getItemMeta();
        meta.setDisplayName("§cItem Skip (§e" + left + "§c)");
        item.setItemMeta(meta);

        p.getInventory().addItem(item);
    }

    // =========================
    // GETTER
    // =========================

    public int getSkips(Player p) {
        return skips.getOrDefault(p.getUniqueId(), 0);
    }
}