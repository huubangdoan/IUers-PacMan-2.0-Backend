package com.Iuer.Pacman.game;

import com.Iuer.Pacman.denylist.DenyListService;
import com.Iuer.Pacman.entity.GameScore;
import com.Iuer.Pacman.repository.GameScoreRepository;
import com.Iuer.Pacman.security.JwtUtil;
import com.Iuer.Pacman.service.GameStateService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final DenyListService denyList;
    private final GameScoreRepository gameScoreRepository;
    private final GameStateService gameStateService;
    private final ConcurrentHashMap<String, PlayerSession> sessions = new ConcurrentHashMap<>();

    public GameWebSocketHandler(
            ObjectMapper objectMapper,
            JwtUtil jwtUtil,
            DenyListService denyList,
            GameScoreRepository gameScoreRepository,
            GameStateService gameStateService
    ) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.denyList = denyList;
        this.gameScoreRepository = gameScoreRepository;
        this.gameStateService = gameStateService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage message) throws Exception {
        JsonNode request;
        try {
            request = objectMapper.readTree(message.getPayload());
        } catch (Exception exception) {
            sendError(socket, "Invalid JSON message");
            return;
        }

        PlayerSession player = sessions.get(socket.getId());
        String type = request.path("type").asText();
        if (player == null) {
            if (!"AUTH".equals(type)) {
                sendError(socket, "Authenticate before sending game actions");
                socket.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            authenticate(socket, request);
            return;
        }

        switch (type) {
            case "MOVE" -> move(socket, player, request);
            case "FINISH" -> finish(socket, player);
            default -> sendError(socket, "Unsupported game message");
        }
    }

    private void authenticate(WebSocketSession socket, JsonNode request) throws IOException {
        String token = request.path("token").asText();
        if (token.isBlank() || !jwtUtil.validateToken(token)) {
            sendError(socket, "Invalid or expired token");
            socket.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        Claims claims = jwtUtil.extractAllClaims(token);
        if (claims.getSubject() == null || claims.getId() == null
                || denyList.isDenied(claims.getId())
                || denyList.isIssuedBeforeRevoke(claims.getSubject(), claims.getIssuedAt())) {
            sendError(socket, "Token has been revoked");
            socket.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        String modeId = request.path("modeId").asText("classic");
        String requestedMapId = request.path("mapId").asText("classic");
        GameMapCatalog.GameMode mode;
        GameMapCatalog.GameMap map;
        try {
            mode = GameMapCatalog.findMode(modeId);
            map = GameMapCatalog.findMap(mode.mapId());
        } catch (IllegalArgumentException exception) {
            sendError(socket, exception.getMessage());
            socket.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        if (!requestedMapId.equals(map.id())) {
            sendError(socket, "Game mode does not match the selected map");
            socket.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        PlayerSession player = new PlayerSession(claims.getSubject(), mode, map);
        sessions.put(socket.getId(), player);
        long bestScore = gameScoreRepository.findBestScoreByUsername(player.username);
        ObjectNode ready = objectMapper.createObjectNode()
                .put("type", "READY")
                .put("mapId", player.map.id())
                .put("modeId", player.mode.id())
                .put("row", player.row)
                .put("col", player.col)
                .put("sequence", player.sequence)
                .put("score", player.score)
                .put("bestScore", bestScore);
        addCollected(ready, player);
        send(socket, ready);
    }

    private void move(WebSocketSession socket, PlayerSession player, JsonNode request) throws IOException {
        int sequence = request.path("sequence").asInt(-1);
        boolean accepted = sequence == player.sequence + 1;
        String direction = request.path("direction").asText();
        int rowDelta = switch (direction) {
            case "up" -> -1;
            case "down" -> 1;
            default -> 0;
        };
        int colDelta = switch (direction) {
            case "left" -> -1;
            case "right" -> 1;
            default -> 0;
        };
        int targetRow = request.path("row").asInt(Integer.MIN_VALUE);
        int targetCol = request.path("col").asInt(Integer.MIN_VALUE);
        accepted = accepted
                && Math.abs(rowDelta) + Math.abs(colDelta) == 1
                && targetRow == player.row + rowDelta
                && targetCol == player.col + colDelta
                && isOpen(player, targetRow, targetCol);

        if (sequence > player.sequence) player.sequence = sequence;
        if (accepted) {
            player.row = targetRow;
            player.col = targetCol;
            String cell = targetRow + ":" + targetCol;
            if (player.collected.add(cell)) player.score += player.mode.pointsPerDot();
        }

        ObjectNode state = objectMapper.createObjectNode()
                .put("type", "STATE")
                .put("sequence", sequence)
                .put("accepted", accepted)
                .put("row", player.row)
                .put("col", player.col)
                .put("score", player.score);
        addCollected(state, player);
        send(socket, state);
    }

    private void finish(WebSocketSession socket, PlayerSession player) throws IOException {
        player.finished = true;
        gameScoreRepository.save(new GameScore(player.username, player.score));
        long bestScore = gameScoreRepository.findBestScoreByUsername(player.username);
        send(socket, objectMapper.createObjectNode()
                .put("type", "FINISHED")
                .put("score", player.score)
                .put("bestScore", bestScore));
        sessions.remove(socket.getId());
        socket.close(CloseStatus.NORMAL);
    }

    private boolean isOpen(PlayerSession player, int row, int col) {
        int[][] grid = player.map.grid();
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[row].length) {
            return false;
        }
        if (grid[row][col] == 0) {
            return true;
        }

        // Nếu là ô tường, kiểm tra xem người chơi có buff WallHack trong Redis hay không
        try {
            var state = gameStateService.getState(player.username);
            return state.wallHack();
        } catch (Exception e) {
            return false;
        }
    }

    private void sendError(WebSocketSession socket, String text) throws IOException {
        send(socket, objectMapper.createObjectNode().put("type", "ERROR").put("message", text));
    }

    private void addCollected(ObjectNode message, PlayerSession player) {
        var cells = message.putArray("collected");
        player.collected.forEach(cells::add);
    }

    private void send(WebSocketSession socket, JsonNode payload) throws IOException {
        if (socket.isOpen()) socket.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) {
        PlayerSession player = sessions.remove(socket.getId());
        if (player != null && !player.finished && player.score > 0) {
            player.finished = true;
            try {
                gameScoreRepository.save(new GameScore(player.username, player.score));
            } catch (Exception ignored) {
            }
        }
    }

    private static final class PlayerSession {
        private final String username;
        private final GameMapCatalog.GameMode mode;
        private final GameMapCatalog.GameMap map;
        private final Set<String> collected = ConcurrentHashMap.newKeySet();
        private int row;
        private int col;
        private int sequence;
        private int score;
        private boolean finished = false;

        private PlayerSession(String username, GameMapCatalog.GameMode mode, GameMapCatalog.GameMap map) {
            this.username = username;
            this.mode = mode;
            this.map = map;
            this.row = map.playerRow();
            this.col = map.playerCol();
        }
    }
}
