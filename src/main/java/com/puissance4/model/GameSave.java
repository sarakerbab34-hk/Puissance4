package com.puissance4.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sauvegarde et restauration d'une partie.
 *
 * <p>Une partie est entièrement déterminée par ses paramètres (mode, difficulté, stratégie, aides
 * restantes) et par la suite des colonnes jouées. On enregistre donc ces informations dans un
 * fichier texte lisible ; la restauration rejoue les coups sur un nouvel objet {@link Game}, ce
 * qui garantit que l'état reconstruit respecte exactement les règles du jeu.</p>
 *
 * <p>Format du fichier :</p>
 * <pre>
 * P4SAVE v1
 * mode=2
 * depth=4
 * pruning=true
 * helps=3
 * moves=3,3,2,4
 * </pre>
 * <p>Les colonnes de {@code moves} sont numérotées de 0 à 6.</p>
 */
public class GameSave {

    /** Première ligne obligatoire d'un fichier de sauvegarde (identifie le format et sa version). */
    private static final String HEADER = "P4SAVE v1";

    /** Mode de jeu : 1 = deux joueurs, 2 = contre la machine. */
    public final int mode;
    /** Difficulté, exprimée en profondeur de recherche. */
    public final int depth;
    /** {@code true} = Alpha-Beta, {@code false} = Minimax pur. */
    public final boolean pruning;
    /** Nombre d'aides restantes au moment de la sauvegarde. */
    public final int helps;
    /** Colonnes jouées (0 à 6), dans l'ordre. */
    public final List<Integer> moves;

    /**
     * Crée une sauvegarde en mémoire (la liste des coups est copiée).
     *
     * @param mode    1 = deux joueurs, 2 = contre la machine
     * @param depth   profondeur de recherche
     * @param pruning {@code true} pour Alpha-Beta
     * @param helps   aides restantes
     * @param moves   colonnes jouées (0 à 6), dans l'ordre
     */
    public GameSave(int mode, int depth, boolean pruning, int helps, List<Integer> moves) {
        this.mode = mode;
        this.depth = depth;
        this.pruning = pruning;
        this.helps = helps;
        this.moves = new ArrayList<>(moves);
    }

    /**
     * Écrit la sauvegarde dans un fichier texte (le fichier est créé ou écrasé).
     *
     * @param file chemin du fichier de destination
     * @throws IOException si l'écriture échoue
     */
    public void save(Path file) throws IOException {
        String m = moves.stream().map(String::valueOf).collect(Collectors.joining(","));
        String content = HEADER + "\n"
                + "mode=" + mode + "\n"
                + "depth=" + depth + "\n"
                + "pruning=" + pruning + "\n"
                + "helps=" + helps + "\n"
                + "moves=" + m + "\n";
        Files.writeString(file, content);
    }

    /**
     * Lit et valide une sauvegarde. Le fichier est refusé s'il est absent, si son en-tête est
     * incorrect, si une valeur est illisible ou invraisemblable, ou si l'un des coups est illégal
     * (la partie est rejouée entièrement pour le vérifier).
     *
     * @param file chemin du fichier à lire
     * @return la sauvegarde lue
     * @throws IOException si le fichier est absent, invalide ou corrompu
     */
    public static GameSave load(Path file) throws IOException {
        if (!Files.exists(file)) throw new IOException("Aucune sauvegarde trouvée.");
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty() || !lines.get(0).equals(HEADER)) {
            throw new IOException("Ce fichier n'est pas une sauvegarde valide.");
        }
        // Lecture des lignes « clé=valeur » situées après l'en-tête
        Map<String, String> kv = new HashMap<>();
        for (String line : lines.subList(1, lines.size())) {
            int i = line.indexOf('=');
            if (i > 0) kv.put(line.substring(0, i), line.substring(i + 1));
        }
        try {
            List<Integer> moves = new ArrayList<>();
            String m = kv.getOrDefault("moves", "").trim();
            if (!m.isEmpty()) {
                for (String part : m.split(",")) moves.add(Integer.parseInt(part.trim()));
            }
            GameSave s = new GameSave(
                    Integer.parseInt(kv.get("mode")),
                    Integer.parseInt(kv.get("depth")),
                    Boolean.parseBoolean(kv.get("pruning")),
                    Integer.parseInt(kv.get("helps")),
                    moves);
            // Plausibilité minimale des paramètres
            if (s.mode < 1 || s.mode > 2 || s.depth < 1 || s.helps < 0) {
                throw new IOException("Valeurs invalides dans le fichier de sauvegarde.");
            }
            s.replay(); // vérifie que tous les coups sont légaux
            return s;
        } catch (RuntimeException e) {
            throw new IOException("Fichier de sauvegarde corrompu (" + e.getMessage() + ").");
        }
    }

    /**
     * Reconstruit la partie en rejouant les coups sur un nouvel objet {@link Game}.
     *
     * @return la partie dans l'état où elle a été sauvegardée
     * @throws RuntimeException si un coup est illégal (colonne invalide ou pleine, partie déjà finie)
     */
    public Game replay() {
        Game g = new Game();
        for (int col : moves) g.play(col);
        return g;
    }
}
