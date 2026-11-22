package com.puissance4.ai;

/** Résultat d'une recherche : valeur minimax et meilleure position fille. */
public class SearchResult {
    public final float value;
    public final Position position;

    public SearchResult(float value, Position position) {
        this.value = value;
        this.position = position;
    }
}
