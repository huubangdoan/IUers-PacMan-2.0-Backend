package com.Iuer.Pacman.game;

import tools.jackson.databind.JsonNode;

// Dữ liệu một lệnh MOVE do frontend gửi: {type:"MOVE", sequence, direction, row, col}
public record MoveRequest(int sequence, String direction, int row, int col) {

    
    public static MoveRequest from(JsonNode json) {
        return new MoveRequest(
                json.path("sequence").asInt(-1),
                json.path("direction").asText(),
                json.path("row").asInt(Integer.MIN_VALUE),
                json.path("col").asInt(Integer.MIN_VALUE)
        );
    }
}