package ben.spiel;

import org.bukkit.plugin.java.JavaPlugin;

import ben.spiel.command.CommandStart;
import ben.spiel.game.GameManager;
import ben.spiel.listener.GameListener;

public class ForceItemBattleTeams extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {
        gameManager = new GameManager();

        getCommand("fibstart").setExecutor(new CommandStart(gameManager));

        getServer().getPluginManager().registerEvents(new GameListener(gameManager), this);

        getLogger().info("Plugin gestartet!");
    }
}