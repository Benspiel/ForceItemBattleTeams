package ben.spiel.game;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ItemManager {

    private final JavaPlugin plugin;
    private final TeamManager teamManager;
    private final Random random = new Random();

    private final Map<Integer, Material> currentItems = new HashMap<>();

    private final String PREFIX = "§8[§eForceItem§8] §7";
    private static final Set<String> SURVIVAL_UNOBTAINABLE = Set.of(
            "AIR",
            "CAVE_AIR",
            "VOID_AIR",
            "BEDROCK",
            "BARRIER",
            "LIGHT",
            "COMMAND_BLOCK",
            "CHAIN_COMMAND_BLOCK",
            "REPEATING_COMMAND_BLOCK",
            "COMMAND_BLOCK_MINECART",
            "STRUCTURE_BLOCK",
            "STRUCTURE_VOID",
            "JIGSAW",
            "DEBUG_STICK",
            "KNOWLEDGE_BOOK",
            "SPAWNER",
            "TRIAL_SPAWNER",
            "VAULT",
            "END_PORTAL_FRAME",
            "END_PORTAL",
            "NETHER_PORTAL",
            "REINFORCED_DEEPSLATE",
            "BUDDING_AMETHYST",
            "FARMLAND",
            "DIRT_PATH",
            "FROGSPAWN",
            "FROSTED_ICE",
            "PETRIFIED_OAK_SLAB",
            "FIRE",
            "SOUL_FIRE",
            "WATER",
            "LAVA",
            "MOVING_PISTON",
            "PISTON_HEAD"
    );

    public ItemManager(JavaPlugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    // =========================
    // NEXT ITEM
    // =========================

    public void nextItem(int team) {
        List<Material> candidates = getCandidates();

        if (candidates.isEmpty()) {
            teamManager.sendTeamMessage(team, PREFIX + "§cKeine gültigen Items gefunden. Prüfe die Blacklist!");
            currentItems.remove(team);
            return;
        }

        Material currentItem = currentItems.get(team);
        List<Material> preferred = candidates.stream()
                .filter(material -> material != currentItem)
                .filter(material -> !isCurrentItemForOtherTeam(team, material))
                .toList();

        if (preferred.isEmpty() && candidates.size() > 1) {
            preferred = candidates.stream()
                    .filter(material -> material != currentItem)
                    .toList();
        }

        if (preferred.isEmpty()) {
            preferred = candidates;
        }

        Material nextItem = preferred.get(random.nextInt(preferred.size()));
        currentItems.put(team, nextItem);

        teamManager.sendTeamMessage(team, PREFIX + "Nächste Aufgabe: §6§l" + getNiceName(team));
    }

    public void reset() {
        currentItems.clear();
    }

    private List<Material> getCandidates() {
        Set<String> blacklist = new HashSet<>();
        for (String item : plugin.getConfig().getStringList("blacklist")) {
            blacklist.add(item.toUpperCase());
        }

        List<Material> candidates = new ArrayList<>();
        for (Material material : Material.values()) {
            if (isAllowedItem(material, blacklist)) {
                candidates.add(material);
            }
        }

        return candidates;
    }

    private boolean isAllowedItem(Material material, Set<String> blacklist) {
        String name = material.name();
        return material.isItem()
                && !material.isAir()
                && !material.isLegacy()
                && !blacklist.contains(name)
                && !SURVIVAL_UNOBTAINABLE.contains(name)
                && !name.endsWith("_SPAWN_EGG")
                && !name.startsWith("INFESTED_")
                && !name.startsWith("POTTED_");
    }

    private boolean isCurrentItemForOtherTeam(int team, Material material) {
        for (Map.Entry<Integer, Material> entry : currentItems.entrySet()) {
            if (entry.getKey() != team && entry.getValue() == material) {
                return true;
            }
        }

        return false;
    }

    public String getNiceName(int team) {

        Material currentItem = currentItems.get(team);
        if (currentItem == null) return "-";

        String name = currentItem.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public Material getCurrentItem(int team) {
        return currentItems.get(team);
    }
}
