package com.puissance4.ai;

/** Coup de Puissance 4 : le numéro de colonne (0 à 6). */
public class Connect4Move extends Move {
    public final int column;

    public Connect4Move(int column) {
        this.column = column;
    }
}
