package ben.spiel;

import ben.spiel.command.BackpackCommand;
import ben.spiel.command.FIBCommand;
import ben.spiel.game.GameManager;
import ben.spiel.listener.GameListener;
import org.bukkit.plugin.java.JavaPlugin;

public class ForceItemBattleTeams extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {

        // 📁 Config erstellen (falls nicht vorhanden)
        saveDefaultConfig();

        // 🧠 GameManager initialisieren
        gameManager = new GameManager(this);

        // ⌨️ FIB Command + TAB
        FIBCommand fibCommand = new FIBCommand(gameManager, this);
        getCommand("forceitembattle").setExecutor(fibCommand);
        getCommand("forceitembattle").setTabCompleter(fibCommand);

        // 🎒 Backpack (immer registrieren!)
        getCommand("backpack").setExecutor(new BackpackCommand(this));

        // 🎧 Events registrieren
        getServer().getPluginManager().registerEvents(new GameListener(gameManager, this), this);

        getLogger().info("ForceItemBattleTeams gestartet!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ForceItemBattleTeams gestoppt!");
    }
}