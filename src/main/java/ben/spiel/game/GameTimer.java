package ben.spiel.game;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTimer {

    private final GameManager gameManager;

    private int timeLeft;

    public GameTimer(GameManager gameManager) {
        this.gameManager = gameManager;

        // Startwert aus Config
        this.timeLeft = gameManager.getPlugin().getConfig().getInt("challenge-seconds");
    }

    public void start() {

        // Timer neu setzen beim Start
        this.timeLeft = gameManager.getPlugin().getConfig().getInt("challenge-seconds");

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!gameManager.isRunning() || gameManager.isStopped()) {
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    gameManager.stopGame();
                    cancel();
                    return;
                }

                // 📊 ActionBar
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(Component.text("§eZeit: §6" + format(timeLeft)));
                }

                // 📊 BossBar Fortschritt
                if (gameManager.getBossBar() != null) {
                    int total = gameManager.getPlugin().getConfig().getInt("challenge-seconds");
                    double progress = (double) timeLeft / total;

                    gameManager.getBossBar().setProgress(progress);
                }

                timeLeft--;
            }
        }.runTaskTimer(gameManager.getPlugin(), 0, 20);
    }

    public void stop() {
        // wird automatisch durch cancel() beendet
    }

    private String format(int sec) {
        int m = sec / 60;
        int s = sec % 60;
        return String.format("%02d:%02d", m, s);
    }

    public int getTimeLeft() {
        return timeLeft;
    }
}