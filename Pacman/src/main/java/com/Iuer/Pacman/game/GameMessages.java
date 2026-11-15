package com.Iuer.Pacman.game;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Collection;

/** Dựng và gửi các thông điệp server -> frontend (READY, STATE, FINISHED, ERROR). */
@Component
public class GameMessages {
    private final ObjectMapper objectMapper;

    public GameMessages(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode ready(PlayerState player, long bestScore) {
        ObjectNode ready = objectMapper.createObjectNode()
                .put("type", "READY")
                .put("mapId", player.map().id())
                .put("modeId", player.mode().id())
                .put("row", player.row())
                .put("col", player.col())
                .put("sequence", player.sequence())
                .put("score", player.score())
                .put("bestScore", bestScore);
        addCollected(ready, player.collected());
        return ready;
    }

    public ObjectNode state(MoveResult result) {
        ObjectNode state = objectMapper.createObjectNode()
                .put("type", "STATE")
                .put("sequence", result.sequence())
                .put("accepted", result.accepted())
                .put("row", result.row())
                .put("col", result.col())
                .put("score", result.score());
        addCollected(state, result.collected());
        return state;
    }

    public ObjectNode finished(int score, long bestScore) {
        return objectMapper.createObjectNode()
                .put("type", "FINISHED")
                .put("score", score)
                .put("bestScore", bestScore);
    }

    public ObjectNode error(String text) {
        return objectMapper.createObjectNode().put("type", "ERROR").put("message", text);
    }

    public void send(WebSocketSession socket, JsonNode payload) throws IOException {
        if (socket.isOpen()) socket.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    public void sendError(WebSocketSession socket, String text) throws IOException {
        send(socket, error(text));
    }

    private void addCollected(ObjectNode message, Collection<String> cells) {
        var array = message.putArray("collected");
        cells.forEach(array::add);
    }
}
