package ben.spiel.command;

import ben.spiel.game.GameManager;
import ben.spiel.gui.SettingsMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FIBCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = "§8[§eForceItem§8] §7";
    private static final List<String> SUBCOMMANDS = List.of(
            "start",
            "stop",
            "restart",
            "settings",
            "reveal",
            "lock",
            "backpack"
    );

    private final GameManager gameManager;

    public FIBCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("backpack")) {
            return openBackpack(sender);
        }

        if (args.length == 0) {
            sender.sendMessage(PREFIX + "Nutze: /" + label + " <start|stop|restart|settings|reveal|lock|backpack>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (requiresAdminPermission(subCommand) && !sender.hasPermission("fib.start")) {
            sender.sendMessage(PREFIX + "§cKeine Rechte!");
            return true;
        }

        switch (subCommand) {
            case "start":
                gameManager.startGame();
                sender.sendMessage(PREFIX + "§aSpiel gestartet!");
                break;

            case "stop":
                gameManager.stopGame();
                sender.sendMessage(PREFIX + "§cSpiel gestoppt!");
                break;

            case "restart":
                gameManager.restartGame();
                sender.sendMessage(PREFIX + "§eSpiel neu gestartet!");
                break;

            case "settings":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(PREFIX + "§cDieses Menü kann nur ein Spieler öffnen.");
                    return true;
                }

                new SettingsMenu(gameManager.getPlugin(), gameManager).open(player);
                break;

            case "reveal":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(PREFIX + "§cReveal kann nur ein Spieler öffnen.");
                    return true;
                }

                gameManager.getRevealManager().revealNext(player);
                break;

            case "lock":
                toggleTeamLock(sender);
                break;

            case "backpack":
                return openBackpack(sender);

            default:
                sender.sendMessage(PREFIX + "§cUnbekannter Command!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        if (cmd.getName().equalsIgnoreCase("backpack")) {
            return List.of();
        }

        if (args.length != 1) {
            return List.of();
        }

        String typed = args[0].toLowerCase();
        List<String> matches = new ArrayList<>();

        for (String subCommand : SUBCOMMANDS) {
            if (subCommand.startsWith(typed)) {
                matches.add(subCommand);
            }
        }

        return matches;
    }

    private boolean openBackpack(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + "§cBackpack kann nur ein Spieler öffnen.");
            return true;
        }

        if (!gameManager.getPlugin().getConfig().getBoolean("enable-backpack", true)) {
            player.sendMessage(PREFIX + "§cBackpack ist deaktiviert!");
            return true;
        }

        int team = gameManager.getTeamManager().getTeam(player);

        if (team == -1) {
            player.sendMessage(PREFIX + "§cDu bist in keinem Team!");
            return true;
        }

        gameManager.getBackpackManager().openBackpack(player, team);
        return true;
    }

    private boolean requiresAdminPermission(String subCommand) {
        return !subCommand.equals("backpack");
    }

    private void toggleTeamLock(CommandSender sender) {
        boolean locked = gameManager.getPlugin().getConfig().getBoolean("team-lock", false);

        gameManager.getPlugin().getConfig().set("team-lock", !locked);
        gameManager.getPlugin().saveConfig();

        sender.sendMessage(PREFIX + "Team-Lock ist jetzt " + (!locked ? "§caktiv" : "§aoffen") + "§7.");
    }
}
