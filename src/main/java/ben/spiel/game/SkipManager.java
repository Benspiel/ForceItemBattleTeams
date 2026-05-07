package ben.spiel.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class SkipManager {

    private final JavaPlugin plugin;
    private final TeamManager teamManager;
    private final HashMap<Integer, Integer> skips = new HashMap<>();

    private static final String PREFIX = "§8[§eForceItem§8] §7";

    public SkipManager(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    // =========================
    // GIVE SKIPS
    // =========================

    public void resetSkips() {
        skips.clear();

        for (int team = 1; team <= 8; team++) {
            skips.put(team, getMaxSkips());
        }
    }

    public void giveSkipItem(Player p) {
        giveItem(p);
    }

    // =========================
    // USE SKIP (FIXED)
    // =========================

    public boolean useSkip(Player p) {

        int team = teamManager.getTeam(p);
        if (team == -1) return false;

        int left = getTeamSkips(team);

        if (left <= 0) return false;

        left--;
        skips.put(team, left);

        updateTeamItems(team);

        p.sendMessage(PREFIX + "§eTeam-Skip benutzt! §7(" + left + " übrig)");

        return true;
    }

    // =========================
    // ITEM UPDATE
    // =========================

    private void giveItem(Player p) {

        p.getInventory().remove(Material.BARRIER);

        int team = teamManager.getTeam(p);
        if (team == -1) {
            return;
        }

        int left = getTeamSkips(team);

        if (left <= 0) {
            return;
        }

        ItemStack item = new ItemStack(Material.BARRIER);
        var meta = item.getItemMeta();

        meta.setDisplayName("§cItem Skip (§e" + left + "§c)");

        item.setItemMeta(meta);

        p.getInventory().addItem(item);
    }

    private void updateTeamItems(int team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (teamManager.getTeam(player) == team) {
                giveItem(player);
            }
        }
    }

    // =========================
    // GETTER
    // =========================

    public int getSkips(Player p) {
        int team = teamManager.getTeam(p);
        if (team == -1) return 0;

        return getTeamSkips(team);
    }

    private int getTeamSkips(int team) {
        return skips.computeIfAbsent(team, ignored -> getMaxSkips());
    }

    private int getMaxSkips() {
        return Math.max(0, plugin.getConfig().getInt("max-skips", 3));
    }
}
