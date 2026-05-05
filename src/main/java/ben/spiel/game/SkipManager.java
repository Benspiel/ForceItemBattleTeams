package ben.spiel.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class SkipManager {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Integer> skips = new HashMap<>();

    private static final String PREFIX = "§8[§eForceItem§8] §7";

    public SkipManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // =========================
    // GIVE SKIPS
    // =========================

    public void giveSkipItem(Player p) {
        skips.put(p.getUniqueId(), getMaxSkips());
        giveItem(p);
    }

    // =========================
    // USE SKIP (FIXED)
    // =========================

    public boolean useSkip(Player p) {

        int left = skips.getOrDefault(p.getUniqueId(), 0);

        if (left <= 0) return false;

        left--;
        skips.put(p.getUniqueId(), left);

        giveItem(p);

        // ✅ EINZIGE Nachricht
        p.sendMessage(PREFIX + "§eSkip benutzt! §7(" + left + " übrig)");

        return true;
    }

    // =========================
    // ITEM UPDATE
    // =========================

    private void giveItem(Player p) {

        p.getInventory().remove(Material.BARRIER);

        int left = skips.getOrDefault(p.getUniqueId(), 0);

        if (left <= 0) {
            return;
        }

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

    private int getMaxSkips() {
        return Math.max(0, plugin.getConfig().getInt("max-skips", 3));
    }
}
