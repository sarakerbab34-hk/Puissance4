package com.puissance4.view;

import com.puissance4.model.Board;
import com.puissance4.model.Player;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

/**
 * Composant graphique représentant le plateau de Puissance 4 (Front-End).
 * Gère l'affichage des 6 lignes x 7 colonnes, les cases circulaires,
 * les pions 3D, le survol des colonnes et l'animation de chute par TranslateTransition.
 */
public class BoardView extends StackPane {

    public static final double CELL_RADIUS = 34.0;
    public static final double CELL_SPACING = 12.0;
    public static final double CELL_SIZE = (CELL_RADIUS * 2) + CELL_SPACING; // ~80px par case
    public static final double BOARD_PADDING = 16.0;

    private static final double BOARD_WIDTH = (Board.COLUMNS * CELL_SIZE) + (BOARD_PADDING * 2);
    private static final double BOARD_HEIGHT = (Board.ROWS * CELL_SIZE) + (BOARD_PADDING * 2);

    // Grille 2D des cercles permanents affichés sur le plateau
    private final Circle[][] cellCircles = new Circle[Board.ROWS][Board.COLUMNS];

    // Constantes d'apparence par défaut des cases vides
    private static final Color DEFAULT_STROKE_COLOR = Color.web("#172554");
    private static final double DEFAULT_STROKE_WIDTH = 1.5;

    // Références pour arrêter proprement les animations
    private final java.util.List<Timeline> winningTimelines = new java.util.ArrayList<>();
    private TranslateTransition currentDropTransition = null;
    private Circle currentFallingDisc = null;

    // Conteneur pour les animations de chute au premier plan
    private final Pane animationPane = new Pane();

    // Pion fantôme au-dessus du plateau lors du survol
    private final Circle previewDisc = new Circle(CELL_RADIUS);

    // Callbacks et état
    private Consumer<Integer> onColumnClick;
    private Player currentTurnPlayer = Player.RED;
    private boolean interactionEnabled = true;

    /**
     * Initialise la vue graphique du plateau.
     */
    public BoardView() {
        setAlignment(Pos.CENTER);
        setPrefSize(BOARD_WIDTH, BOARD_HEIGHT + 60);

        // 1. Fond bleu moderne du plateau avec coins arrondis et ombre
        Rectangle boardBackground = new Rectangle(BOARD_WIDTH, BOARD_HEIGHT);
        boardBackground.setArcWidth(25);
        boardBackground.setArcHeight(25);
        boardBackground.setFill(Color.web("#1e40af")); // Bleu profond élégant
        boardBackground.setEffect(new DropShadow(15, 0, 6, Color.rgb(0, 0, 0, 0.35)));

        // 2. Panneau contenant les cases (trous circulaires)
        Pane gridPane = new Pane();
        gridPane.setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);

        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLUMNS; c++) {
                double centerX = getColumnCenterX(c);
                double centerY = getRowCenterY(r);

                Circle hole = createEmptyHole();
                hole.setCenterX(centerX);
                hole.setCenterY(centerY);

                cellCircles[r][c] = hole;
                gridPane.getChildren().add(hole);
            }
        }

        // 3. Zones cliquables et détection de survol par colonne
        Pane clickColumnsPane = new Pane();
        clickColumnsPane.setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);

        for (int c = 0; c < Board.COLUMNS; c++) {
            final int colIndex = c;
            Rectangle colZone = new Rectangle(CELL_SIZE, BOARD_HEIGHT);
            colZone.setX(BOARD_PADDING + (c * CELL_SIZE));
            colZone.setY(0);
            colZone.setFill(Color.TRANSPARENT);
            colZone.setCursor(javafx.scene.Cursor.HAND);

            // Effet visuel au survol
            colZone.setOnMouseEntered(e -> handleColumnHover(colIndex, true));
            colZone.setOnMouseExited(e -> handleColumnHover(colIndex, false));
            colZone.setOnMouseMoved(e -> updatePreviewPosition(colIndex));

            // Clic sur la colonne
            colZone.setOnMouseClicked(e -> {
                if (interactionEnabled && onColumnClick != null) {
                    onColumnClick.accept(colIndex);
                }
            });

            clickColumnsPane.getChildren().add(colZone);
        }

        // 4. Pion fantôme (preview disc)
        previewDisc.setVisible(false);
        previewDisc.setOpacity(0.65);
        previewDisc.setMouseTransparent(true);

        animationPane.setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        animationPane.setMouseTransparent(true);
        animationPane.getChildren().add(previewDisc);

        // Assemblage des couches
        Pane boardStack = new Pane();
        boardStack.setPrefSize(BOARD_WIDTH, BOARD_HEIGHT);
        boardStack.getChildren().addAll(boardBackground, gridPane, clickColumnsPane, animationPane);

        getChildren().add(boardStack);
    }

    /**
     * Calcule la coordonnée X du centre d'une colonne donnée.
     */
    public double getColumnCenterX(int col) {
        return BOARD_PADDING + (col * CELL_SIZE) + (CELL_SIZE / 2.0);
    }

    /**
     * Calcule la coordonnée Y du centre d'une ligne donnée.
     */
    public double getRowCenterY(int row) {
        return BOARD_PADDING + (row * CELL_SIZE) + (CELL_SIZE / 2.0);
    }

    /**
     * Crée le dégradé sombre représentant le fond d'une case vide.
     */
    private static RadialGradient createEmptyHoleGradient() {
        return new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#0f172a")),
                new Stop(0.85, Color.web("#1e293b")),
                new Stop(1.0, Color.web("#334155"))
        );
    }

    /**
     * Crée une case vide (trou avec dégradé d'ombre intérieure).
     */
    private Circle createEmptyHole() {
        Circle circle = new Circle(CELL_RADIUS);
        circle.setFill(createEmptyHoleGradient());
        circle.setStroke(DEFAULT_STROKE_COLOR);
        circle.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        return circle;
    }

    /**
     * Crée un pion avec un rendu 3D réaliste (dégradé radial et reflet).
     *
     * @param player Le joueur dont on veut la couleur de pion.
     * @return Circle configuré avec le dégradé du joueur.
     */
    public static Circle createDisc(Player player) {
        Circle disc = new Circle(CELL_RADIUS);
        applyPlayerStyle(disc, player);
        return disc;
    }

    /**
     * Applique le dégradé 3D au cercle selon le joueur.
     */
    private static void applyPlayerStyle(Circle disc, Player player) {
        RadialGradient gradient;
        if (player == Player.RED) {
            gradient = new RadialGradient(
                    0, 0, 0.35, 0.35, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#f87171")), // Reflet clair
                    new Stop(0.4, Color.web("#ef4444")), // Rouge vif
                    new Stop(0.9, Color.web("#b91c1c")), // Rouge foncé
                    new Stop(1.0, Color.web("#7f1d1d"))  // Bordure ombrée
            );
        } else {
            gradient = new RadialGradient(
                    0, 0, 0.35, 0.35, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#fef08a")), // Reflet clair
                    new Stop(0.4, Color.web("#facc15")), // Jaune vif
                    new Stop(0.9, Color.web("#ca8a04")), // Jaune ambré
                    new Stop(1.0, Color.web("#854d0e"))  // Bordure ombrée
            );
        }
        disc.setFill(gradient);
        disc.setEffect(new DropShadow(4, 0, 2, Color.rgb(0, 0, 0, 0.3)));
    }

    /**
     * Gère l'effet de prévisualisation au survol d'une colonne.
     */
    private void handleColumnHover(int colIndex, boolean isHovered) {
        if (!interactionEnabled || !isHovered) {
            previewDisc.setVisible(false);
            return;
        }
        updatePreviewPosition(colIndex);
        applyPlayerStyle(previewDisc, currentTurnPlayer);
        previewDisc.setVisible(true);
    }

    private void updatePreviewPosition(int colIndex) {
        previewDisc.setCenterX(getColumnCenterX(colIndex));
        previewDisc.setCenterY(BOARD_PADDING - 10);
    }

    /**
     * Lance l'animation de chute du pion depuis le sommet de la colonne jusqu'à sa case cible.
     *
     * @param col        Indice de la colonne.
     * @param targetRow  Indice de la ligne calculée par le modèle.
     * @param player     Joueur ayant joué le coup.
     * @param onFinished Action à exécuter à la fin de l'animation.
     */
    public void playDropAnimation(int col, int targetRow, Player player, Runnable onFinished) {
        // Désactive l'interaction pendant la chute
        setInteractionEnabled(false);
        previewDisc.setVisible(false);

        double startX = getColumnCenterX(col);
        double startY = -CELL_RADIUS * 2; // Part du dessus du plateau
        double targetY = getRowCenterY(targetRow);

        Circle fallingDisc = createDisc(player);
        fallingDisc.setCenterX(startX);
        fallingDisc.setCenterY(startY);
        animationPane.getChildren().add(fallingDisc);
        this.currentFallingDisc = fallingDisc;

        // Durée proportionnelle à la distance parcourue avec un minimum
        double distance = targetY - startY;
        double durationMs = 280 + (targetRow * 35); // Environ 300 à 480 ms

        TranslateTransition transition = new TranslateTransition(Duration.millis(durationMs), fallingDisc);
        transition.setFromY(0);
        transition.setToY(distance);
        // Effet d'accélération gravitationnelle
        transition.setInterpolator(Interpolator.EASE_IN);
        this.currentDropTransition = transition;

        transition.setOnFinished(e -> {
            // Nettoyage du pion animé
            animationPane.getChildren().remove(fallingDisc);
            this.currentFallingDisc = null;
            this.currentDropTransition = null;

            // Fixation permanente du pion dans la case correspondante
            Circle permanentCell = cellCircles[targetRow][col];
            applyPlayerStyle(permanentCell, player);

            // Exécution du callback de fin
            if (onFinished != null) {
                onFinished.run();
            }
        });

        transition.play();
    }

    /**
     * Surligne les cases composant l'alignement gagnant avec un effet lumineux et une pulsation dorée.
     *
     * @param winningPositions Liste des coordonnées [ligne, colonne] des pions gagnants.
     */
    public void highlightWinningCells(List<int[]> winningPositions) {
        if (winningPositions == null) return;

        // Nettoyer d'abord d'éventuelles anciennes animations
        clearWinningHighlights();

        for (int[] pos : winningPositions) {
            int r = pos[0];
            int c = pos[1];
            if (r >= 0 && r < Board.ROWS && c >= 0 && c < Board.COLUMNS) {
                Circle cell = cellCircles[r][c];
                cell.setStroke(Color.web("#fbbf24")); // Bordure jaune or éclatante
                cell.setStrokeWidth(5.0);

                // Effet lumineux / lueur dorée (glow)
                DropShadow winGlow = new DropShadow(18, Color.web("#fbbf24"));
                cell.setEffect(winGlow);

                // Animation de pulsation
                Timeline pulse = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(cell.scaleXProperty(), 1.0), new KeyValue(cell.scaleYProperty(), 1.0)),
                        new KeyFrame(Duration.millis(400), new KeyValue(cell.scaleXProperty(), 1.14), new KeyValue(cell.scaleYProperty(), 1.14)),
                        new KeyFrame(Duration.millis(800), new KeyValue(cell.scaleXProperty(), 1.0), new KeyValue(cell.scaleYProperty(), 1.0))
                );
                pulse.setCycleCount(Timeline.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();

                // Enregistrement de la Timeline pour pouvoir l'arrêter proprement
                winningTimelines.add(pulse);
            }
        }
    }

    /**
     * Supprime complètement tous les effets de surbrillance, lueurs lumineuses
     * et animations de pulsation sur l'ensemble de la grille.
     */
    public void clearWinningHighlights() {
        // 1. Arrêter et vider toutes les animations de pulsation en cours
        for (Timeline timeline : winningTimelines) {
            timeline.stop();
        }
        winningTimelines.clear();

        // 2. Réinitialiser l'échelle, les bordures et les effets graphiques de toutes les cases
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLUMNS; c++) {
                Circle cell = cellCircles[r][c];
                cell.setScaleX(1.0);
                cell.setScaleY(1.0);
                cell.setStroke(DEFAULT_STROKE_COLOR);
                cell.setStrokeWidth(DEFAULT_STROKE_WIDTH);
                cell.setEffect(null);
            }
        }
    }

    /**
     * Réinitialise visuellement l'ensemble des cases du plateau.
     * Arrête toutes les animations, supprime les surbrillances et rétablit les trous vides.
     */
    public void reset() {
        // Arrêt des animations de victoire et réinitialisation des bordures/effets
        clearWinningHighlights();

        // Arrêt d'une éventuelle chute en cours
        if (currentDropTransition != null) {
            currentDropTransition.stop();
            currentDropTransition = null;
        }
        if (currentFallingDisc != null) {
            animationPane.getChildren().remove(currentFallingDisc);
            currentFallingDisc = null;
        }

        animationPane.getChildren().removeIf(node -> node != previewDisc);
        previewDisc.setVisible(false);

        // Rétablissement de toutes les cases dans leur état vide initial
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLUMNS; c++) {
                Circle hole = cellCircles[r][c];
                hole.setScaleX(1.0);
                hole.setScaleY(1.0);
                hole.setStroke(DEFAULT_STROKE_COLOR);
                hole.setStrokeWidth(DEFAULT_STROKE_WIDTH);
                hole.setFill(createEmptyHoleGradient());
                hole.setEffect(null);
            }
        }
        setInteractionEnabled(true);
    }

    /**
     * Place directement un pion dans une case (sans animation), utilisé lors de la restauration d'une partie.
     */
    public void setCellPiece(int row, int col, Player player) {
        applyPlayerStyle(cellCircles[row][col], player);
    }

    public void setOnColumnClick(Consumer<Integer> onColumnClick) {
        this.onColumnClick = onColumnClick;
    }

    public void setCurrentTurnPlayer(Player player) {
        this.currentTurnPlayer = player;
        applyPlayerStyle(previewDisc, player);
    }

    public void setInteractionEnabled(boolean enabled) {
        this.interactionEnabled = enabled;
        if (!enabled) {
            previewDisc.setVisible(false);
        }
    }
}

