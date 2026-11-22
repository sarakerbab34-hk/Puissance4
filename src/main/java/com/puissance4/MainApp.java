package com.puissance4;

import com.puissance4.controller.GameController;
import com.puissance4.controller.SaveFileDialogs;
import com.puissance4.model.Game;
import com.puissance4.model.GameSave;
import com.puissance4.model.GameSettings;
import com.puissance4.view.GameView;
import com.puissance4.view.MenuView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Classe principale JavaFX de l'application Puissance 4.
 * Affiche d'abord le menu, puis la partie (Modèle, Vue et Contrôleur MVC).
 */
public class MainApp extends Application {

    private Stage stage;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        MenuView menu = createMenu();
        scene = new Scene(menu, 800, 760);

        URL cssUrl = getClass().getResource("/com/puissance4/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("Puissance 4");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(780);
        primaryStage.setMinHeight(720);
        primaryStage.show();
    }

    /** Construit l'écran d'accueil et branche ses boutons. */
    private MenuView createMenu() {
        MenuView menu = new MenuView();
        menu.getStartButton().setOnAction(e -> startGame(menu.getSettings(), null));
        menu.getLoadButton().setOnAction(e -> {
            Path file = SaveFileDialogs.chooseOpenFile(stage);
            if (file == null) {
                return;
            }
            try {
                GameSave save = GameSave.load(file);
                startGame(new GameSettings(save.mode, save.depth, save.pruning, save.helps), save);
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Chargement impossible : " + ex.getMessage()).showAndWait();
            }
        });
        return menu;
    }

    /** Retour à l'écran d'accueil (bouton "Nouvelle partie"). */
    private void showMenu() {
        scene.setRoot(createMenu());
    }

    /** Lance une partie avec les paramètres donnés, éventuellement restaurée depuis une sauvegarde. */
    private void startGame(GameSettings settings, GameSave save) {
        Game game = new Game();
        GameView gameView = new GameView();
        GameController controller = new GameController(game, gameView, settings, this::showMenu);
        if (save != null) {
            controller.loadSave(save);
        }
        scene.setRoot(gameView);
    }

    /**
     * Méthode main standard lançant l'application JavaFX.
     *
     * @param args Arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
