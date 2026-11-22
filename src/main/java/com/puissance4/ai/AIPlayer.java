package com.puissance4.ai;

import com.puissance4.model.Board;
import com.puissance4.model.Player;

/**
 * Joueur machine : adaptateur entre le plateau du jeu (Board) et la recherche (Connect4Search).
 * Le joueur pour lequel on cherche le meilleur coup joue le rôle de PROGRAM dans la recherche.
 */
public class AIPlayer {
    private final Connect4Search search;

    public AIPlayer(int depth, boolean pruning) {
        this.search = new Connect4Search(depth, pruning);
    }

    /** Convertit le plateau en position de recherche vue du joueur "me" (me = PROGRAM). */
    public static Connect4Position toPosition(Board b, Player me) {
        Connect4Position pos = new Connect4Position();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLUMNS; c++) {
                Player p = b.getCell(r, c);
                if (p != null) {
                    pos.setCell(r, c, Connect4Position.piece(p == me ? GameSearch.PROGRAM : GameSearch.HUMAN));
                }
            }
        }
        return pos;
    }

    /** Meilleure colonne (0-6) dans la position donnée (me = PROGRAM), -1 si aucun coup. */
    public int bestColumn(Connect4Position pos) {
        return search.bestColumn(pos, GameSearch.PROGRAM);
    }

    /** Meilleure colonne (0-6) pour le joueur "me" sur ce plateau. */
    public int bestColumn(Board b, Player me) {
        return bestColumn(toPosition(b, me));
    }

    /** Nombre de noeuds explorés lors de la dernière recherche. */
    public long getLastNodes() {
        return search.getNodesVisited();
    }
}
