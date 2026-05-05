package ben.spiel.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ItemManager {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    private Material currentItem;

    private final String PREFIX = "§8[§eForceItem§8] §7";

    public ItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // =========================
    // NEXT ITEM
    // =========================

    public void nextItem() {

        Material[] materials = Material.values();
        Set<String> blacklist = new HashSet<>();
        for (String item : plugin.getConfig().getStringList("blacklist")) {
            blacklist.add(item.toUpperCase());
        }

        List<Material> candidates = new ArrayList<>();
        for (Material material : materials) {
            if (material.isItem() && !blacklist.contains(material.name())) {
                candidates.add(material);
            }
        }

        if (candidates.isEmpty()) {
            Bukkit.broadcastMessage(PREFIX + "§cKeine gültigen Items gefunden. Prüfe die Blacklist!");
            currentItem = null;
            return;
        }

        Material nextItem;
        do {
            nextItem = candidates.get(random.nextInt(candidates.size()));
        } while (candidates.size() > 1 && nextItem == currentItem);

        currentItem = nextItem;

        Bukkit.broadcastMessage(PREFIX + "Nächste Aufgabe: §6§l" + getNiceName());
    }

    // =========================
    // SKIP MESSAGE (NEU)
    // =========================

    public void sendSkipUsed() {
        Bukkit.broadcastMessage(PREFIX + "§eSkip benutzt!");
    }

    // =========================
    // SUCCESS MESSAGE
    // =========================

    public void sendSuccessMessage(String playerName) {
        Bukkit.broadcastMessage(PREFIX + "§a" + playerName + " hat die Aufgabe geschafft!");
    }

    // =========================
    // NAME FORMAT
    // =========================

    public String getNiceName() {

        if (currentItem == null) return "-";

        String name = currentItem.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public Material getCurrentItem() {
        return currentItem;
    }
}
