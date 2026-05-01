package ben.spiel.command;

import ben.spiel.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandStart implements CommandExecutor {

    private final GameManager gameManager;

    public CommandStart(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("fib.start")) {
            sender.sendMessage("§cKeine Rechte!");
            return true;
        }

        if (gameManager.isRunning()) {
            sender.sendMessage("§cSpiel läuft bereits!");
            return true;
        }

        gameManager.startGame();
        sender.sendMessage("§aSpiel gestartet!");
        return true;
    }
}