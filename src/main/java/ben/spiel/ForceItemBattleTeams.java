package ben.spiel;

import ben.spiel.command.FIBCommand;
import ben.spiel.game.GameManager;
import ben.spiel.listener.GameListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ForceItemBattleTeams extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {

        // Config laden
        saveDefaultConfig();

        // GameManager erstellen
        this.gameManager = new GameManager(this);

        // Listener registrieren
        getServer().getPluginManager().registerEvents(
                new GameListener(gameManager, this),
                this
        );

        // Command registrieren (FIXED!)
        FIBCommand cmd = new FIBCommand(gameManager);

        registerCommand("forceitembattle", cmd);
        registerCommand("backpack", cmd);

        getLogger().info("ForceItemBattle gestartet!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.shutdown();
        }

        getLogger().info("ForceItemBattle gestoppt!");
    }

    private void registerCommand(String name, FIBCommand command) {
        PluginCommand pluginCommand = getCommand(name);

        if (pluginCommand == null) {
            getLogger().warning("Command '" + name + "' fehlt in plugin.yml.");
            return;
        }

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }

    // Getter
    public GameManager getGameManager() {
        return gameManager;
    }
}
