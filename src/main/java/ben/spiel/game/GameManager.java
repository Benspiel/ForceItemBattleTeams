package ben.spiel.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class GameManager {

    private final JavaPlugin plugin;

    private final GameTimer timer;
    private final ItemManager itemManager;
    private final SkipManager skipManager;
    private final TeamManager teamManager;

    private boolean running = false;
    private boolean stopped = false;

    private BossBar bossBar;

    // ArmorStand Map
    private final HashMap<UUID, ArmorStand> headDisplays = new HashMap<>();

    // Task für smooth follow
    private BukkitTask armorTask;

    public GameManager(JavaPlugin plugin) {
        this.plugin = plugin;

        this.teamManager = new TeamManager(plugin);
        this.skipManager = new SkipManager();
        this.itemManager = new ItemManager(plugin);
        this.timer = new GameTimer(this);
    }

    // =========================
    // GAME CONTROL
    // =========================

    public void startGame() {
        running = true;
        stopped = false;

        bossBar = Bukkit.createBossBar(
                "§eWarte auf Item...",
                BarColor.YELLOW,
                BarStyle.SOLID
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            bossBar.addPlayer(p);
            skipManager.giveSkipItem(p);
        }

        itemManager.nextItem();
        updateBossBar();
        updateArmorStands();

        startArmorStandUpdater(); // 🔥 wichtig!

        timer.start();
    }

    public void stopGame() {
        stopped = true;

        if (bossBar != null) {
            bossBar.setTitle("§cTime: Stopped");
            bossBar.setProgress(0);
        }

        if (armorTask != null) {
            armorTask.cancel();
            armorTask = null;
        }

        removeAllDisplays();

        Bukkit.broadcastMessage("§8[§6Force Item Battle§8] §cSpiel gestoppt!");
    }

    public void restartGame() {
        stopGame();

        running = false;
        stopped = false;

        removeBossBar();
        removeAllDisplays();

        teamManager.resetTeams();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            teamManager.giveSelector(p);
        }

        Bukkit.broadcastMessage("§8[§6Force Item Battle§8] §eSpiel wurde zurückgesetzt!");
    }

    // =========================
    // BOSSBAR
    // =========================

    public void updateBossBar() {
        if (bossBar == null) return;
        bossBar.setTitle("§6Item: §e" + itemManager.getNiceName());
    }

    private void removeBossBar() {
        if (bossBar == null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.removePlayer(p);
        }

        bossBar.setVisible(false);
        bossBar = null;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    // =========================
    // ARMOR STAND SYSTEM
    // =========================

    public void updateArmorStands() {

        if (itemManager.getCurrentItem() == null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {

            if (teamManager.getTeam(p).equals("-")) continue;

            ArmorStand stand = headDisplays.get(p.getUniqueId());

            Location loc = p.getLocation();
            var forward = loc.getDirection().normalize().multiply(0.25);
            Location spawnLoc = loc.clone().add(forward).add(0, 2.0, 0);

            if (stand == null || stand.isDead()) {

                stand = (ArmorStand) p.getWorld().spawnEntity(
                        spawnLoc,
                        EntityType.ARMOR_STAND
                );

                stand.setInvisible(true);
                stand.setGravity(false);
                stand.setSmall(true);
                stand.setMarker(false);

                headDisplays.put(p.getUniqueId(), stand);
            }

            if (stand.getEquipment() != null) {
                stand.getEquipment().setHelmet(
                        new ItemStack(itemManager.getCurrentItem())
                );
            }
        }
    }

    // 🔥 ULTRA SMOOTH FOLLOW
    public void startArmorStandUpdater() {

        armorTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (!running || stopped) return;

            for (Player p : Bukkit.getOnlinePlayers()) {

                ArmorStand stand = headDisplays.get(p.getUniqueId());

                if (stand == null || stand.isDead()) continue;

                Location loc = p.getLocation();
                var forward = loc.getDirection().normalize().multiply(0.25);

                stand.teleport(loc.add(forward).add(0, 2.0, 0));
            }

        }, 0L, 1L); // 🔥 jede Tick (20 TPS)
    }

    public void removeAllDisplays() {
        for (ArmorStand stand : headDisplays.values()) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        headDisplays.clear();
    }

    public ArmorStand getHeadDisplay(Player player) {
        return headDisplays.get(player.getUniqueId());
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

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public SkipManager getSkipManager() {
        return skipManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }
}