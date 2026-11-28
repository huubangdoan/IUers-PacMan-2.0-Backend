package com.Iuer.Pacman.game;
import java.util.Map;
public class SpeedRules {
    // Frontend: PacmanPlayer(spawn, speed = 5) => 5 ô/giây
    public static final double BASE_PLAYER_CELLS_PER_SECOND = 5.0;
    // Nới 20% cho độ trễ mạng và sai số khung hình
    public static final double TOLERANCE = 1.2;
    // Tối đa số bước đi dồn lại được chấp nhận cùng lúc (khoảng 2 giây giật lag)
    public static final int BURST = 10;

    private SpeedRules() {}
    public static double playerCellsPerSecond(GameMapCatalog.GameMap map) {
        double multiplier = 1.0;
        Map<String, Object> feature = map.features() == null ? null : map.features().get("playerSpeed");
        if (feature != null && feature.get("multiplier") instanceof Number number) {
            multiplier = number.doubleValue();
        }
        return BASE_PLAYER_CELLS_PER_SECOND * multiplier;
    }
    public static double allowedMovesPerSecond(GameMapCatalog.GameMap map) {
        return playerCellsPerSecond(map) * TOLERANCE;
    }
}
