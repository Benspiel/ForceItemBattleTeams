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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameListener implements Listener {

    private static final String PREFIX = "§8[§eForceItem§8] §7";

    private final GameManager gameManager;
    private final JavaPlugin plugin;

    public GameListener(GameManager gameManager, JavaPlugin plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getItem() == null) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (event.getItem().getType() == Material.BARRIER) {
            event.setCancelled(true);

            boolean success = gameManager.getSkipManager().useSkip(player);

            if (success) {
                gameManager.skipChallenge(player);
            } else {
                player.sendMessage(PREFIX + "§cDu hast keine Skips mehr!");
            }
            return;
        }

        if (event.getItem().getType() == Material.COMMAND_BLOCK_MINECART) {
            event.setCancelled(true);
            openTeamMenu(player);
        }
    }

    @EventHandler
    public void onMoveCheck(PlayerMoveEvent event) {

        if (!gameManager.isRunning() || gameManager.isStopped()) return;
        if (event.getTo() == null) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        checkInventory(event.getPlayer());
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {

        if (!gameManager.isRunning() || gameManager.isStopped()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack pickedUp = event.getItem().getItemStack();
        if (pickedUp.getType() == gameManager.getItemManager().getCurrentItem()) {
            Bukkit.getScheduler().runTask(plugin, () -> checkInventory(player));
        }
    }

    private void checkInventory(Player player) {
        gameManager.checkPlayerInventory(player);
    }

    private void openTeamMenu(Player player) {

        Inventory inv = Bukkit.createInventory(null, 9, "§8Teams");
        boolean teamLock = plugin.getConfig().getBoolean("team-lock", false);

        for (int i = 1; i <= 8; i++) {

            ItemStack teamItem = new ItemStack(gameManager.getTeamManager().getTeamWool(i));
            ItemMeta meta = teamItem.getItemMeta();

            meta.setDisplayName(gameManager.getTeamManager().getTeamTag(i) + " §eTeam " + i);

            List<String> lore = new ArrayList<>();
            if (teamLock) {
                lore.add("§cTeam-Lock aktiv");
                lore.add("§7Du kannst die Teams anschauen, aber nicht wechseln.");
                lore.add("");
            } else {
                lore.add("§aKlicken zum Wechseln");
                lore.add("");
            }

            List<String> players = plugin.getConfig().getStringList("teams.team" + i);

            for (String uuid : players) {
                Player p;
                try {
                    p = Bukkit.getPlayer(UUID.fromString(uuid));
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

                if (p != null) lore.add("§7- " + gameManager.getTeamManager().formatPlayerName(p));
            }

            meta.setLore(lore);
            teamItem.setItemMeta(meta);

            inv.setItem(i - 1, teamItem);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onTeamClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals("§8Teams")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 9) return;

        Player player = (Player) event.getWhoClicked();

        if (plugin.getConfig().getBoolean("team-lock", false)) {
            player.sendMessage(PREFIX + "§cTeam-Wechsel sind gesperrt!");
            player.closeInventory();
            return;
        }

        int team = slot + 1;

        gameManager.getTeamManager().addToTeam(player, team);

        player.closeInventory();
    }

    @EventHandler
    public void onProtectedMenuClick(InventoryClickEvent event) {
        if (!isProtectedReadOnlyMenu(event.getView().getTitle())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onProtectedMenuDrag(InventoryDragEvent event) {
        if (!isProtectedReadOnlyMenu(event.getView().getTitle())) return;

        int topSize = event.getView().getTopInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isProtectedReadOnlyMenu(String title) {
        return title.startsWith("§8Reveal Platz ") || title.contains("§8Team ") && title.contains(" Übersicht");
    }

    @EventHandler
    public void onSettingsClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!event.getView().getTitle().equals("§8Force Item Battle")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType().isAir()) return;

        int slot = event.getRawSlot();

        if (slot >= event.getView().getTopInventory().getSize()) return;

        if (slot == 10) {
            int current = plugin.getConfig().getInt("challenge-seconds", 300);

            int change = event.isShiftClick() ? 300 : 60;
            if (event.isLeftClick()) change *= -1;

            int newTime = Math.max(60, current + change);

            plugin.getConfig().set("challenge-seconds", newTime);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
            return;
        }

        if (slot == 11) {
            int current = plugin.getConfig().getInt("max-skips", 3);

            if (event.isLeftClick()) current--;
            if (event.isRightClick()) current++;

            current = Math.max(0, current);

            plugin.getConfig().set("max-skips", current);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
            return;
        }

        if (slot == 12) {
            boolean val = plugin.getConfig().getBoolean("enable-backpack", true);

            plugin.getConfig().set("enable-backpack", !val);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
            return;
        }

        if (slot == 13) {
            boolean val = plugin.getConfig().getBoolean("team-lock", false);

            plugin.getConfig().set("team-lock", !val);
            plugin.saveConfig();

            new SettingsMenu(plugin, gameManager).open(player);
            return;
        }

        if (slot == 21) {
            gameManager.startGame();
            player.closeInventory();
            return;
        }

        if (slot == 22) {
            gameManager.stopGame();
            player.closeInventory();
            return;
        }

        if (slot == 23) {
            gameManager.restartGame();
            player.closeInventory();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (gameManager.isRunning()) {
            if (gameManager.getTeamManager().getTeam(event.getPlayer()) == -1) {
                gameManager.getTeamManager().addToTeam(event.getPlayer(), 1);
            }

            gameManager.getSkipManager().giveSkipItem(event.getPlayer());
            gameManager.updateArmorStands();
        } else {
            gameManager.getTeamManager().giveSelector(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        gameManager.removeDisplay(event.getPlayer());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (gameManager.isStopped()) {
            event.setCancelled(true);
        }
    }
}
