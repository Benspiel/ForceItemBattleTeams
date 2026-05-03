package ben.spiel.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class ItemManager {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    private Material currentItem;

    private final String PREFIX = "§8[§bForceItem§8] §7";

    public ItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // =========================
    // NEXT ITEM
    // =========================

    public void nextItem() {

        Material[] materials = Material.values();
        List<String> blacklist = plugin.getConfig().getStringList("blacklist");

        do {
            currentItem = materials[random.nextInt(materials.length)];
        } while (!currentItem.isItem() || blacklist.contains(currentItem.name()));

        // 📢 Nachricht (clean)
        Bukkit.broadcastMessage(PREFIX + "Nächste Aufgabe: §6§l" + getNiceName());
    }

    // =========================
    // SKIP MESSAGE
    // =========================

    public void sendSkipMessage() {
        Bukkit.broadcastMessage(PREFIX + "§eAufgabe übersprungen!");
    }

    // =========================
    // SUCCESS MESSAGE
    // =========================

    public void sendSuccessMessage(String playerName) {
        Bukkit.broadcastMessage(PREFIX + "§a" + playerName + " hat die Aufgabe geschafft!");
    }

    // =========================
    // DEUTSCHER NAME (CLIENT SIDE)
    // =========================

    public String getNiceName() {

        if (currentItem == null) return "-";

        // fallback (falls translation nicht genutzt wird)
        String name = currentItem.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public Material getCurrentItem() {
        return currentItem;
    }
}