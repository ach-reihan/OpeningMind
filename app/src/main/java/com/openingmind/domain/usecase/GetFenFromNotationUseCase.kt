package com.openingmind.domain.usecase

import android.util.Log
import com.github.bhlangonijr.chesslib.Board
import javax.inject.Inject

class GetFenFromNotationUseCase @Inject constructor() {

    /**
     * Parses a PGN notation string (e.g. "1. e4 e5") and returns the final FEN string.
     * If parsing fails or the notation is invalid/empty, it returns the standard starting position FEN.
     */
    operator fun invoke(notation: String): String {
        val defaultFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        if (notation.isBlank()) return defaultFen
        return try {
            val moveList = com.github.bhlangonijr.chesslib.move.MoveList()
            moveList.loadFromSan(notation)
            
            val board = Board()
            moveList.forEach { board.doMove(it) }
            
            board.fen
        } catch (e: Exception) {
            Log.e("GetFenUseCase", "Failed to parse notation: $notation", e)
            defaultFen
        }
    }
}
