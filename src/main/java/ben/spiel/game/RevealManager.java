package ben.spiel.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RevealManager {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final int MAX_ANIMATED_REVEALS = 3;
    private static final int AUTO_CLOSE_DELAY_TICKS = 40;
    private static final int REVEAL_START_DELAY_TICKS = 20;
    private static final int REVEAL_STEP_TICKS = 10;

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

        if (revealIndex >= MAX_ANIMATED_REVEALS) {
            sendRemainingTeams(ranking);
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
                        inv.setItem(22, createInfoItem(Material.BARRIER, "§cKeine Items", List.of("§7Hier wurden noch keine Items angezeigt.")));
                        playRevealSound(viewers, 0);
                        animationDone = true;
                        finishRevealAfterDelay(viewers, title, team, place, this);
                    }
                    return;
                }

                if (index >= results.size() || index >= slots.size()) {
                    if (!animationDone) {
                        animationDone = true;
                        finishRevealAfterDelay(viewers, title, team, place, this);
                    }
                    return;
                }

                int slot = slots.get(index);
                inv.setItem(slot, createResultItem(results.get(index), index + 1, false));

                playRevealSound(viewers, index);

                index++;
            }

        }.runTaskTimer(plugin, REVEAL_START_DELAY_TICKS, REVEAL_STEP_TICKS);
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

    private ItemStack createResultItem(ChallengeResult result, int number, boolean showPlayerName) {
        ItemStack item = new ItemStack(result.material());
        ItemMeta meta = item.getItemMeta();

        String itemName = formatMaterialName(result.material());
        meta.setDisplayName((result.skipped() ? "§cSkip" : "§a#" + number) + " §7- §6" + itemName);

        List<String> lore = new ArrayList<>();
        lore.add("§7Status: " + (result.skipped() ? "§cSkip" : "§aGeschafft"));
        lore.add("§7Zeit: §e" + gameManager.getTeamManager().formatDuration(result.seconds()));

        if (showPlayerName) {
            lore.add((result.skipped() ? "§7Geskippt von: §e" : "§7Erledigt von: §e") + result.playerName());
        }

        meta.setLore(lore);

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

    private void finishRevealAfterDelay(Set<UUID> viewers, String title, int team, int place, BukkitRunnable revealTask) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            closeRevealInventories(viewers, title);
            showWinTitle(team, place);
            revealRunning = false;
            revealTask.cancel();
        }, AUTO_CLOSE_DELAY_TICKS);
    }

    private void closeRevealInventories(Set<UUID> viewers, String title) {
        viewers.removeIf(viewerId -> {
            Player viewer = Bukkit.getPlayer(viewerId);
            return viewer == null || !viewer.isOnline();
        });

        for (UUID viewerId : viewers) {
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.getOpenInventory().getTitle().equals(title)) {
                viewer.closeInventory();
            }
        }
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
    // OVERVIEW / REMAINING TEAMS
    // =========================

    public void openOverview(Player player, int team) {
        if (team < 1 || team > 8) {
            player.sendMessage("§8[§eForceItem§8] §cDieses Team gibt es nicht.");
            return;
        }

        String title = "§8Team " + team + " Übersicht";
        Inventory inv = Bukkit.createInventory(null, 54, title);
        List<ChallengeResult> results = gameManager.getTeamManager().getResults(team);
        List<Integer> slots = revealSlots();

        fillFrame(inv);

        if (results.isEmpty()) {
            inv.setItem(22, createInfoItem(Material.BARRIER, "§cKeine Items", List.of("§7Dieses Team hat noch keine Items geschafft.")));
        } else {
            for (int i = 0; i < results.size() && i < slots.size(); i++) {
                inv.setItem(slots.get(i), createResultItem(results.get(i), i + 1, true));
            }

            if (results.size() > slots.size()) {
                inv.setItem(49, createInfoItem(Material.PAPER, "§eWeitere Items", List.of(
                        "§7Angezeigt: §e" + slots.size() + "§7/§e" + results.size(),
                        "§7Die Übersicht ist auf eine Inventarseite begrenzt."
                )));
            }
        }

        player.openInventory(inv);
    }

    private void sendRemainingTeams(List<Integer> ranking) {

        clearChat();
        Bukkit.broadcastMessage("§8[§eForceItem§8] §7Alle Teams:");
        Bukkit.broadcastMessage("");

        for (int i = 0; i < ranking.size(); i++) {
            int team = ranking.get(i);
            Component line = LEGACY.deserialize("§e" + (i + 1) + ". §7"
                            + gameManager.getTeamManager().getTeamTag(team)
                            + " §7Team " + team
                            + " §8- §a" + gameManager.getTeamManager().getScore(team) + " Items §8[")
                    .append(LEGACY.deserialize("§bÜbersicht")
                            .clickEvent(ClickEvent.runCommand("/fib overview " + team))
                            .hoverEvent(HoverEvent.showText(LEGACY.deserialize("§7Klicke, um die Items von §eTeam " + team + " §7ohne Animation anzuschauen."))))
                    .append(LEGACY.deserialize("§8]"));

            Bukkit.getServer().sendMessage(line);

            if (i == 2) {
                Bukkit.broadcastMessage("");
            }
        }
    }

    private void clearChat() {
        for (int i = 0; i < 80; i++) {
            Bukkit.broadcastMessage("");
        }
    }

    public void reset() {
        revealIndex = 0;
        revealRunning = false;
    }
}
