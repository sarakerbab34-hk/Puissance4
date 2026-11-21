package com.puissance4;

import com.puissance4.controller.GameController;
import com.puissance4.model.Game;
import com.puissance4.view.GameView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Classe principale JavaFX de l'application Puissance 4.
 * Point d'entrée graphique initialisant le Modèle, la Vue et le Contrôleur (MVC).
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Initialisation du modèle (Back-End)
        Game game = new Game();

        // 2. Initialisation de la vue (Front-End)
        GameView gameView = new GameView();

        // 3. Initialisation du contrôleur (Liaison MVC)
        new GameController(game, gameView);

        // 4. Configuration de la scène et chargement du CSS
        Scene scene = new Scene(gameView, 680, 680);

        URL cssUrl = getClass().getResource("/com/puissance4/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // 5. Configuration de la fenêtre principale
        primaryStage.setTitle("Puissance 4");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(660);
        primaryStage.show();
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
