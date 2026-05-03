package ben.spiel.gui;

import ben.spiel.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class SettingsMenu {

    private final JavaPlugin plugin;
    private final GameManager gameManager;

    public SettingsMenu(JavaPlugin plugin, GameManager gm) {
        this.plugin = plugin;
        this.gameManager = gm;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 27, "§8Force Item Battle");

        // =========================
        // GLAS DESIGN
        // =========================
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // =========================
        // ⏱ TIMER
        // =========================
        int seconds = plugin.getConfig().getInt("challenge-seconds");

        inv.setItem(10, createItem(Material.CLOCK,
                "§6Timer",
                "§7Zeit: §e" + format(seconds),
                "",
                "§7Left: §c-1 Minute",
                "§7Right: §a+1 Minute",
                "§7Shift: §a±5 Minuten"
        ));

        // =========================
        // 🟥 SKIPS
        // =========================
        int skips = plugin.getConfig().getInt("max-skips", 3);

        inv.setItem(11, createItem(Material.BARRIER,
                "§cSkips",
                "§7Max Skips: §e" + skips,
                "",
                "§7Left: §c-1",
                "§7Right: §a+1"
        ));

        // =========================
        // 🎒 BACKPACK
        // =========================
        boolean backpack = plugin.getConfig().getBoolean("enable-backpack");

        inv.setItem(12, createItem(
                backpack ? Material.LIME_DYE : Material.GRAY_DYE,
                "§aBackpack",
                "§7Status: " + (backpack ? "§aAN" : "§cAUS")
        ));

        // =========================
        // 🔒 TEAM LOCK
        // =========================
        boolean lock = plugin.getConfig().getBoolean("team-lock");

        inv.setItem(13, createItem(
                lock ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                "§cTeam Lock",
                "§7Status: " + (lock ? "§cGesperrt" : "§aOffen")
        ));

        // =========================
        // ⚡ GAME CONTROL
        // =========================

        inv.setItem(21, createItem(Material.LIME_WOOL, "§aSpiel starten"));
        inv.setItem(22, createItem(Material.RED_WOOL, "§cSpiel stoppen"));
        inv.setItem(23, createItem(Material.BARRIER, "§cReset"));

        player.openInventory(inv);
    }

    // =========================
    // ITEM BUILDER
    // =========================

    private ItemStack createItem(Material mat, String name, String... lore) {

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);
        return item;
    }

    // =========================
    // TIME FORMAT
    // =========================

    private String format(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }
}