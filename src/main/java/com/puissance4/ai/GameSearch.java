package com.puissance4.ai;

/**
 * Recherche adversariale générique (Minimax / Alpha-Beta).
 * La méthode alphaBetaHelper du cours est remplacée par max_Value et min_Value.
 *
 * Convention : PROGRAM = false (-1), HUMAN = true (1).
 * Le paramètre "me" est le joueur pour lequel on cherche le meilleur coup :
 * il maximise (max_Value), son adversaire minimise (min_Value).
 */
public abstract class GameSearch {

    public static final boolean PROGRAM = false;
    public static final boolean HUMAN = true;

    private static final float WIN = 1000000f;

    /** true = Alpha-Beta, false = Minimax pur (stratégie choisie par le menu). */
    protected boolean pruning;
    /** Nombre de noeuds explorés lors de la dernière recherche (pour comparer les stratégies). */
    protected long nodesVisited;

    protected GameSearch(boolean pruning) {
        this.pruning = pruning;
    }

    // ---- Méthodes abstraites (cours) ----
    public abstract boolean drawnPosition(Position p);
    public abstract boolean wonPosition(Position p, boolean player);
    /** Évaluation de p du point de vue de "player" (grand = bon pour player). */
    public abstract float positionEvaluation(Position p, boolean player);
    public abstract void printPosition(Position p);
    public abstract Position[] possibleMoves(Position p, boolean player);
    public abstract Position makeMove(Position p, boolean player, Move move);
    public abstract boolean reachedMaxDepth(Position p, int depth);
    public abstract Move createMove();

    public long getNodesVisited() {
        return nodesVisited;
    }

    public boolean isPruning() {
        return pruning;
    }

    /** Lance la recherche : "me" joue au trait dans la position p. */
    public SearchResult search(Position p, boolean me) {
        nodesVisited = 0;
        return max_Value(p, me, 0, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    /** Test d'arrêt : retourne un résultat si p est terminale ou à profondeur max, sinon null. */
    private SearchResult terminalTest(Position p, boolean me, int depth) {
        nodesVisited++;
        if (wonPosition(p, me)) return new SearchResult(WIN - depth, null);   // victoire rapide préférée
        if (wonPosition(p, !me)) return new SearchResult(-WIN + depth, null); // défaite tardive préférée
        if (drawnPosition(p)) return new SearchResult(0f, null);
        if (reachedMaxDepth(p, depth)) return new SearchResult(positionEvaluation(p, me), null);
        return null;
    }

    /** Noeud MAX : c'est à "me" de jouer. */
    protected SearchResult max_Value(Position p, boolean me, int depth, float alpha, float beta) {
        SearchResult t = terminalTest(p, me, depth);
        if (t != null) return t;
        float v = Float.NEGATIVE_INFINITY;
        Position best = null;
        for (Position child : possibleMoves(p, me)) {
            float cv = min_Value(child, me, depth + 1, alpha, beta).value;
            if (cv > v) {
                v = cv;
                best = child;
            }
            if (pruning && v >= beta) return new SearchResult(v, best); // coupure beta
            alpha = Math.max(alpha, v);
        }
        return new SearchResult(v, best);
    }

    /** Noeud MIN : c'est à l'adversaire de "me" de jouer. */
    protected SearchResult min_Value(Position p, boolean me, int depth, float alpha, float beta) {
        SearchResult t = terminalTest(p, me, depth);
        if (t != null) return t;
        float v = Float.POSITIVE_INFINITY;
        Position best = null;
        for (Position child : possibleMoves(p, !me)) {
            float cv = max_Value(child, me, depth + 1, alpha, beta).value;
            if (cv < v) {
                v = cv;
                best = child;
            }
            if (pruning && v <= alpha) return new SearchResult(v, best); // coupure alpha
            beta = Math.min(beta, v);
        }
        return new SearchResult(v, best);
    }
}
