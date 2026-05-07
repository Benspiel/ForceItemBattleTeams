package ben.spiel.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class TeamManager {

    private final JavaPlugin plugin;

    // Spieler → Team
    private final Map<UUID, Integer> playerTeams = new HashMap<>();

    // Team → Ergebnisse (für Fortschritt + Reveal)
    private final Map<Integer, List<ChallengeResult>> teamResults = new HashMap<>();

    public TeamManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Teams initialisieren
        for (int i = 1; i <= 8; i++) {
            teamResults.put(i, new ArrayList<>());
        }

        loadTeams();
    }

    // =========================
    // TEAM JOIN
    // =========================

    public boolean addToTeam(Player p, int team) {
        if (team < 1 || team > 8) {
            return false;
        }

        playerTeams.put(p.getUniqueId(), team);
        savePlayerTeam(p.getUniqueId(), team);
        updatePlayerName(p);
        p.sendMessage("§8[§eForceItem§8] §7Du bist jetzt in " + getTeamTag(team) + " §7Team " + team + "§7.");
        return true;
    }

    public int getTeam(Player p) {
        return playerTeams.getOrDefault(p.getUniqueId(), -1);
    }

    // =========================
    // ITEM TRACKING (WICHTIG!)
    // =========================

    public boolean addResult(Player p, Material mat, int seconds, boolean skipped) {

        int team = getTeam(p);
        if (team == -1) return false;

        teamResults.get(team).add(new ChallengeResult(mat, p.getName(), seconds, skipped));
        return true;
    }

    public List<ChallengeResult> getResults(int team) {
        return teamResults.getOrDefault(team, new ArrayList<>());
    }

    public int getScore(int team) {
        int score = 0;

        for (ChallengeResult result : getResults(team)) {
            if (!result.skipped()) {
                score++;
            }
        }

        return score;
    }

    // =========================
    // RANKING (FIX!)
    // =========================

    public List<Integer> getRanking() {

        List<Integer> teams = new ArrayList<>(teamResults.keySet());

        teams.sort((a, b) -> {
            int sizeA = getScore(a);
            int sizeB = getScore(b);
            return Integer.compare(sizeB, sizeA);
        });

        return teams;
    }

    // =========================
    // RESET
    // =========================

    public void resetTeams() {
        playerTeams.clear();

        for (List<ChallengeResult> list : teamResults.values()) {
            list.clear();
        }

        for (int i = 1; i <= 8; i++) {
            plugin.getConfig().set("teams.team" + i, new ArrayList<String>());
        }

        plugin.saveConfig();
        updateAllPlayerNames();
    }

    public void updateAllPlayerNames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerName(player);
        }
    }

    public void updatePlayerName(Player player) {
        int team = getTeam(player);
        String rawName = player.getName();

        removeFromAllScoreboardTeams(player);

        if (team == -1) {
            player.setDisplayName(rawName);
            player.setPlayerListName(rawName);
            return;
        }

        Team scoreboardTeam = getOrCreateScoreboardTeam(team);
        scoreboardTeam.addEntry(rawName);

        String formattedName = getTeamTag(team) + " " + rawName;
        player.setDisplayName(formattedName);
        player.setPlayerListName(formattedName);
    }

    private Team getOrCreateScoreboardTeam(int team) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "fib_team_" + team;
        Team scoreboardTeam = scoreboard.getTeam(teamName);

        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(teamName);
        }

        scoreboardTeam.setPrefix(getTeamTag(team) + " ");
        scoreboardTeam.setSuffix("");
        return scoreboardTeam;
    }

    private void removeFromAllScoreboardTeams(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String entry = player.getName();

        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(entry)) {
                team.removeEntry(entry);
            }
        }
    }

    // =========================
    // TEAM SELECTOR ITEM
    // =========================

    public void giveSelector(Player p) {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK_MINECART);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§eTeam auswählen");
        meta.setLore(List.of("§7Rechtsklick zum Team-Menü"));
        item.setItemMeta(meta);

        p.getInventory().setItem(0, item);
    }

    public Material getTeamWool(int team) {
        return switch (team) {
            case 1 -> Material.WHITE_WOOL;
            case 2 -> Material.ORANGE_WOOL;
            case 3 -> Material.MAGENTA_WOOL;
            case 4 -> Material.LIGHT_BLUE_WOOL;
            case 5 -> Material.YELLOW_WOOL;
            case 6 -> Material.LIME_WOOL;
            case 7 -> Material.PINK_WOOL;
            case 8 -> Material.RED_WOOL;
            default -> Material.GRAY_WOOL;
        };
    }

    public String getTeamTag(int team) {
        return getTeamColor(team) + "[" + getTeamColorName(team) + "]§r";
    }

    public String formatPlayerName(Player player) {
        int team = getTeam(player);
        if (team == -1) {
            return "§7[Kein Team]§r " + player.getName();
        }

        return getTeamTag(team) + " " + player.getName();
    }

    public void sendTeamMessage(int team, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getTeam(player) == team) {
                player.sendMessage(message);
            }
        }
    }

    public String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int rest = seconds % 60;
        return String.format("%02d:%02d", minutes, rest);
    }

    private String getTeamColor(int team) {
        return switch (team) {
            case 1 -> "§f";
            case 2 -> "§6";
            case 3 -> "§d";
            case 4 -> "§b";
            case 5 -> "§e";
            case 6 -> "§a";
            case 7 -> "§d";
            case 8 -> "§c";
            default -> "§7";
        };
    }

    private String getTeamColorName(int team) {
        return switch (team) {
            case 1 -> "Weiss";
            case 2 -> "Orange";
            case 3 -> "Magenta";
            case 4 -> "Hellblau";
            case 5 -> "Gelb";
            case 6 -> "Gruen";
            case 7 -> "Pink";
            case 8 -> "Rot";
            default -> "Grau";
        };
    }

    private void loadTeams() {
        playerTeams.clear();

        for (int i = 1; i <= 8; i++) {
            for (String uuid : plugin.getConfig().getStringList("teams.team" + i)) {
                try {
                    playerTeams.put(UUID.fromString(uuid), i);
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().warning("Ungültige UUID in teams.team" + i + ": " + uuid);
                }
            }
        }
    }

    private void savePlayerTeam(UUID playerId, int team) {
        String uuid = playerId.toString();

        for (int i = 1; i <= 8; i++) {
            List<String> players = new ArrayList<>(plugin.getConfig().getStringList("teams.team" + i));
            players.remove(uuid);

            if (i == team) {
                players.add(uuid);
            }

            plugin.getConfig().set("teams.team" + i, players);
        }

        plugin.saveConfig();
    }
}
