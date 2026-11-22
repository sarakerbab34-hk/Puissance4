package com.puissance4.ai;

/**
 * Position de Puissance 4 (immuable une fois construite par drop()).
 * Cases : 0 = vide, 1 = HUMAN, -1 = PROGRAM. Ligne 0 = haut, ligne 5 = bas.
 */
public class Connect4Position extends Position {
    public static final int ROWS = 6;
    public static final int COLS = 7;

    private final int[][] grid;
    private final int lastColumn; // dernière colonne jouée (-1 si aucune)

    public Connect4Position() {
        this.grid = new int[ROWS][COLS];
        this.lastColumn = -1;
    }

    private Connect4Position(int[][] grid, int lastColumn) {
        this.grid = grid;
        this.lastColumn = lastColumn;
    }

    /** Valeur d'une case pour un joueur : HUMAN -> 1, PROGRAM -> -1. */
    public static int piece(boolean player) {
        return player == GameSearch.HUMAN ? 1 : -1;
    }

    public int get(int row, int col) {
        return grid[row][col];
    }

    /** Pour construire une position initiale (adaptateur depuis le plateau). */
    public void setCell(int row, int col, int value) {
        grid[row][col] = value;
    }

    public int getLastColumn() {
        return lastColumn;
    }

    public boolean isColumnFull(int col) {
        return grid[0][col] != 0;
    }

    public boolean isFull() {
        for (int c = 0; c < COLS; c++) {
            if (!isColumnFull(c)) return false;
        }
        return true;
    }

    /** Retourne la nouvelle position après la chute d'un pion, ou null si coup interdit. */
    public Connect4Position drop(int col, boolean player) {
        if (col < 0 || col >= COLS || isColumnFull(col)) return null;
        int[][] g = new int[ROWS][];
        for (int r = 0; r < ROWS; r++) g[r] = grid[r].clone();
        for (int r = ROWS - 1; r >= 0; r--) {
            if (g[r][col] == 0) {
                g[r][col] = piece(player);
                break;
            }
        }
        return new Connect4Position(g, col);
    }
}
