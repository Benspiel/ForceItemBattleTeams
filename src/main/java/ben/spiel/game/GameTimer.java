package ben.spiel.game;

import org.bukkit.scheduler.BukkitRunnable;

public class GameTimer {

    private final GameManager gameManager;

    private int remainingTime;
    private int elapsedTime;
    private BukkitRunnable task;

    public GameTimer(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // =========================
    // START
    // =========================

    public void start() {

        // Zeit aus config laden
        reset();

        // Falls schon ein Timer läuft → stoppen
        if (task != null) {
            task.cancel();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {

                // Game gestoppt → abbrechen
                if (!gameManager.isRunning() || gameManager.isStopped()) {
                    cancel();
                    return;
                }

                // runterzählen
                remainingTime--;
                elapsedTime++;

                // Zeit abgelaufen
                if (remainingTime <= 0) {
                    remainingTime = 0;
                    task = null;
                    cancel();
                    gameManager.finishByTime();
                }
            }
        };

        // jede Sekunde
        task.runTaskTimer(gameManager.getPlugin(), 20L, 20L);
    }

    // =========================
    // STOP (optional sauber)
    // =========================

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void reset() {
        remainingTime = Math.max(60, gameManager.getPlugin()
                .getConfig().getInt("challenge-seconds", 300));
        elapsedTime = 0;
    }

    // =========================
    // GETTER
    // =========================

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }
}
