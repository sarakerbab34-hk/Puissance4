package com.puissance4.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Puissance 4 : implémentation des méthodes abstraites de GameSearch. */
public class Connect4Search extends GameSearch {

    /** Ordre d'exploration : colonnes centrales d'abord (meilleur élagage). */
    private static final int[] ORDER = {3, 2, 4, 1, 5, 0, 6};
    private static final int[][] DIRS = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

    private final int maxDepth; // difficulté = profondeur de recherche
    private final Scanner in;   // pour createMove()

    public Connect4Search(int maxDepth, boolean pruning) {
        this(maxDepth, pruning, null);
    }

    public Connect4Search(int maxDepth, boolean pruning, Scanner in) {
        super(pruning);
        this.maxDepth = maxDepth;
        this.in = in;
    }

    /** Meilleure colonne (0-6) pour "player" dans p, ou -1 si aucun coup. */
    public int bestColumn(Connect4Position p, boolean player) {
        SearchResult r = search(p, player);
        return r.position == null ? -1 : ((Connect4Position) r.position).getLastColumn();
    }

    @Override
    public boolean drawnPosition(Position p) {
        return ((Connect4Position) p).isFull();
    }

    @Override
    public boolean wonPosition(Position p, boolean player) {
        Connect4Position pos = (Connect4Position) p;
        int piece = Connect4Position.piece(player);
        for (int r = 0; r < Connect4Position.ROWS; r++) {
            for (int c = 0; c < Connect4Position.COLS; c++) {
                for (int[] d : DIRS) {
                    if (countWindow(pos, r, c, d, piece) == 4) return true;
                }
            }
        }
        return false;
    }

    /** Nombre de pions "piece" dans la fenêtre de 4 cases ; -1 si hors grille ou fenêtre mixte. */
    private int countWindow(Connect4Position pos, int r, int c, int[] d, int piece) {
        int er = r + 3 * d[0], ec = c + 3 * d[1];
        if (er < 0 || er >= Connect4Position.ROWS || ec < 0 || ec >= Connect4Position.COLS) return -1;
        int mine = 0;
        for (int k = 0; k < 4; k++) {
            int v = pos.get(r + k * d[0], c + k * d[1]);
            if (v == piece) mine++;
            else if (v != 0) return -1;
        }
        return mine;
    }

    /** Évaluation heuristique : fenêtres de 4 + bonus colonne centrale. */
    @Override
    public float positionEvaluation(Position p, boolean player) {
        Connect4Position pos = (Connect4Position) p;
        int me = Connect4Position.piece(player);
        int opp = -me;
        float score = 0;
        for (int r = 0; r < Connect4Position.ROWS; r++) {
            if (pos.get(r, 3) == me) score += 3;
            else if (pos.get(r, 3) == opp) score -= 3;
        }
        for (int r = 0; r < Connect4Position.ROWS; r++) {
            for (int c = 0; c < Connect4Position.COLS; c++) {
                for (int[] d : DIRS) {
                    int m = countWindow(pos, r, c, d, me);
                    if (m == 3) score += 5;
                    else if (m == 2) score += 2;
                    int o = countWindow(pos, r, c, d, opp);
                    if (o == 3) score -= 4;
                    else if (o == 2) score -= 1;
                }
            }
        }
        return score;
    }

    @Override
    public void printPosition(Position p) {
        Connect4Position pos = (Connect4Position) p;
        System.out.println(" 1 2 3 4 5 6 7");
        for (int r = 0; r < Connect4Position.ROWS; r++) {
            StringBuilder sb = new StringBuilder("|");
            for (int c = 0; c < Connect4Position.COLS; c++) {
                int v = pos.get(r, c);
                sb.append(v == 0 ? "." : v == 1 ? "H" : "P").append("|");
            }
            System.out.println(sb);
        }
    }

    @Override
    public Position[] possibleMoves(Position p, boolean player) {
        Connect4Position pos = (Connect4Position) p;
        List<Position> moves = new ArrayList<>();
        for (int c : ORDER) {
            Connect4Position child = pos.drop(c, player);
            if (child != null) moves.add(child);
        }
        return moves.toArray(new Position[0]);
    }

    @Override
    public Position makeMove(Position p, boolean player, Move move) {
        return ((Connect4Position) p).drop(((Connect4Move) move).column, player);
    }

    @Override
    public boolean reachedMaxDepth(Position p, int depth) {
        return depth >= maxDepth;
    }

    @Override
    public Move createMove() {
        System.out.print("Column (1-7): ");
        return new Connect4Move(Integer.parseInt(in.nextLine().trim()) - 1);
    }
}
