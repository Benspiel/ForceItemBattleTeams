package ben.spiel.listener;

import ben.spiel.game.GameManager;
import ben.spiel.gui.SettingsMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GameListener implements Listener {

    private final GameManager gameManager;
    private final JavaPlugin plugin;

    public GameListener(GameManager gameManager, JavaPlugin plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

    // =========================
    // INTERACT (Skip + Team Selector)
    // =========================

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getItem() == null) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        // 🟥 SKIP
        if (event.getItem().getType() == Material.BARRIER) {
            event.setCancelled(true);
            gameManager.useSkip(player);
            return;
        }

        // 🚂 TEAM SELECTOR
        if (event.getItem().getType() == Material.COMMAND_BLOCK_MINECART) {
            event.setCancelled(true);
            openTeamMenu(player);
        }
    }

    // =========================
    // TEAM GUI
    // =========================

    private void openTeamMenu(Player player) {

        Inventory inv = Bukkit.createInventory(null, 9, "§8Teams");

        Material[] beds = {
                Material.WHITE_BED,
                Material.ORANGE_BED,
                Material.MAGENTA_BED,
                Material.LIGHT_BLUE_BED,
                Material.YELLOW_BED,
                Material.LIME_BED,
                Material.PINK_BED,
                Material.RED_BED
        };

        for (int i = 1; i <= 8; i++) {

            ItemStack bed = new ItemStack(beds[i - 1]);
            ItemMeta meta = bed.getItemMeta();

            meta.setDisplayName("§eTeam " + i);

            List<String> lore = new ArrayList<>();

            List<String> players = plugin.getConfig().getStringList("teams.team" + i);

            for (String uuid : players) {
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));
                if (p != null) lore.add("§7- " + p.getName());
            }

            meta.setLore(lore);
            bed.setItemMeta(meta);

            inv.setItem(i - 1, bed);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onTeamClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals("§8Teams")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        int slot = event.getSlot();
        int team = slot + 1;

        gameManager.addToTeam(player, team);

        player.closeInventory();
    }

    // =========================
    // SETTINGS GUI
    // =========================

    @EventHandler
    public void onSettingsClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals("§8Settings")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        // ⏱ TIMER
        if (event.getCurrentItem().getType() == Material.CLOCK) {

            int current = plugin.getConfig().getInt("challenge-seconds");
            int change = 0;

            boolean shift = event.isShiftClick();

            switch (event.getClick()) {
                case LEFT -> change = shift ? -300 : -60;
                case RIGHT -> change = shift ? 300 : 60;
                default -> {}
            }

            int newTime = Math.max(60, current + change);

            plugin.getConfig().set("challenge-seconds", newTime);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
        }

        // 🎒 BACKPACK
        if (event.getCurrentItem().getType() == Material.LIME_DYE ||
                event.getCurrentItem().getType() == Material.GRAY_DYE) {

            boolean current = plugin.getConfig().getBoolean("enable-backpack");
            boolean newValue = !current;

            plugin.getConfig().set("enable-backpack", newValue);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
        }

        // 🔒 TEAM LOCK
        if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK ||
                event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {

            boolean current = plugin.getConfig().getBoolean("team-lock");
            boolean newValue = !current;

            plugin.getConfig().set("team-lock", newValue);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
        }

        // ▶ START
        if (event.getCurrentItem().getType() == Material.LIME_WOOL) {
            gameManager.startGame();
            player.closeInventory();
        }

        // ⏹ STOP
        if (event.getCurrentItem().getType() == Material.RED_WOOL) {
            gameManager.stopGame();
            player.closeInventory();
        }
    }

    // =========================
    // JOIN
    // =========================

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!gameManager.isRunning()) return;
        gameManager.addPlayerToBossBar(event.getPlayer());
    }

    // =========================
    // BLOCK BREAK LOCK
    // =========================

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (gameManager.isStopped()) {
            event.setCancelled(true);
        }
    }
}