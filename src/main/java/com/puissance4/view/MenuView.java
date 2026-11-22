package com.puissance4.view;

import com.puissance4.model.GameSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Écran d'accueil : choix du mode, de la difficulté, de la stratégie et du nombre d'aides.
 */
public class MenuView extends VBox {

    private final RadioButton twoPlayersRadio = new RadioButton("Deux joueurs");
    private final RadioButton vsMachineRadio = new RadioButton("Contre la machine");
    private final ComboBox<String> difficultyBox = new ComboBox<>();
    private final ComboBox<String> strategyBox = new ComboBox<>();
    private final Spinner<Integer> helpsSpinner = new Spinner<>(0, 9, 3);
    private final Button startButton = new Button("Jouer");
    private final Button loadButton = new Button("📂 Charger une partie");

    public MenuView() {
        super(22);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(30));

        Label title = new Label("Puissance 4");
        title.getStyleClass().add("game-title");
        Label subtitle = new Label("Paramètres de la partie");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");

        ToggleGroup modeGroup = new ToggleGroup();
        twoPlayersRadio.setToggleGroup(modeGroup);
        vsMachineRadio.setToggleGroup(modeGroup);
        vsMachineRadio.setSelected(true);
        HBox modeBox = new HBox(18, twoPlayersRadio, vsMachineRadio);

        difficultyBox.getItems().addAll("Facile", "Moyen", "Difficile");
        difficultyBox.getSelectionModel().select(1);
        strategyBox.getItems().addAll("Minimax", "Alpha-Beta");
        strategyBox.getSelectionModel().select(1);
        helpsSpinner.setEditable(false);
        helpsSpinner.setPrefWidth(90);

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(14);
        form.setAlignment(Pos.CENTER);
        form.addRow(0, new Label("Mode de jeu"), modeBox);
        form.addRow(1, new Label("Difficulté"), difficultyBox);
        form.addRow(2, new Label("Stratégie (machine et aide)"), strategyBox);
        form.addRow(3, new Label("Nombre d'aides"), helpsSpinner);

        // La difficulté et la stratégie servent aussi à l'aide, donc restent actives dans les deux modes.
        startButton.getStyleClass().add("action-button");
        loadButton.getStyleClass().add("restart-button");
        HBox buttons = new HBox(12, startButton, loadButton);
        buttons.setAlignment(Pos.CENTER);

        getChildren().addAll(title, subtitle, form, buttons);
    }

    /** Lit les paramètres choisis : difficulté -> profondeur 2 / 4 / 6. */
    public GameSettings getSettings() {
        int mode = vsMachineRadio.isSelected() ? 2 : 1;
        int depth = new int[]{2, 4, 6}[difficultyBox.getSelectionModel().getSelectedIndex()];
        boolean pruning = strategyBox.getSelectionModel().getSelectedIndex() == 1;
        return new GameSettings(mode, depth, pruning, helpsSpinner.getValue());
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getLoadButton() {
        return loadButton;
    }
}
