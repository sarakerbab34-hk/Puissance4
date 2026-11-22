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
 * Sauvegarde / restauration d'une partie.
 * On enregistre les paramètres (mode, difficulté, stratégie, aides restantes)
 * et la liste des colonnes jouées ; la restauration rejoue les coups sur un Game.
 */
public class GameSave {
    private static final String HEADER = "P4SAVE v1";

    public final int mode;          // 1 = 2 joueurs, 2 = contre la machine
    public final int depth;         // difficulté (profondeur de recherche)
    public final boolean pruning;   // true = Alpha-Beta, false = Minimax
    public final int helps;         // aides restantes
    public final List<Integer> moves; // colonnes jouées (0-6), dans l'ordre

    public GameSave(int mode, int depth, boolean pruning, int helps, List<Integer> moves) {
        this.mode = mode;
        this.depth = depth;
        this.pruning = pruning;
        this.helps = helps;
        this.moves = new ArrayList<>(moves);
    }

    /** Écrit la sauvegarde dans un fichier texte. */
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

    /** Lit une sauvegarde ; lève IOException si le fichier est absent ou invalide. */
    public static GameSave load(Path file) throws IOException {
        if (!Files.exists(file)) throw new IOException("No saved game found.");
        List<String> lines = Files.readAllLines(file);
        if (lines.isEmpty() || !lines.get(0).equals(HEADER)) {
            throw new IOException("Not a valid save file.");
        }
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
            if (s.mode < 1 || s.mode > 2 || s.depth < 1 || s.helps < 0) {
                throw new IOException("Invalid values in save file.");
            }
            s.replay(); // vérifie que tous les coups sont légaux
            return s;
        } catch (RuntimeException e) {
            throw new IOException("Corrupted save file (" + e.getMessage() + ").");
        }
    }

    /** Reconstruit la partie en rejouant les coups. */
    public Game replay() {
        Game g = new Game();
        for (int col : moves) g.play(col);
        return g;
    }
}
