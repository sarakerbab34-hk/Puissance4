package com.puissance4.ai;

import com.puissance4.model.Board;
import com.puissance4.model.Player;

/**
 * Joueur machine : adaptateur entre le plateau du jeu ({@link Board}) et le moteur de recherche
 * ({@link Connect4Search}).
 *
 * <p>Le plateau du jeu et la position de recherche ({@link Connect4Position}) sont deux
 * représentations distinctes. Cette classe convertit l'une en l'autre. Le joueur pour lequel on
 * cherche un coup reçoit toujours le rôle de {@link GameSearch#PROGRAM} : le même code sert donc
 * à faire jouer la machine (Jaunes) et à conseiller un humain (Rouges ou Jaunes).</p>
 *
 * <p>La difficulté et la stratégie fixées au menu sont transmises à la recherche :
 * profondeur maximale et activation (ou non) de l'élagage Alpha-Beta.</p>
 */
public class AIPlayer {

    /** Moteur de recherche Minimax / Alpha-Beta pour Puissance 4. */
    private final Connect4Search search;

    /**
     * Crée un joueur machine.
     *
     * @param depth   profondeur maximale de recherche (2 facile, 4 moyen, 6 difficile)
     * @param pruning {@code true} pour Alpha-Beta, {@code false} pour Minimax pur
     */
    public AIPlayer(int depth, boolean pruning) {
        this.search = new Connect4Search(depth, pruning);
    }

    /**
     * Convertit le plateau en position de recherche vue du joueur {@code me}.
     * Les pions de {@code me} deviennent des pions PROGRAM, ceux de l'adversaire des pions HUMAN.
     *
     * @param b  plateau du jeu
     * @param me joueur pour lequel on cherche un coup
     * @return la position de recherche correspondante
     */
    public static Connect4Position toPosition(Board b, Player me) {
        Connect4Position pos = new Connect4Position();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLUMNS; c++) {
                Player p = b.getCell(r, c);
                if (p != null) {
                    pos.setCell(r, c, Connect4Position.piece(p == me ? GameSearch.PROGRAM : GameSearch.HUMAN));
                }
            }
        }
        return pos;
    }

    /**
     * Cherche le meilleur coup dans une position déjà convertie.
     * Utilisée par l'interface graphique, qui copie la position avant de lancer le calcul dans
     * un thread séparé.
     *
     * @param pos position de recherche, dans laquelle c'est à PROGRAM de jouer
     * @return la meilleure colonne (0 à 6), ou -1 si aucun coup n'est possible
     */
    public int bestColumn(Connect4Position pos) {
        return search.bestColumn(pos, GameSearch.PROGRAM);
    }

    /**
     * Cherche le meilleur coup pour le joueur {@code me} sur ce plateau.
     * Sert à la fois pour le coup de la machine et pour l'aide donnée au joueur.
     *
     * @param b  plateau du jeu
     * @param me joueur au trait
     * @return la meilleure colonne (0 à 6), ou -1 si aucun coup n'est possible
     */
    public int bestColumn(Board b, Player me) {
        return bestColumn(toPosition(b, me));
    }

    /**
     * Indique le coût de la dernière recherche, ce qui permet de comparer Minimax et Alpha-Beta.
     *
     * @return le nombre de nœuds explorés lors de la dernière recherche
     */
    public long getLastNodes() {
        return search.getNodesVisited();
    }
}
