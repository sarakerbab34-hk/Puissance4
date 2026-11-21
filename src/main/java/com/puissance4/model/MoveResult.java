package com.puissance4.model;

import java.util.List;

/**
 * Encapsule le résultat d'un coup joué dans la partie.
 *
 * @param row              Indice de la ligne où le pion s'est arrêté.
 * @param col              Indice de la colonne choisie.
 * @param player           Le joueur qui vient de placer ce pion.
 * @param isWin            Indique si ce coup a provoqué une victoire.
 * @param isDraw           Indique si ce coup a provoqué un match nul.
 * @param winningPositions Liste des coordonnées [ligne, colonne] des pions gagnants (vide si pas de victoire).
 */
public record MoveResult(
        int row,
        int col,
        Player player,
        boolean isWin,
        boolean isDraw,
        List<int[]> winningPositions
) {
    /**
     * Vérifie si la partie est terminée suite à ce coup.
     *
     * @return true si victoire ou match nul, false sinon.
     */
    public boolean isGameOver() {
        return isWin || isDraw;
    }
}

