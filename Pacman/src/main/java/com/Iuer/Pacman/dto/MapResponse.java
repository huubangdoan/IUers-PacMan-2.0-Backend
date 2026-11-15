package com.Iuer.Pacman.dto;

import com.Iuer.Pacman.game.map.Cell;
import com.Iuer.Pacman.game.map.GameMap;
import com.Iuer.Pacman.game.map.GhostSpawn;

import java.util.List;
import java.util.Map;


// Dữ liệu map trả về cho frontend. Cấu trúc và thứ tự khóa khớp với MapDefinition ở frontend:
// { id, name, grid, spawns: { player: {row,col}, ghosts: [{row,col,type}] }, features }

public record MapResponse(
        String id,
        String name,
        int[][] grid,
        Spawns spawns,
        Map<String, Map<String, Object>> features
) {
    public record Spawns(Cell player, List<GhostSpawn> ghosts) {}

    public static MapResponse from(GameMap map) {
        return new MapResponse(
                map.id(),
                map.name(),
                map.grid(),
                new Spawns(map.playerCell(), map.ghosts()),
                map.features()
        );
    }
}
