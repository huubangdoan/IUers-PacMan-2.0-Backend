package com.Iuer.Pacman.game;

import com.Iuer.Pacman.denylist.DenyListService;
import com.Iuer.Pacman.entity.GameScore;
import com.Iuer.Pacman.repository.GameScoreRepository;
import com.Iuer.Pacman.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandlerV2 extends TextWebSocketHandler  {
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final DenyListService denyList;
    private final GameScoreRepository gameScoreRepository;
    private final MoveService moveService;
    private final GameMessages messages;
    private final GameResultService resultService;
    private final ConcurrentHashMap<String, PlayerState> sessions = new ConcurrentHashMap<>();

    public GameWebSocketHandlerV2(
            ObjectMapper objectMapper,
            JwtUtil jwtUtil,
            DenyListService denyList,
            GameScoreRepository gameScoreRepository,
            MoveService moveService,
            GameMessages messages,
            GameResultService resultService
    ) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        this.denyList = denyList;
        this.gameScoreRepository = gameScoreRepository;
        this.moveService = moveService;
        this.messages = messages;
        this.resultService = resultService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage message) throws Exception {
        JsonNode request;
        try {
            request = objectMapper.readTree(message.getPayload());
        } catch (Exception exception) {
            messages.sendError(socket, "Invalid JSON message");
            return;
        }

        PlayerState player = sessions.get(socket.getId());
        String type = request.path("type").asText();
        if (player == null) {
            if (!"AUTH".equals(type)) {
                messages.sendError(socket, "Authenticate before sending game actions");
                socket.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            authenticate(socket, request);
            return;
        }

        switch (type) {
            case "MOVE" -> messages.send(socket, messages.state(moveService.process(player, MoveRequest.from(request))));
            case "FINISH" -> finish(socket, player);
            default -> messages.sendError(socket, "Unsupported game message");
        }
    }

    private void authenticate(WebSocketSession socket, JsonNode request) throws IOException {
        String token = request.path("token").asText();
        if (token.isBlank() || !jwtUtil.validateToken(token)) {
            messages.sendError(socket, "Invalid or expired token");
            socket.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        Claims claims = jwtUtil.extractAllClaims(token);
        if (claims.getSubject() == null || claims.getId() == null
                || denyList.isDenied(claims.getId())
                || denyList.isIssuedBeforeRevoke(claims.getSubject(), claims.getIssuedAt())) {
            messages.sendError(socket, "Token has been revoked");
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
            messages.sendError(socket, exception.getMessage());
            socket.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        if (!requestedMapId.equals(map.id())) {
            messages.sendError(socket, "Game mode does not match the selected map");
            socket.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        PlayerState player = new PlayerState(claims.getSubject(), mode, map, Instant.now(), System.nanoTime());
        sessions.put(socket.getId(), player);
        long bestScore = gameScoreRepository.findBestScoreByUsername(player.username());
        messages.send(socket, messages.ready(player, bestScore));
    }

    private void finish(WebSocketSession socket, PlayerState player) throws IOException {
        saveOnce(player);
        long bestScore = gameScoreRepository.findBestScoreByUsername(player.username());
        messages.send(socket, messages.finished(player.score(), bestScore));
        sessions.remove(socket.getId());
        socket.close(CloseStatus.NORMAL);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) {
        PlayerState player = sessions.remove(socket.getId());
        if (player != null) {
            try {
                saveOnce(player);
            } catch (Exception ignored) {
            }
        }
    }
    private void saveOnce(PlayerState player) {
        synchronized (player) {
            if (player.isFinished()) return;
            player.markFinished();
        }
        if (player.score() <= 0) return;
        gameScoreRepository.save(new GameScore(player.username(), player.score()));
        resultService.record(player);
    }
}
