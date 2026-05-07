package ben.spiel.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final JavaPlugin plugin;

    private final GameTimer timer;
    private final ItemManager itemManager;
    private final SkipManager skipManager;
    private final TeamManager teamManager;
    private final RevealManager revealManager;
    private final BackpackManager backpackManager;

    private boolean running = false;
    private boolean stopped = false;
    private final Map<Integer, Integer> currentItemStartedAtSeconds = new HashMap<>();

    private final HashMap<UUID, ArmorStand> headDisplays = new HashMap<>();

    private BukkitTask armorTask;
    private BukkitTask actionTask;
    private BukkitTask inventoryTask;
    private BukkitTask statusTask;

    public GameManager(JavaPlugin plugin) {
        this.plugin = plugin;

        this.teamManager = new TeamManager(plugin);
        this.skipManager = new SkipManager(plugin, teamManager);
        this.itemManager = new ItemManager(plugin, teamManager);
        this.timer = new GameTimer(this);
        this.revealManager = new RevealManager(plugin, this);
        this.backpackManager = new BackpackManager();
    }

    public void startGame() {
        stopTasksOnly();

        running = true;
        stopped = false;

        revealManager.reset();
        itemManager.reset();
        currentItemStartedAtSeconds.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (teamManager.getTeam(player) == -1) {
                teamManager.addToTeam(player, 1);
            }

            teamManager.updatePlayerName(player);
            player.getInventory().clear();
        }

        skipManager.resetSkips();
        for (Player player : Bukkit.getOnlinePlayers()) {
            skipManager.giveSkipItem(player);
        }

        timer.start();

        int startedAtSeconds = timer.getElapsedTime();
        for (int team = 1; team <= 8; team++) {
            currentItemStartedAtSeconds.put(team, startedAtSeconds);
            itemManager.nextItem(team);
        }

        updateArmorStands();

        startArmorUpdater();
        startActionBar();
        startInventoryChecker();

        Bukkit.broadcastMessage("§8[§eForceItem§8] §aSpiel gestartet!");
    }

    public void completeChallenge(Player player) {
        int team = teamManager.getTeam(player);
        if (team == -1) {
            player.sendMessage("§8[§eForceItem§8] §cDu bist in keinem Team!");
            return;
        }

        Material completedItem = itemManager.getCurrentItem(team);
        if (completedItem == null) {
            return;
        }

        int seconds = getCurrentItemSeconds(team);

        boolean counted = teamManager.addResult(player, completedItem, seconds, false);
        if (!counted) {
            player.sendMessage("§8[§eForceItem§8] §cDu bist in keinem Team!");
            return;
        }

        teamManager.sendTeamMessage(team, "§8[§eForceItem§8] " + teamManager.formatPlayerName(player)
                + " §ahat §6" + formatMaterialName(completedItem) + " §ageschafft! §7("
                + teamManager.formatDuration(seconds) + ")");
        itemManager.nextItem(team);
        currentItemStartedAtSeconds.put(team, timer.getElapsedTime());
        updateArmorStands();
    }

    public void skipChallenge(Player player) {
        int team = teamManager.getTeam(player);
        if (team == -1) {
            player.sendMessage("§8[§eForceItem§8] §cDu bist in keinem Team!");
            return;
        }

        Material skippedItem = itemManager.getCurrentItem(team);
        if (skippedItem == null) {
            return;
        }

        int seconds = getCurrentItemSeconds(team);

        boolean counted = teamManager.addResult(player, skippedItem, seconds, true);
        if (!counted) {
            player.sendMessage("§8[§eForceItem§8] §cDu bist in keinem Team!");
            return;
        }

        teamManager.sendTeamMessage(team, "§8[§eForceItem§8] " + teamManager.formatPlayerName(player)
                + " §ehat §6" + formatMaterialName(skippedItem) + " §egeskippt! §7("
                + teamManager.formatDuration(seconds) + ")");
        itemManager.nextItem(team);
        currentItemStartedAtSeconds.put(team, timer.getElapsedTime());
        updateArmorStands();
    }

    public void checkPlayerInventory(Player player) {
        if (!running || stopped) {
            return;
        }

        int team = teamManager.getTeam(player);
        if (team == -1) {
            return;
        }

        Material currentItem = itemManager.getCurrentItem(team);
        if (currentItem == null) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            if (item.getType() == currentItem) {
                completeChallenge(player);
                return;
            }
        }
    }

    private int getCurrentItemSeconds(int team) {
        return Math.max(0, timer.getElapsedTime() - currentItemStartedAtSeconds.getOrDefault(team, timer.getElapsedTime()));
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public void stopGame() {
        stopGame(true);
    }

    private void stopGame(boolean announce) {
        if (!running && stopped) {
            return;
        }

        stopped = true;
        running = false;

        stopTasksOnly();
        timer.stop();
        removeAllDisplays();
        itemManager.reset();
        currentItemStartedAtSeconds.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
        }

        if (announce) {
            Bukkit.broadcastMessage("§8[§eForceItem§8] §cSpiel gestoppt!");
            showStatusActionBar("§cSpiel gestoppt!");
        }
    }

    public void finishByTime() {
        if (!running || stopped) {
            return;
        }

        stopped = true;
        running = false;

        stopTasksOnly();
        timer.stop();
        removeAllDisplays();
        itemManager.reset();
        currentItemStartedAtSeconds.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();
        }

        Bukkit.broadcastMessage("§8[§eForceItem§8] §cZeit abgelaufen!");
        showStatusActionBar("§cZeit abgelaufen!");
    }

    public void restartGame() {
        stopGame(false);

        running = false;
        stopped = false;

        removeAllDisplays();
        backpackManager.clearAll();
        teamManager.resetTeams();
        revealManager.reset();
        itemManager.reset();
        currentItemStartedAtSeconds.clear();

        plugin.getConfig().set("team-lock", false);
        plugin.saveConfig();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().clear();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                teamManager.giveSelector(player);
                teamManager.updatePlayerName(player);
            }, 2L);
        }

        Bukkit.broadcastMessage("§8[§eForceItem§8] §eSpiel wurde zurückgesetzt!");
    }

    public void shutdown() {
        running = false;
        stopped = true;

        stopTasksOnly();
        timer.stop();
        removeAllDisplays();
        itemManager.reset();
        currentItemStartedAtSeconds.clear();
    }

    private void stopTasksOnly() {
        if (armorTask != null) {
            armorTask.cancel();
            armorTask = null;
        }

        if (actionTask != null) {
            actionTask.cancel();
            actionTask = null;
        }

        if (inventoryTask != null) {
            inventoryTask.cancel();
            inventoryTask = null;
        }

        if (statusTask != null) {
            statusTask.cancel();
            statusTask = null;
        }
    }

    public void startActionBar() {
        actionTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (!running || stopped) return;

            int time = timer.getRemainingTime();

            int minutes = time / 60;
            int seconds = time % 60;

            String timeString = String.format("%02d:%02d", minutes, seconds);

            for (Player player : Bukkit.getOnlinePlayers()) {
                int team = teamManager.getTeam(player);
                String itemName = team == -1 ? "-" : itemManager.getNiceName(team);
                Component actionBar = LEGACY.deserialize("§d" + timeString + " §7- §6" + itemName);

                player.sendActionBar(actionBar);
            }

        }, 0L, 20L);
    }

    private void startInventoryChecker() {
        inventoryTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!running || stopped) return;

            for (Player player : Bukkit.getOnlinePlayers()) {
                checkPlayerInventory(player);
            }
        }, 5L, 10L);
    }

    private void showStatusActionBar(String message) {
        if (statusTask != null) {
            statusTask.cancel();
            statusTask = null;
        }

        Component actionBar = LEGACY.deserialize(message);

        statusTask = new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(actionBar);
                }

                ticks++;
                if (ticks >= 6) {
                    statusTask = null;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void updateArmorStands() {
        removeDisplaysForOfflinePlayers();

        for (Player player : Bukkit.getOnlinePlayers()) {
            int team = teamManager.getTeam(player);
            Material currentItem = team == -1 ? null : itemManager.getCurrentItem(team);

            if (currentItem == null) {
                removeDisplay(player);
                continue;
            }

            ArmorStand stand = headDisplays.get(player.getUniqueId());

            Location spawnLoc = getDisplayLocation(player);

            if (stand == null || stand.isDead()) {
                stand = (ArmorStand) player.getWorld().spawnEntity(
                        spawnLoc,
                        EntityType.ARMOR_STAND
                );

                stand.setInvisible(true);
                stand.setGravity(false);
                stand.setSmall(true);
                stand.setMarker(true);
                stand.setBasePlate(false);
                stand.setInvulnerable(true);
                stand.setCollidable(false);
                stand.setPersistent(false);

                headDisplays.put(player.getUniqueId(), stand);
            }

            if (stand.getEquipment() != null) {
                stand.getEquipment().setHelmet(
                        new ItemStack(currentItem)
                );
            }
        }
    }

    public void startArmorUpdater() {
        armorTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (!running || stopped) return;

            for (Player player : Bukkit.getOnlinePlayers()) {

                ArmorStand stand = headDisplays.get(player.getUniqueId());

                if (stand == null || stand.isDead()) continue;

                stand.teleport(getDisplayLocation(player));
            }

        }, 0L, 1L);
    }

    private Location getDisplayLocation(Player player) {
        Location loc = player.getLocation();
        Vector forward = loc.getDirection().setY(0);

        if (forward.lengthSquared() > 0) {
            forward.normalize().multiply(0.25);
        }

        return loc.clone().add(forward).add(0, 2.15, 0);
    }

    public void removeAllDisplays() {
        for (ArmorStand stand : headDisplays.values()) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }

        headDisplays.clear();
    }

    public void removeDisplay(Player player) {
        ArmorStand stand = headDisplays.remove(player.getUniqueId());

        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
    }

    private void removeDisplaysForOfflinePlayers() {
        headDisplays.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            ArmorStand stand = entry.getValue();

            if (player != null && player.isOnline()) {
                return false;
            }

            if (stand != null && !stand.isDead()) {
                stand.remove();
            }

            return true;
        });
    }

    public ArmorStand getHeadDisplay(Player player) {
        return headDisplays.get(player.getUniqueId());
    }

    public Material getCurrentItem(Player player) {
        int team = teamManager.getTeam(player);
        if (team == -1) {
            return null;
        }

        return itemManager.getCurrentItem(team);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isStopped() {
        return stopped;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public GameTimer getTimer() {
        return timer;
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

    public RevealManager getRevealManager() {
        return revealManager;
    }

    public BackpackManager getBackpackManager() {
        return backpackManager;
    }
}
