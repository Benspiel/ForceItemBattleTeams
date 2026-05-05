package ben.spiel.game;

import org.bukkit.Material;

public record ChallengeResult(Material material, String playerName, int seconds, boolean skipped) {
}
