package com.puissance4.model;

/**
 * Représente les deux joueurs du jeu Puissance 4.
 */
public enum Player {
    RED("Rouge", "#E74C3C"),
    YELLOW("Jaune", "#F1C40F");

    private final String displayName;
    private final String colorHex;

    Player(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    /**
     * @return Le nom d'affichage du joueur (ex: "Rouge", "Jaune").
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Le code hexadécimal de la couleur associée au pion.
     */
    public String getColorHex() {
        return colorHex;
    }

    /**
     * Retourne l'adversaire du joueur courant.
     *
     * @return YELLOW si le joueur est RED, RED si le joueur est YELLOW.
     */
    public Player getOpponent() {
        return this == RED ? YELLOW : RED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

