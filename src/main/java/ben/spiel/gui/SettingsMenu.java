package ben.spiel.gui;

import ben.spiel.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SettingsMenu {

    private final JavaPlugin plugin;
    private final GameManager gameManager;

    public SettingsMenu(JavaPlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Settings");

        int time = plugin.getConfig().getInt("challenge-seconds");
        boolean backpack = plugin.getConfig().getBoolean("enable-backpack");
        boolean teamLock = plugin.getConfig().getBoolean("team-lock");

        // ⏱ Timer
        ItemStack timer = new ItemStack(Material.CLOCK);
        ItemMeta tMeta = timer.getItemMeta();
        tMeta.setDisplayName("§eTimer: §6" + formatTime(time));
        tMeta.setLore(List.of(
                "§7Left Click: §c-1m",
                "§7Right Click: §a+1m",
                "§7Shift Left: §c-5m",
                "§7Shift Right: §a+5m"
        ));
        timer.setItemMeta(tMeta);

        // 🎒 Backpack
        ItemStack bp = new ItemStack(backpack ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta bpMeta = bp.getItemMeta();
        bpMeta.setDisplayName("§eBackpack: " + (backpack ? "§aAN" : "§cAUS"));
        bp.setItemMeta(bpMeta);

        // 🔒 Team Lock
        ItemStack lock = new ItemStack(teamLock ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK);
        ItemMeta lMeta = lock.getItemMeta();
        lMeta.setDisplayName("§eTeam Lock: " + (teamLock ? "§cAN" : "§aAUS"));
        lock.setItemMeta(lMeta);

        // ▶️ Start
        ItemStack start = new ItemStack(Material.LIME_WOOL);
        ItemMeta sMeta = start.getItemMeta();
        sMeta.setDisplayName("§aSpiel starten");
        start.setItemMeta(sMeta);

        // ⏹ Stop
        ItemStack stop = new ItemStack(Material.RED_WOOL);
        ItemMeta stMeta = stop.getItemMeta();
        stMeta.setDisplayName("§cSpiel stoppen");
        stop.setItemMeta(stMeta);

        inv.setItem(10, timer);
        inv.setItem(12, bp);
        inv.setItem(14, lock);
        inv.setItem(16, start);
        inv.setItem(22, stop);

        player.openInventory(inv);
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}