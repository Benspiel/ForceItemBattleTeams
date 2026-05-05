package ben.spiel.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class BackpackManager {

    private final Map<Integer, Inventory> backpacks = new HashMap<>();

    public Inventory getBackpack(int team) {

        if (!backpacks.containsKey(team)) {
            backpacks.put(team,
                    Bukkit.createInventory(null, 27, "§8Team " + team + " Backpack"));
        }

        return backpacks.get(team);
    }

    public void openBackpack(Player player, int team) {
        player.openInventory(getBackpack(team));
    }

    public void clearAll() {
        backpacks.clear();
    }
}