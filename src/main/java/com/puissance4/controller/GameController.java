package com.puissance4.controller;

import com.puissance4.ai.AIPlayer;
import com.puissance4.ai.Connect4Position;
import com.puissance4.model.Game;
import com.puissance4.model.GameSave;
import com.puissance4.model.GameSettings;
import com.puissance4.model.MoveResult;
import com.puissance4.model.Player;
import com.puissance4.view.GameView;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur du jeu Puissance 4 (Architecture MVC).
 * Fait la passerelle entre l'interface graphique (GameView) et le moteur de règles (Game).
 * Gère les clics, les animations, la machine (Minimax / Alpha-Beta), l'aide,
 * la sauvegarde et la restauration des parties.
 */
public class GameController {

    private final Game game;
    private final GameView view;
    private final Runnable onNewGame; // retour au menu (peut être null)

    // Paramètres de la partie (modifiés par un chargement)
    private int mode;
    private int depth;
    private boolean pruning;
    private int helpsLeft;
    private int initialHelps;

    /** Colonnes jouées depuis le début (pour la sauvegarde). */
    private final List<Integer> moves = new ArrayList<>();

    // Verrou pour empêcher les clics simultanés pendant qu'une animation de chute est en cours
    private boolean isAnimationRunning = false;
    // Vrai pendant que la machine calcule son coup
    private boolean machineThinking = false;
    // Change à chaque nouvelle partie / chargement : ignore les résultats d'une ancienne partie
    private int gameToken = 0;

    /** Constructeur de compatibilité : deux joueurs, sans aide ni menu. */
    public GameController(Game game, GameView view) {
        this(game, view, new GameSettings(1, 4, true, 0), null);
    }

    /**
     * @param game      Modèle de la partie.
     * @param view      Vue de l'interface graphique.
     * @param settings  Paramètres choisis dans le menu.
     * @param onNewGame Action du bouton "Nouvelle partie" (retour au menu), ou null.
     */
    public GameController(Game game, GameView view, GameSettings settings, Runnable onNewGame) {
        if (game == null || view == null || settings == null) {
            throw new IllegalArgumentException("Le modèle, la vue et les paramètres ne peuvent pas être null.");
        }
        this.game = game;
        this.view = view;
        this.onNewGame = onNewGame;
        this.mode = settings.mode();
        this.depth = settings.depth();
        this.pruning = settings.pruning();
        this.helpsLeft = settings.helps();
        this.initialHelps = settings.helps();

        initHandlers();
        view.setHelpCount(helpsLeft);
        view.showMessage(mode == 2
                ? "Vous jouez Rouge contre la machine (Jaune). Cliquez sur une colonne."
                : "Cliquez sur une colonne pour déposer votre pion.");
    }

    private void initHandlers() {
        view.getBoardView().setOnColumnClick(this::handleColumnClick);
        view.getNewGameButton().setOnAction(e -> handleNewGame());
        view.getRestartButton().setOnAction(e -> handleRestart());
        view.getHelpButton().setOnAction(e -> handleHelp());
        view.getSaveButton().setOnAction(e -> handleSave());
        view.getLoadButton().setOnAction(e -> handleLoad());
    }

    // ------------------------------------------------------------------ coups

    /** Clic du joueur humain sur une colonne (0 à 6). */
    public void handleColumnClick(int col) {
        if (isAnimationRunning || machineThinking) {
            return;
        }
        if (game.isGameOver()) {
            view.showError("La partie est terminée ! Cliquez sur 'Nouvelle partie' pour rejouer.");
            return;
        }
        if (mode == 2 && game.getCurrentPlayer() == Player.YELLOW) {
            return; // tour de la machine
        }
        playMove(col);
    }

    /** Joue un coup (humain ou machine) : modèle, animation, puis suite de la partie. */
    private void playMove(int col) {
        if (game.isGameOver()) {
            view.showError("La partie est terminée ! Cliquez sur 'Nouvelle partie' pour rejouer.");
            return;
        }
        if (game.getBoard().isColumnFull(col)) {
            view.showError("La colonne " + (col + 1) + " est pleine ! Choisissez une autre colonne.");
            return;
        }

        MoveResult result;
        try {
            result = game.play(col);
        } catch (Exception ex) {
            view.showError(ex.getMessage());
            return;
        }
        moves.add(col);

        isAnimationRunning = true;
        final int token = gameToken;
        view.showMessage("Coup joué en colonne " + (col + 1) + "...");

        view.getBoardView().playDropAnimation(result.col(), result.row(), result.player(), () -> {
            if (token != gameToken) {
                return; // la partie a été réinitialisée entre-temps
            }
            isAnimationRunning = false;

            if (result.isWin()) {
                view.getBoardView().highlightWinningCells(result.winningPositions());
                view.showWinner(result.player());
                view.getBoardView().setInteractionEnabled(false);
            } else if (result.isDraw()) {
                view.showDraw();
                view.getBoardView().setInteractionEnabled(false);
            } else {
                Player next = game.getCurrentPlayer();
                view.updateCurrentPlayer(next);
                if (mode == 2 && next == Player.YELLOW) {
                    startMachineTurn();
                } else {
                    view.showMessage("Au tour du joueur " + next.getDisplayName() + ". Cliquez sur une colonne.");
                    view.getBoardView().setInteractionEnabled(true);
                }
            }
        });
    }

    // ---------------------------------------------------------------- machine

    /** Lance le calcul du coup de la machine dans un thread séparé (l'interface reste fluide). */
    private void startMachineTurn() {
        machineThinking = true;
        view.getBoardView().setInteractionEnabled(false);
        view.showMessage("🤖 La machine réfléchit...");

        final int token = gameToken;
        final AIPlayer ai = new AIPlayer(depth, pruning);
        // La position est copiée ici (thread JavaFX) : le thread de calcul n'accède pas au modèle.
        final Connect4Position pos = AIPlayer.toPosition(game.getBoard(), Player.YELLOW);

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() {
                return ai.bestColumn(pos);
            }
        };
        task.setOnSucceeded(e -> {
            if (token != gameToken) {
                return;
            }
            machineThinking = false;
            playMove(task.getValue());
        });
        task.setOnFailed(e -> {
            if (token != gameToken) {
                return;
            }
            machineThinking = false;
            view.showError("Erreur de l'IA : " + task.getException());
            view.getBoardView().setInteractionEnabled(true);
        });

        Thread t = new Thread(task, "ai-search");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------- aide

    /** Suggère le meilleur coup au joueur courant (consomme une aide). */
    public void handleHelp() {
        if (isAnimationRunning || machineThinking) {
            return;
        }
        if (game.isGameOver()) {
            view.showError("La partie est terminée.");
            return;
        }
        if (helpsLeft <= 0) {
            view.showError("Vous n'avez plus d'aide disponible.");
            return;
        }
        AIPlayer ai = new AIPlayer(depth, pruning);
        int col = ai.bestColumn(game.getBoard(), game.getCurrentPlayer());
        if (col < 0) {
            return;
        }
        helpsLeft--;
        view.setHelpCount(helpsLeft);
        view.showMessage("💡 Suggestion : colonne " + (col + 1) + " ("
                + (pruning ? "Alpha-Beta" : "Minimax") + ", " + ai.getLastNodes() + " nœuds explorés)");
    }

    // ------------------------------------------------- sauvegarde / chargement

    private void handleSave() {
        Path file = SaveFileDialogs.chooseSaveFile(view.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            new GameSave(mode, depth, pruning, helpsLeft, moves).save(file);
            view.showMessage("💾 Partie sauvegardée : " + file.getFileName());
        } catch (IOException ex) {
            view.showError("Sauvegarde impossible : " + ex.getMessage());
        }
    }

    private void handleLoad() {
        if (isAnimationRunning) {
            return;
        }
        Path file = SaveFileDialogs.chooseOpenFile(view.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            loadSave(GameSave.load(file));
        } catch (IOException ex) {
            view.showError("Chargement impossible : " + ex.getMessage());
        }
    }

    /** Restaure une partie sauvegardée : rejoue les coups sans animation. */
    public void loadSave(GameSave save) {
        gameToken++;
        machineThinking = false;
        isAnimationRunning = false;

        view.getBoardView().clearWinningHighlights();
        view.getBoardView().reset();
        game.restart();
        moves.clear();

        this.mode = save.mode;
        this.depth = save.depth;
        this.pruning = save.pruning;
        this.helpsLeft = save.helps;
        this.initialHelps = save.helps;
        view.setHelpCount(helpsLeft);

        MoveResult last = null;
        for (int col : save.moves) {
            last = game.play(col);
            moves.add(col);
            view.getBoardView().setCellPiece(last.row(), last.col(), last.player());
        }

        if (last != null && last.isWin()) {
            view.getBoardView().highlightWinningCells(last.winningPositions());
            view.showWinner(last.player());
            view.getBoardView().setInteractionEnabled(false);
        } else if (last != null && last.isDraw()) {
            view.showDraw();
            view.getBoardView().setInteractionEnabled(false);
        } else {
            Player next = game.getCurrentPlayer();
            view.updateCurrentPlayer(next);
            view.showMessage("📂 Partie chargée (" + moves.size() + " coups). Au tour du joueur "
                    + next.getDisplayName() + ".");
            if (mode == 2 && next == Player.YELLOW) {
                startMachineTurn();
            } else {
                view.getBoardView().setInteractionEnabled(true);
            }
        }
    }

    // ------------------------------------------------------ nouvelle partie

    /** Bouton "Nouvelle partie" : retour au menu pour choisir de nouveaux paramètres. */
    public void handleNewGame() {
        if (onNewGame != null) {
            gameToken++;
            onNewGame.run();
        } else {
            handleRestart();
        }
    }

    /** Bouton "Recommencer" : même paramètres, grille vide. */
    public void handleRestart() {
        gameToken++;
        machineThinking = false;
        isAnimationRunning = false;

        view.getBoardView().clearWinningHighlights();
        view.getBoardView().reset();

        game.restart();
        moves.clear();
        helpsLeft = initialHelps;
        view.setHelpCount(helpsLeft);

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
