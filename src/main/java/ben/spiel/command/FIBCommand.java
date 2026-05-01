package ben.spiel.command;

import ben.spiel.game.GameManager;
import ben.spiel.gui.SettingsMenu;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FIBCommand implements CommandExecutor, TabCompleter {

    private final GameManager gameManager;
    private final JavaPlugin plugin;

    private final String PREFIX = "§8[§6Force Item Battle§8] §r";

    public FIBCommand(GameManager gameManager, JavaPlugin plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

    // =========================
    // COMMAND
    // =========================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(PREFIX + "/forceitembattle <start|stop|restart|settings|lock>");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "start" -> {
                gameManager.startGame();
                sender.sendMessage(PREFIX + "§aSpiel gestartet!");
            }

            case "stop" -> {
                gameManager.stopGame();
            }

            case "restart" -> {
                gameManager.restartGame();
            }

            case "settings" -> {
                if (sender instanceof Player player) {
                    new SettingsMenu(plugin, gameManager).open(player);
                }
            }

            case "lock" -> {
                plugin.getConfig().set("team-lock", true);
                plugin.saveConfig();
                sender.sendMessage(PREFIX + "§cTeams wurden gelockt!");
            }

            default -> sender.sendMessage(PREFIX + "§cUnbekannter Command!");
        }

        return true;
    }

    // =========================
    // TAB COMPLETION
    // =========================

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            return List.of("start", "stop", "restart", "settings", "lock");
        }

        return List.of();
    }
}