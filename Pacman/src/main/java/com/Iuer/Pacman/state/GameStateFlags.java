package com.Iuer.Pacman.state;

import java.util.Locale;

public final class GameStateFlags {
    public static final int HAS_THORNS = 1 << 0;
    public static final int HAS_POWERUP = 1 << 1;
    public static final int HAS_WATERMELON = 1 << 2;
    public static final int HAS_CHILLI = 1 << 3;
    public static final int DRAGON_MODE = 1 << 4;
    public static final int DISGUISED = 1 << 5;
    public static final int WALL_HACK = 1 << 6;

    private GameStateFlags() {
    }

    public static GameStateChange fromEvent(String event, String value) {
        return switch (event) {
            case "fruitCollected" -> new GameStateChange(fruitFlag(value), true);
            case "effectEnded", "effectConsumed" -> new GameStateChange(effectFlag(value), false);
            case "wallHackActivated" -> new GameStateChange(WALL_HACK, true);
            case "wallHackEnded" -> new GameStateChange(WALL_HACK, false);
            default -> throw new IllegalArgumentException("Unsupported game event: " + event);
        };
    }

    private static int fruitFlag(String fruit) {
        if (fruit == null) {
            throw new IllegalArgumentException("A fruit type is required");
        }
        return switch (fruit.toLowerCase(Locale.ROOT)) {
            case "durian" -> HAS_THORNS;
            case "apple" -> HAS_POWERUP;
            case "watermelon" -> HAS_WATERMELON;
            case "chilli" -> HAS_CHILLI;
            case "dragonfruit" -> DRAGON_MODE;
            case "kiwi" -> DISGUISED;
            default -> throw new IllegalArgumentException("Unsupported fruit: " + fruit);
        };
    }

    private static int effectFlag(String effect) {
        if (effect == null) {
            throw new IllegalArgumentException("An effect is required");
        }
        return switch (effect.toLowerCase(Locale.ROOT)) {
            case "thorns" -> HAS_THORNS;
            case "powerup" -> HAS_POWERUP;
            case "watermelon" -> HAS_WATERMELON;
            case "chilli" -> HAS_CHILLI;
            case "dragonmode" -> DRAGON_MODE;
            case "disguised" -> DISGUISED;
            case "wallhack" -> WALL_HACK;
            default -> throw new IllegalArgumentException("Unsupported effect: " + effect);
        };
    }
}
