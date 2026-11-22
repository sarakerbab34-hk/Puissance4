package com.puissance4.model;

/**
 * Paramètres d'une partie choisis dans le menu.
 *
 * @param mode    1 = deux joueurs, 2 = contre la machine (la machine joue Jaune)
 * @param depth   difficulté = profondeur de recherche (2 facile, 4 moyen, 6 difficile)
 * @param pruning true = Alpha-Beta, false = Minimax
 * @param helps   nombre d'aides autorisées
 */
public record GameSettings(int mode, int depth, boolean pruning, int helps) {
}
