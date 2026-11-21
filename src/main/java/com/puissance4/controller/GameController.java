package com.puissance4.controller;

import com.puissance4.model.Game;
import com.puissance4.model.MoveResult;
import com.puissance4.model.Player;
import com.puissance4.view.GameView;

/**
 * Contrôleur du jeu Puissance 4 (Architecture MVC).
 * Fait la passerelle entre l'interface graphique (GameView) et le moteur de règles (Game).
 * Intercepte les clics, vérifie la validité des coups, ordonne les animations
 * et met à jour l'affichage en conséquence.
 */
public class GameController {

    private final Game game;
    private final GameView view;

    // Verrou pour empêcher les clics simultanés pendant qu'une animation de chute est en cours
    private boolean isAnimationRunning = false;

    /**
     * Initialise le contrôleur en connectant la vue et le modèle.
     *
     * @param game Modèle de la partie.
     * @param view Vue de l'interface graphique.
     */
    public GameController(Game game, GameView view) {
        if (game == null || view == null) {
            throw new IllegalArgumentException("Le modèle et la vue ne peuvent pas être null.");
        }
        this.game = game;
        this.view = view;

        initHandlers();
    }

    /**
     * Configure les écouteurs d'événements de l'interface graphique.
     */
    private void initHandlers() {
        // Clic sur une colonne du plateau
        view.getBoardView().setOnColumnClick(this::handleColumnClick);

        // Boutons "Nouvelle partie" et "Recommencer"
        view.getNewGameButton().setOnAction(e -> handleRestart());
        view.getRestartButton().setOnAction(e -> handleRestart());
    }

    /**
     * Gère la sélection d'une colonne par le joueur.
     *
     * @param col Indice de la colonne cliquée (0 à 6).
     */
    public void handleColumnClick(int col) {
        // 1. Vérifier si une animation est déjà en cours
        if (isAnimationRunning) {
            return;
        }

        // 2. Vérifier si la partie est déjà terminée
        if (game.isGameOver()) {
            view.showError("La partie est terminée ! Cliquez sur 'Nouvelle partie' pour rejouer.");
            return;
        }

        // 3. Vérifier si la colonne choisie est pleine
        if (game.getBoard().isColumnFull(col)) {
            view.showError("La colonne " + (col + 1) + " est pleine ! Choisissez une autre colonne.");
            return;
        }

        // 4. Exécuter le coup dans le modèle Back-End
        MoveResult result;
        try {
            result = game.play(col);
        } catch (Exception ex) {
            view.showError(ex.getMessage());
            return;
        }

        // 5. Déclencher l'animation Front-End de chute du pion
        isAnimationRunning = true;
        view.showMessage("Coup joué en colonne " + (col + 1) + "...");

        view.getBoardView().playDropAnimation(result.col(), result.row(), result.player(), () -> {
            // Callback exécuté après la fin de l'animation TranslateTransition
            isAnimationRunning = false;

            if (result.isWin()) {
                // Surligner les 4 pions gagnants
                view.getBoardView().highlightWinningCells(result.winningPositions());
                view.showWinner(result.player());
                view.getBoardView().setInteractionEnabled(false);
            } else if (result.isDraw()) {
                view.showDraw();
                view.getBoardView().setInteractionEnabled(false);
            } else {
                // Passer visuellement au joueur suivant
                Player nextPlayer = game.getCurrentPlayer();
                view.updateCurrentPlayer(nextPlayer);
                view.showMessage("Au tour du joueur " + nextPlayer.getDisplayName() + ". Cliquez sur une colonne.");
                view.getBoardView().setInteractionEnabled(true);
            }
        });
    }

    /**
     * Réinitialise la partie et l'interface utilisateur.
     */
    public void handleRestart() {
        // Interrompre toute animation éventuelle
        isAnimationRunning = false;

        // Réinitialiser la vue (arrêt des animations, suppression des highlights et réinitialisation des cases)
        view.getBoardView().clearWinningHighlights();
        view.getBoardView().reset();

        // Réinitialiser le modèle
        game.restart();

        // Réinitialiser l'affichage
        view.updateCurrentPlayer(Player.RED);
        view.showMessage("Nouvelle partie commencée. Le joueur Rouge commence !");
    }

    public Game getGame() {
        return game;
    }

    public GameView getView() {
        return view;
    }
}

