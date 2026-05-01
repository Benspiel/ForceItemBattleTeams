package ben.spiel.command;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class BackpackCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    private final String PREFIX = "§8[§6Force Item Battle§8] §r";

    private static final HashMap<UUID, Inventory> backpacks = new HashMap<>();

    public BackpackCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        // 🔥 HIER wird jetzt geprüft
        if (!plugin.getConfig().getBoolean("enable-backpack")) {
            player.sendMessage(PREFIX + "§cBackpack ist deaktiviert!");
            return true;
        }

        Inventory inv = backpacks.computeIfAbsent(
                player.getUniqueId(),
                uuid -> Bukkit.createInventory(null, 27, "§6Backpack")
        );

        player.openInventory(inv);
        return true;
    }
}