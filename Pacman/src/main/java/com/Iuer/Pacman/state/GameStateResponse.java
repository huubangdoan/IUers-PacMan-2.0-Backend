package com.Iuer.Pacman.state;

public record GameStateResponse(
        int canonicalMask,
        boolean hasThorns,
        boolean hasPowerup,
        boolean hasWatermelon,
        boolean hasChilli,
        boolean dragonMode,
        boolean disguised,
        boolean wallHack
) {
    public static GameStateResponse fromMask(int mask) {
        return new GameStateResponse(
                mask,
                (mask & GameStateFlags.HAS_THORNS) != 0,
                (mask & GameStateFlags.HAS_POWERUP) != 0,
                (mask & GameStateFlags.HAS_WATERMELON) != 0,
                (mask & GameStateFlags.HAS_CHILLI) != 0,
                (mask & GameStateFlags.DRAGON_MODE) != 0,
                (mask & GameStateFlags.DISGUISED) != 0,
                (mask & GameStateFlags.WALL_HACK) != 0
        );
    }
}
