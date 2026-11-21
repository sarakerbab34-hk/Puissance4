package com.puissance4.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Représente la grille de jeu du Puissance 4 (6 lignes par 7 colonnes).
 * Cette classe gère uniquement l'état de la grille et les règles de placement et d'alignement.
 * Elle est totalement indépendante de toute interface graphique (JavaFX).
 *
 * Convention des coordonnées :
 * - Ligne 0 : Sommet de la grille (haut).
 * - Ligne 5 : Fond de la grille (bas, case vide la plus basse au début).
 * - Colonnes : 0 à 6 (de gauche à droite).
 */
public class Board {

    public static final int ROWS = 6;
    public static final int COLUMNS = 7;
    public static final int WIN_COUNT = 4;

    private final Player[][] grid;
    private final List<int[]> lastWinningPositions;

    /**
     * Initialise une nouvelle grille vide de 6 lignes et 7 colonnes.
     */
    public Board() {
        this.grid = new Player[ROWS][COLUMNS];
        this.lastWinningPositions = new ArrayList<>();
        reset();
    }

    /**
     * Réinitialise toutes les cases de la grille (toutes vides).
     */
    public void reset() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                grid[row][col] = null;
            }
        }
        lastWinningPositions.clear();
    }

    /**
     * Vérifie si un indice de colonne est dans les limites valides (0 à 6).
     *
     * @param col Indice de colonne.
     * @return true si la colonne est valide, false sinon.
     */
    public boolean isValidColumn(int col) {
        return col >= 0 && col < COLUMNS;
    }

    /**
     * Vérifie si un indice de ligne est dans les limites valides (0 à 5).
     *
     * @param row Indice de ligne.
     * @return true si la ligne est valide, false sinon.
     */
    public boolean isValidRow(int row) {
        return row >= 0 && row < ROWS;
    }

    /**
     * Vérifie si une colonne est pleine.
     * Une colonne est pleine si sa case la plus haute (ligne 0) est déjà occupée.
     *
     * @param col Indice de colonne.
     * @return true si la colonne est pleine ou invalide, false si un pion peut y tomber.
     */
    public boolean isColumnFull(int col) {
        if (!isValidColumn(col)) {
            return true;
        }
        return grid[0][col] != null;
    }

    /**
     * Place un pion pour un joueur donné dans la colonne spécifiée.
     * Le pion tombe par gravité dans la case vide la plus basse disponible.
     *
     * @param col    Indice de colonne (0 à 6).
     * @param player Joueur qui place le pion (ROUGE ou JAUNE).
     * @return L'indice de la ligne (0 à 5) où le pion s'est posé.
     * @throws IllegalArgumentException si la colonne est hors limites ou si le joueur est null.
     * @throws IllegalStateException    si la colonne est déjà pleine.
     */
    public int placePiece(int col, Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Le joueur ne peut pas être null.");
        }
        if (!isValidColumn(col)) {
            throw new IllegalArgumentException("Indice de colonne invalide : " + col + ". Les colonnes doivent être entre 0 et " + (COLUMNS - 1) + ".");
        }
        if (isColumnFull(col)) {
            throw new IllegalStateException("La colonne " + col + " est déjà pleine.");
        }

        // On parcourt de bas en haut (de la ligne 5 vers la ligne 0)
        for (int row = ROWS - 1; row >= 0; row--) {
            if (grid[row][col] == null) {
                grid[row][col] = player;
                return row;
            }
        }

        // Cas théoriquement impossible si isColumnFull a été vérifié
        throw new IllegalStateException("Impossible de trouver une case vide dans la colonne " + col);
    }

    /**
     * Récupère le contenu d'une case.
     *
     * @param row Indice de ligne (0 à 5).
     * @param col Indice de colonne (0 à 6).
     * @return Le Player occupant la case, ou null si la case est vide.
     * @throws IllegalArgumentException si les coordonnées sont hors limites.
     */
    public Player getCell(int row, int col) {
        if (!isValidRow(row) || !isValidColumn(col)) {
            throw new IllegalArgumentException("Coordonnées hors limites : (" + row + ", " + col + ")");
        }
        return grid[row][col];
    }

    /**
     * Indique si une case est vide.
     *
     * @param row Indice de ligne.
     * @param col Indice de colonne.
     * @return true si la case est vide, false sinon.
     */
    public boolean isCellEmpty(int row, int col) {
        return getCell(row, col) == null;
    }

    /**
     * Vérifie si toute la grille est pleine (plus aucun coup possible).
     *
     * @return true si toutes les colonnes sont pleines, false sinon.
     */
    public boolean isGridFull() {
        for (int col = 0; col < COLUMNS; col++) {
            if (!isColumnFull(col)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si le coup récemment joué à la position (row, col) a provoqué un alignement de 4 pions.
     * Si une victoire est détectée, les positions gagnantes sont enregistrées.
     *
     * @param row Ligne du pion joué.
     * @param col Colonne du pion joué.
     * @return true si 4 pions ou plus sont alignés, false sinon.
     */
    public boolean checkWin(int row, int col) {
        if (!isValidRow(row) || !isValidColumn(col)) {
            return false;
        }

        Player player = grid[row][col];
        if (player == null) {
            return false;
        }

        lastWinningPositions.clear();

        // 4 axes à tester :
        // 1. Horizontal (deltaRow = 0, deltaCol = 1)
        // 2. Vertical (deltaRow = 1, deltaCol = 0)
        // 3. Diagonale descendante \ (deltaRow = 1, deltaCol = 1)
        // 4. Diagonale montante / (deltaRow = -1, deltaCol = 1)
        int[][] directions = {
                {0, 1},   // Horizontal
                {1, 0},   // Vertical
                {1, 1},   // Diagonale descendante
                {-1, 1}   // Diagonale montante
        };

        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];

            List<int[]> aligned = new ArrayList<>();
            aligned.add(new int[]{row, col});

            // Parcours dans le sens positif
            int r = row + dRow;
            int c = col + dCol;
            while (isValidRow(r) && isValidColumn(c) && grid[r][c] == player) {
                aligned.add(new int[]{r, c});
                r += dRow;
                c += dCol;
            }

            // Parcours dans le sens opposé
            r = row - dRow;
            c = col - dCol;
            while (isValidRow(r) && isValidColumn(c) && grid[r][c] == player) {
                aligned.add(new int[]{r, c});
                r -= dRow;
                c -= dCol;
            }

            if (aligned.size() >= WIN_COUNT) {
                lastWinningPositions.addAll(aligned);
                return true;
            }
        }

        return false;
    }

    /**
     * Vérifie si le joueur spécifié a au moins un alignement gagnant de 4 pions sur toute la grille.
     *
     * @param player Joueur à tester.
     * @return true si le joueur possède un alignement gagnant, false sinon.
     */
    public boolean hasWon(Player player) {
        if (player == null) {
            return false;
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                if (grid[r][c] == player && checkWin(r, c)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retourne la liste des coordonnées [ligne, colonne] des pions composant le dernier alignement gagnant détecté.
     *
     * @return Liste non modifiable des positions gagnantes.
     */
    public List<int[]> getLastWinningPositions() {
        return Collections.unmodifiableList(lastWinningPositions);
    }

    /**
     * Retourne le nombre total de pions actuellement placés sur la grille.
     *
     * @return Nombre de pions.
     */
    public int getPieceCount() {
        int count = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                if (grid[r][c] != null) {
                    count++;
                }
            }
        }
        return count;
    }
}

