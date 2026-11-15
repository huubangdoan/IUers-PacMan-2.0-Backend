package com.Iuer.Pacman.game.map;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Danh sách map và chế độ chơi do server giữ (nguồn chuẩn để đối chiếu).
 * Thêm map mới: tạo một lớp như ClassicMap rồi đăng ký ở MAPS bên dưới.
 */
public final class MapRegistry {
    private static final Map<String, GameMap> MAPS = register(ClassicMap.create(), CompactMap.create());
    private static final Map<String, GameMode> MODES = Map.of(
            "classic", new GameMode("classic", ClassicMap.ID, 10),
            "compact", new GameMode("compact", CompactMap.ID, 10)
    );

    static {
        MODES.values().forEach(mode -> {
            if (!MAPS.containsKey(mode.mapId())) {
                throw new IllegalStateException("Mode '" + mode.id() + "' refers to unknown map '" + mode.mapId() + "'");
            }
        });
    }

    private MapRegistry() {}

    public static GameMode findMode(String id) {
        GameMode mode = MODES.get(id);
        if (mode == null) throw new IllegalArgumentException("Unknown game mode");
        return mode;
    }

    public static GameMap findMap(String id) {
        GameMap map = MAPS.get(id);
        if (map == null) throw new IllegalArgumentException("Unknown game map");
        return map;
    }

    private static Map<String, GameMap> register(GameMap... maps) {
        Map<String, GameMap> byId = new LinkedHashMap<>();
        for (GameMap map : maps) {
            MapValidator.validate(map);
            if (byId.put(map.id(), map) != null) {
                throw new IllegalStateException("Trùng id map: " + map.id());
            }
        }
        return Collections.unmodifiableMap(byId);
    }
}