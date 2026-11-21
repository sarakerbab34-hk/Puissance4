package com.puissance4.model;

import java.util.Collections;
import java.util.List;

/**
 * Coordonne le déroulement d'une partie de Puissance 4 :
 * - Gestion du joueur courant (Rouge commence par défaut).
 * - Ordonnancement des tours.
 * - Suivi de l'état de la partie (En cours, Victoire Rouge, Victoire Jaune, Match nul).
 * - Refus des coups après la fin de la partie.
 */
public class Game {

    private final Board board;
    private Player currentPlayer;
    private GameState state;
    private Player winner;

    /**
     * Initialise une nouvelle partie avec une grille vide.
     * Le joueur ROUGE commence toujours.
     */
    public Game() {
        this(new Board());
    }

    /**
     * Initialise une nouvelle partie avec une grille donnée (utile pour l'injection ou les tests).
     *
     * @param board Grille de jeu.
     */
    public Game(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("La grille ne peut pas être null.");
        }
        this.board = board;
        this.currentPlayer = Player.RED;
        this.state = GameState.IN_PROGRESS;
        this.winner = null;
    }

    /**
     * Joue un coup pour le joueur courant dans la colonne donnée.
     *
     * @param col Indice de la colonne où déposer le pion (0 à 6).
     * @return Un objet MoveResult décrivant le résultat du coup.
     * @throws IllegalStateException    si la partie est déjà terminée ou si la colonne est pleine.
     * @throws IllegalArgumentException si l'indice de colonne est invalide.
     */
    public MoveResult play(int col) {
        if (isGameOver()) {
            throw new IllegalStateException("La partie est déjà terminée. Aucun coup n'est accepté.");
        }

        Player playerWhoPlayed = this.currentPlayer;
        int row = board.placePiece(col, playerWhoPlayed);

        boolean isWin = board.checkWin(row, col);
        boolean isDraw = false;
        List<int[]> winningPositions = Collections.emptyList();

        if (isWin) {
            this.winner = playerWhoPlayed;
            this.state = (playerWhoPlayed == Player.RED) ? GameState.RED_WON : GameState.YELLOW_WON;
            winningPositions = board.getLastWinningPositions();
        } else if (board.isGridFull()) {
            isDraw = true;
            this.state = GameState.DRAW;
        } else {
            // Passe au tour de l'adversaire
            this.currentPlayer = this.currentPlayer.getOpponent();
        }

        return new MoveResult(row, col, playerWhoPlayed, isWin, isDraw, winningPositions);
    }

    /**
     * Réinitialise la partie : vide la grille, redonne le tour au joueur Rouge,
     * et réinitialise l'état à IN_PROGRESS.
     */
    public void restart() {
        board.reset();
        this.currentPlayer = Player.RED;
        this.state = GameState.IN_PROGRESS;
        this.winner = null;
    }

    /**
     * Indique si la partie est terminée (soit par victoire, soit par match nul).
     *
     * @return true si terminée, false si en cours.
     */
    public boolean isGameOver() {
        return state != GameState.IN_PROGRESS;
    }

    /**
     * @return Le joueur dont c'est le tour de jouer.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * @return L'état actuel de la partie.
     */
    public GameState getState() {
        return state;
    }

    /**
     * @return Le vainqueur de la partie, ou null si la partie est en cours ou match nul.
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * @return La grille de jeu associée.
     */
    public Board getBoard() {
        return board;
    }
}

