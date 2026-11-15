package com.Iuer.Pacman.game;

import com.Iuer.Pacman.service.GameStateService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoveService {
    private final GameStateService gameStateService;

    public MoveService(GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }

    public MoveResult process(PlayerState player, MoveRequest request) {
        Direction direction = Direction.fromName(request.direction());

        // Điều kiện được kiểm tra theo đúng thứ tự (dừng sớm) như code gốc,
        // nên Redis chỉ bị gọi khi ô đích là tường và các điều kiện trước đó đã đúng.
        boolean accepted = request.sequence() == player.sequence() + 1
                && direction != null
                && request.row() == player.row() + direction.rowDelta()
                && request.col() == player.col() + direction.colDelta()
                && isOpen(player, request.row(), request.col());

        player.advanceSequenceTo(request.sequence());
        if (accepted) {
            player.moveTo(request.row(), request.col());
        }

        return new MoveResult(
                accepted,
                request.sequence(),
                player.row(),
                player.col(),
                player.score(),
                List.copyOf(player.collected())
        );
    }

    private boolean isOpen(PlayerState player, int row, int col) {
        int[][] grid = player.map().grid();
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[row].length) {
            return false;
        }
        if (grid[row][col] == 0) {
            return true;
        }

        // Ô tường: chỉ đi được nếu người chơi đang có buff WallHack trong Redis
        try {
            return gameStateService.getState(player.username()).wallHack();
        } catch (Exception e) {
            return false;
        }
    }
}