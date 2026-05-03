package ben.spiel.listener;

import ben.spiel.game.GameManager;
import ben.spiel.gui.SettingsMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

    private final String PREFIX = "§8[§eForce Item Battle§8] §7";

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

            boolean success = gameManager.getSkipManager().useSkip(player);

            if (success) {
                gameManager.getItemManager().sendSkipMessage();
                gameManager.getItemManager().nextItem();
                gameManager.updateBossBar();
                gameManager.updateArmorStands();
            } else {
                player.sendMessage(PREFIX + "§cDu hast keine Skips mehr!");
            }
            return;
        }

        // 🚂 TEAM SELECTOR
        if (event.getItem().getType() == Material.COMMAND_BLOCK_MINECART) {
            event.setCancelled(true);
            openTeamMenu(player);
        }
    }

    // =========================
    // ITEM CHECK (Inventar)
    // =========================

    @EventHandler
    public void onMoveCheck(PlayerMoveEvent event) {

        if (!gameManager.isRunning() || gameManager.isStopped()) return;

        Player player = event.getPlayer();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            if (item.getType() == gameManager.getItemManager().getCurrentItem()) {

                gameManager.getItemManager().sendSuccessMessage(player.getName());
                gameManager.getItemManager().nextItem();
                gameManager.updateBossBar();
                gameManager.updateArmorStands();
                return;
            }
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

        int team = event.getSlot() + 1;

        gameManager.getTeamManager().addToTeam(player, team);

        player.closeInventory();
    }

    // =========================
    // SETTINGS GUI (NEU)
    // =========================

    @EventHandler
    public void onSettingsClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals("§8ForceItem Settings")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        int slot = event.getSlot();

        // =========================
        // TIMER
        // =========================
        if (slot == 10) {

            int current = plugin.getConfig().getInt("challenge-seconds");

            int change = event.isShiftClick() ? 300 : 60;

            if (event.isLeftClick()) change *= -1;

            int newTime = Math.max(60, current + change);

            plugin.getConfig().set("challenge-seconds", newTime);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
        }

        // =========================
        // SKIPS
        // =========================
        if (slot == 11) {

            int current = plugin.getConfig().getInt("max-skips", 3);

            if (event.isLeftClick()) current--;
            if (event.isRightClick()) current++;

            current = Math.max(0, current);

            plugin.getConfig().set("max-skips", current);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
        }

        // =========================
        // BACKPACK
        // =========================
        if (slot == 12) {

            boolean val = plugin.getConfig().getBoolean("enable-backpack");
            plugin.getConfig().set("enable-backpack", !val);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
        }

        // =========================
        // TEAM LOCK
        // =========================
        if (slot == 13) {

            boolean val = plugin.getConfig().getBoolean("team-lock");
            plugin.getConfig().set("team-lock", !val);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
        }

        // =========================
        // RANDOM ITEMS
        // =========================
        if (slot == 14) {

            boolean val = plugin.getConfig().getBoolean("random-items", true);
            plugin.getConfig().set("random-items", !val);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
        }

        // =========================
        // START
        // =========================
        if (slot == 21) {
            gameManager.startGame();
            player.closeInventory();
        }

        // =========================
        // STOP
        // =========================
        if (slot == 22) {
            gameManager.stopGame();
            player.closeInventory();
        }

        // =========================
        // RESET
        // =========================
        if (slot == 23) {
            gameManager.restartGame();
            player.closeInventory();
        }
    }

    // =========================
    // JOIN
    // =========================

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (gameManager.isRunning()) {
            gameManager.getSkipManager().giveSkipItem(event.getPlayer());
            gameManager.updateArmorStands();
        } else {
            gameManager.getTeamManager().giveSelector(event.getPlayer());
        }
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