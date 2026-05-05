package ben.spiel.game;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RevealManager {

    private final JavaPlugin plugin;
    private final GameManager gameManager;

    private int revealIndex = 0;
    private boolean revealRunning = false;

    public RevealManager(JavaPlugin plugin, GameManager gm) {
        this.plugin = plugin;
        this.gameManager = gm;
    }

    // =========================
    // MAIN COMMAND
    // =========================

    public void revealNext(Player player) {

        if (revealRunning) {
            player.sendMessage("§8[§eForceItem§8] §cEin Reveal läuft bereits.");
            return;
        }

        List<Integer> ranking = gameManager.getTeamManager().getRanking();

        if (revealIndex >= 3) {
            sendLeaderboard(ranking);
            return;
        }

        int team = ranking.get(revealIndex);
        int place = revealIndex + 1;

        revealRunning = true;
        showTeam(player, team, place);

        revealIndex++;
    }

    // =========================
    // TEAM SHOW
    // =========================

    private void showTeam(Player player, int team, int place) {

        String title = "§8Reveal Platz " + place;
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<ChallengeResult> results = gameManager.getTeamManager().getResults(team);
        List<Integer> slots = revealSlots();
        Set<UUID> viewers = new HashSet<>();

        fillFrame(inv);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.openInventory(inv);
            viewers.add(onlinePlayer.getUniqueId());
        }

        new BukkitRunnable() {

            int index = 0;
            boolean animationDone = false;

            @Override
            public void run() {

                if (viewers.isEmpty()) {
                    revealRunning = false;
                    cancel();
                    return;
                }

                if (results.isEmpty()) {
                    if (!animationDone) {
                        inv.setItem(22, createInfoItem(Material.BARRIER, "§cKeine Items", List.of("§7Noch keine Items angezeigt.")));
                        playRevealSound(viewers, 0);
                        animationDone = true;
                    }

                    if (allViewersClosed(viewers, title)) {
                        showWinTitle(team, place);
                        revealRunning = false;
                        cancel();
                    }
                    return;
                }

                if (index >= results.size() || index >= slots.size()) {
                    animationDone = true;

                    if (allViewersClosed(viewers, title)) {
                        showWinTitle(team, place);
                        revealRunning = false;
                        cancel();
                    }
                    return;
                }

                int slot = slots.get(index);
                inv.setItem(slot, createResultItem(results.get(index), index + 1));

                playRevealSound(viewers, index);

                index++;
            }

        }.runTaskTimer(plugin, 0L, 6L);
    }

    private void fillFrame(Inventory inv) {
        ItemStack glass = createInfoItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());

        for (int i = 0; i < inv.getSize(); i++) {
            boolean top = i < 9;
            boolean bottom = i >= 45;
            boolean left = i % 9 == 0;
            boolean right = i % 9 == 8;

            if (top || bottom || left || right) {
                inv.setItem(i, glass);
            }
        }
    }

    private List<Integer> revealSlots() {
        return List.of(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        );
    }

    private ItemStack createResultItem(ChallengeResult result, int number) {
        ItemStack item = new ItemStack(result.material());
        ItemMeta meta = item.getItemMeta();

        String itemName = formatMaterialName(result.material());
        meta.setDisplayName((result.skipped() ? "§cSkip" : "§a#" + number) + " §7- §6" + itemName);
        meta.setLore(List.of(
                "§7Status: " + (result.skipped() ? "§cSkip" : "§aGeschafft"),
                "§7Zeit: §e" + gameManager.getTeamManager().formatDuration(result.seconds()),
                "§7Spieler: §e" + result.playerName()
        ));

        item.setItemMeta(meta);
        return item;
    }

    private void playRevealSound(Set<UUID> viewers, int index) {
        float pitch = Math.min(2.0F, 0.8F + (index * 0.05F));

        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline()) {
                viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7F, pitch);
            }
        }
    }

    private boolean allViewersClosed(Set<UUID> viewers, String title) {
        viewers.removeIf(viewerId -> {
            Player viewer = Bukkit.getPlayer(viewerId);
            return viewer == null || !viewer.isOnline();
        });

        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.getOpenInventory().getTitle().equals(title)) {
                return false;
            }
        }

        return true;
    }

    private ItemStack createInfoItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    // =========================
    // TITLE
    // =========================

    private void showWinTitle(int team, int place) {

        String teamName = gameManager.getTeamManager().getTeamTag(team) + " Team " + team;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(
                    "§6" + place + ". Platz",
                    "§e" + teamName,
                    10, 60, 20
            );
        }
    }

    // =========================
    // LEADERBOARD
    // =========================

    private void sendLeaderboard(List<Integer> ranking) {

        Bukkit.broadcastMessage("§8[§eForceItem§8] §7Leaderboard:");

        for (int i = 0; i < ranking.size(); i++) {
            int team = ranking.get(i);
            Bukkit.broadcastMessage("§e" + (i + 1) + ". §7"
                    + gameManager.getTeamManager().getTeamTag(team)
                    + " §7Team " + team
                    + " §8- §a" + gameManager.getTeamManager().getScore(team) + " Items");
        }
    }

    public void reset() {
        revealIndex = 0;
        revealRunning = false;
    }
}
