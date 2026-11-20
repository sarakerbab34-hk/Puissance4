package com.puissance4.view;

import com.puissance4.model.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Vue principale de l'application Puissance 4 (Front-End).
 * Assemble la barre d'outils supérieure, le plateau de jeu graphique et le bandeau de messages inférieur.
 */
public class GameView extends BorderPane {

    private final BoardView boardView;
    private final Label titleLabel;
    private final Label turnLabel;
    private final Circle turnIndicatorCircle;
    private final HBox turnBadgeBox;
    private final Button newGameButton;
    private final Button restartButton;
    private final Label statusMessageLabel;

    /**
     * Initialise la vue complète du jeu Puissance 4.
     */
    public GameView() {
        this.boardView = new BoardView();

        // 1. Barre supérieure (Header)
        this.titleLabel = new Label("Puissance 4");
        this.titleLabel.getStyleClass().add("game-title");

        this.turnIndicatorCircle = new Circle(8);
        this.turnLabel = new Label("Tour : Joueur Rouge");
        this.turnBadgeBox = new HBox(8, turnIndicatorCircle, turnLabel);
        this.turnBadgeBox.setAlignment(Pos.CENTER);
        this.turnBadgeBox.getStyleClass().addAll("turn-badge", "turn-badge-red");

        this.newGameButton = new Button("Nouvelle partie");
        this.newGameButton.getStyleClass().add("action-button");

        this.restartButton = new Button("Recommencer");
        this.restartButton.getStyleClass().add("restart-button");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox buttonBox = new HBox(10, restartButton, newGameButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(20, titleLabel, spacer1, turnBadgeBox, spacer2, buttonBox);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("header-box");
        setTop(header);

        // 2. Zone centrale : Plateau de jeu
        VBox centerBox = new VBox(boardView);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20, 20, 10, 20));
        setCenter(centerBox);

        // 3. Barre inférieure (Footer / Messages)
        this.statusMessageLabel = new Label("Cliquez sur une colonne pour déposer votre pion.");
        this.statusMessageLabel.getStyleClass().addAll("status-message");

        HBox footer = new HBox(statusMessageLabel);
        footer.setAlignment(Pos.CENTER);
        footer.getStyleClass().add("status-footer");
        setBottom(footer);

        // Mise à jour initiale du badge
        updateCurrentPlayer(Player.RED);
    }

    /**
     * Met à jour l'affichage de l'indicateur du joueur courant.
     *
     * @param player Le joueur dont c'est le tour.
     */
    public void updateCurrentPlayer(Player player) {
        boardView.setCurrentTurnPlayer(player);

        turnBadgeBox.getStyleClass().removeAll("turn-badge-red", "turn-badge-yellow");
        if (player == Player.RED) {
            turnLabel.setText("Tour : Joueur Rouge");
            turnBadgeBox.getStyleClass().add("turn-badge-red");
            turnIndicatorCircle.setFill(javafx.scene.paint.Color.web("#ef4444"));
        } else {
            turnLabel.setText("Tour : Joueur Jaune");
            turnBadgeBox.getStyleClass().add("turn-badge-yellow");
            turnIndicatorCircle.setFill(javafx.scene.paint.Color.web("#eab308"));
        }
    }

    /**
     * Affiche un message d'information standard dans le bandeau inférieur.
     */
    public void showMessage(String message) {
        statusMessageLabel.getStyleClass().removeAll("status-error", "status-win", "status-draw");
        statusMessageLabel.setText(message);
    }

    /**
     * Affiche un message d'erreur (ex: colonne pleine ou clic hors jeu).
     */
    public void showError(String message) {
        statusMessageLabel.getStyleClass().removeAll("status-win", "status-draw");
        if (!statusMessageLabel.getStyleClass().contains("status-error")) {
            statusMessageLabel.getStyleClass().add("status-error");
        }
        statusMessageLabel.setText("⚠️ " + message);
    }

    /**
     * Affiche l'annonce de la victoire.
     */
    public void showWinner(Player winner) {
        statusMessageLabel.getStyleClass().removeAll("status-error", "status-draw");
        if (!statusMessageLabel.getStyleClass().contains("status-win")) {
            statusMessageLabel.getStyleClass().add("status-win");
        }
        statusMessageLabel.setText("🎉 Victoire éclatante du joueur " + winner.getDisplayName() + " ! Félicitations !");
        turnLabel.setText("Partie gagnée par " + winner.getDisplayName());
    }

    /**
     * Affiche l'annonce du match nul.
     */
    public void showDraw() {
        statusMessageLabel.getStyleClass().removeAll("status-error", "status-win");
        if (!statusMessageLabel.getStyleClass().contains("status-draw")) {
            statusMessageLabel.getStyleClass().add("status-draw");
        }
        statusMessageLabel.setText("🤝 Match nul ! La grille est entièrement pleine.");
        turnLabel.setText("Match nul !");
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public Button getNewGameButton() {
        return newGameButton;
    }

    public Button getRestartButton() {
        return restartButton;
    }
}

