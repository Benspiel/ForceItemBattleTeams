package ben.spiel.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TeamManager {

    private final JavaPlugin plugin;

    public TeamManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // =========================
    // TEAM SETZEN
    // =========================

    public void addToTeam(Player player, int team) {

        // 🔒 Team Lock check
        if (plugin.getConfig().getBoolean("team-lock")) return;

        removeFromAllTeams(player);

        String path = "teams.team" + team;
        List<String> list = plugin.getConfig().getStringList(path);

        list.add(player.getUniqueId().toString());
        plugin.getConfig().set(path, list);
        plugin.saveConfig();
    }

    // =========================
    // REMOVE AUS ALLEN TEAMS
    // =========================

    private void removeFromAllTeams(Player player) {

        for (int i = 1; i <= 8; i++) {
            String path = "teams.team" + i;

            List<String> list = plugin.getConfig().getStringList(path);
            list.remove(player.getUniqueId().toString());

            plugin.getConfig().set(path, list);
        }
    }

    // =========================
    // TEAM RESET
    // =========================

    public void resetTeams() {
        for (int i = 1; i <= 8; i++) {
            plugin.getConfig().set("teams.team" + i, new ArrayList<>());
        }
        plugin.saveConfig();
    }

    // =========================
    // TEAM SELECTOR ITEM
    // =========================

    public void giveSelector(Player player) {

        ItemStack item = new ItemStack(Material.COMMAND_BLOCK_MINECART);
        var meta = item.getItemMeta();

        meta.setDisplayName("§eTeam auswählen");
        item.setItemMeta(meta);

        player.getInventory().addItem(item);
    }

    // =========================
    // TEAM ABFRAGEN
    // =========================

    public String getTeam(Player player) {

        for (int i = 1; i <= 8; i++) {
            List<String> list = plugin.getConfig().getStringList("teams.team" + i);

            if (list.contains(player.getUniqueId().toString())) {
                return "Team " + i;
            }
        }

        return "-";
    }
}