package ben.spiel.game;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Random;

public class GameManager {

    private boolean running = false;
    private Material currentItem;
    private final Random random = new Random();

    public void startGame() {
        running = true;
        nextItem();
    }

    public void nextItem() {
        Material[] materials = Material.values();

        do {
            currentItem = materials[random.nextInt(materials.length)];
        } while (!currentItem.isItem());

        Bukkit.broadcastMessage("§6Neues Item: §e" + currentItem.name());
    }

    public void checkPlayer(Player player) {
        if (!running) return;

        if (player.getInventory().contains(currentItem)) {
            Bukkit.broadcastMessage("§a" + player.getName() + " hat das Item!");
            nextItem();
        }
    }

    public boolean isRunning() {
        return running;
    }
}