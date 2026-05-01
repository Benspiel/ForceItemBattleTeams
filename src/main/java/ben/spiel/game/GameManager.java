package ben.spiel.game;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.boss.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class GameManager {

    private final JavaPlugin plugin;
    private final String PREFIX = "§8[§6Force Item Battle§8] §r";

    private boolean running = false;
    private boolean stopped = false;

    private Material currentItem;
    private final Random random = new Random();

    private BossBar bossBar;
    private int timeLeft;

    private final Set<Material> blacklist = new HashSet<>();
    private final Map<UUID, Integer> skips = new HashMap<>();

    // 📊 Scoreboard
    private Scoreboard board;
    private Objective objective;

    public GameManager(JavaPlugin plugin) {
        this.plugin = plugin;

        for (String s : plugin.getConfig().getStringList("blacklist")) {
            try {
                blacklist.add(Material.valueOf(s));
            } catch (Exception ignored) {}
        }
    }

    // =========================
    // GAME CONTROL
    // =========================

    public void startGame() {
        running = true;
        stopped = false;

        timeLeft = plugin.getConfig().getInt("challenge-seconds");

        bossBar = Bukkit.createBossBar(
                "§eWarte auf Item...",
                BarColor.YELLOW,
                BarStyle.SOLID
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();

            bossBar.addPlayer(p);

            skips.put(p.getUniqueId(), 3);
            giveOrUpdateSkipItem(p);
        }

        setupScoreboard();
        nextItem();
        startTimer();
        startInventoryCheck();
    }

    public void stopGame() {
        stopped = true;

        if (bossBar != null) {
            bossBar.setTitle("§cTime: Stopped");
            bossBar.setProgress(0);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
        }

        updateScoreboard();

        Bukkit.broadcastMessage(PREFIX + "§cSpiel gestoppt!");
    }

    public void restartGame() {

        // 🔴 Auto Stop
        stopGame();

        running = false;
        stopped = false;

        // 🧹 BossBar entfernen
        removeBossBar();

        // 🧠 Teams reset
        for (int i = 1; i <= 8; i++) {
            plugin.getConfig().set("teams.team" + i, new ArrayList<>());
        }

        plugin.getConfig().set("team-lock", false);
        plugin.saveConfig();

        // 🧹 Spieler reset
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            giveTeamSelector(p);
        }

        Bukkit.broadcastMessage(PREFIX + "§eSpiel wurde zurückgesetzt!");
    }

    // =========================
    // BOSSBAR REMOVE
    // =========================

    private void removeBossBar() {
        if (bossBar == null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.removePlayer(p);
        }

        bossBar.setVisible(false);
        bossBar = null;
    }

    // =========================
    // SCOREBOARD
    // =========================

    public void setupScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();

        objective = board.registerNewObjective("fib", "dummy", "§6Force Item Battle");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    public void updateScoreboard() {

        if (board == null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {

            board.getEntries().forEach(board::resetScores);

            String item = (currentItem != null) ? getNiceName(currentItem) : "-";
            String time = stopped ? "Stopped" : formatTime(timeLeft);
            String team = getPlayerTeam(p);
            int skip = skips.getOrDefault(p.getUniqueId(), 0);
            String status = stopped ? "§cStopped" : "§aRunning";

            objective.getScore("§7").setScore(9);
            objective.getScore("§eItem: §f" + item).setScore(8);
            objective.getScore("§eZeit: §f" + time).setScore(7);
            objective.getScore("§eTeam: §f" + team).setScore(6);
            objective.getScore("§eSkips: §f" + skip).setScore(5);
            objective.getScore("§eStatus: " + status).setScore(4);
        }
    }

    private String getPlayerTeam(Player player) {
        for (int i = 1; i <= 8; i++) {
            List<String> list = plugin.getConfig().getStringList("teams.team" + i);

            if (list.contains(player.getUniqueId().toString())) {
                return "Team " + i;
            }
        }
        return "-";
    }

    // =========================
    // TIMER
    // =========================

    private void startTimer() {
        int totalTime = plugin.getConfig().getInt("challenge-seconds");

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!running || stopped) {
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    stopGame();
                    cancel();
                    return;
                }

                double progress = (double) timeLeft / totalTime;
                bossBar.setProgress(progress);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(Component.text("§eZeit: §6" + formatTime(timeLeft)));
                }

                timeLeft--;
                updateScoreboard();
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    // =========================
    // ITEM SYSTEM
    // =========================

    public void nextItem() {
        Material[] materials = Material.values();

        do {
            currentItem = materials[random.nextInt(materials.length)];
        } while (!currentItem.isItem() || blacklist.contains(currentItem));

        String itemName = getNiceName(currentItem);

        bossBar.setTitle("§6Item: §e" + itemName);
        Bukkit.broadcastMessage(PREFIX + "§6Neues Item: §e" + itemName);

        updateScoreboard();
    }

    private void startInventoryCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!running || stopped) {
                    cancel();
                    return;
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    checkPlayer(p);
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    public void checkPlayer(Player player) {
        if (!running || stopped || currentItem == null) return;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            if (item.getType() == currentItem) {
                Bukkit.broadcastMessage(PREFIX + "§a" + player.getName() + " hat das Item!");
                nextItem();
                return;
            }
        }
    }

    // =========================
    // SKIP SYSTEM
    // =========================

    public void useSkip(Player player) {
        int left = skips.getOrDefault(player.getUniqueId(), 0);

        if (left <= 0) return;

        left--;
        skips.put(player.getUniqueId(), left);

        giveOrUpdateSkipItem(player);
        nextItem();

        updateScoreboard();
    }

    private void giveOrUpdateSkipItem(Player player) {
        int left = skips.getOrDefault(player.getUniqueId(), 0);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BARRIER) {
                player.getInventory().remove(item);
            }
        }

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        meta.setDisplayName("§cItem Skip (§e" + left + "§c)");
        barrier.setItemMeta(meta);

        player.getInventory().addItem(barrier);
    }

    // =========================
    // TEAM SYSTEM
    // =========================

    public void addToTeam(Player player, int team) {

        if (plugin.getConfig().getBoolean("team-lock")) return;

        removeFromAllTeams(player);

        String path = "teams.team" + team;
        List<String> list = plugin.getConfig().getStringList(path);

        list.add(player.getUniqueId().toString());
        plugin.getConfig().set(path, list);
        plugin.saveConfig();
    }

    private void removeFromAllTeams(Player player) {
        for (int i = 1; i <= 8; i++) {
            String path = "teams.team" + i;
            List<String> list = plugin.getConfig().getStringList(path);
            list.remove(player.getUniqueId().toString());
            plugin.getConfig().set(path, list);
        }
    }

    private void giveTeamSelector(Player player) {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK_MINECART);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eTeam auswählen");
        item.setItemMeta(meta);

        player.getInventory().addItem(item);
    }

    // =========================
    // GETTER
    // =========================

    public boolean isRunning() {
        return running;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void addPlayerToBossBar(Player player) {
        if (bossBar != null) {
            bossBar.addPlayer(player);
        }
    }

    // =========================
    // UTIL
    // =========================

    private String getNiceName(Material mat) {
        String name = mat.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}