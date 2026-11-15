package com.Iuer.Pacman.game;

import java.util.List;

// Kết quả xử lý một lệnh MOVE, đủ để dựng thông điệp STATE gửi lại frontend
public record MoveResult(
        boolean accepted,
        int sequence,
        int row,
        int col,
        int score,
        List<String> collected
) {}