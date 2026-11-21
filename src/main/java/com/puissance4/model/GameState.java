package com.puissance4.model;

/**
 * Représente les différents états possibles d'une partie de Puissance 4.
 */
public enum GameState {
    IN_PROGRESS("Partie en cours"),
    RED_WON("Victoire du joueur Rouge !"),
    YELLOW_WON("Victoire du joueur Jaune !"),
    DRAW("Match nul ! La grille est pleine.");

    private final String message;

    GameState(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

