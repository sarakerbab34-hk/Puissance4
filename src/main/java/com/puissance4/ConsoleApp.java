package com.puissance4;

import com.puissance4.ai.AIPlayer;
import com.puissance4.model.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Version terminal de Puissance 4.
 *
 * <p>Elle offre les mêmes fonctionnalités que l'interface graphique : jeu à deux ou contre la
 * machine, choix de la difficulté (profondeur de recherche) et de la stratégie (Minimax ou
 * Alpha-Beta), aide limitée, sauvegarde et restauration d'une partie.</p>
 *
 * <p>Pendant la partie, le joueur saisit un numéro de colonne (1 à 7) ou une commande :
 * {@code h} (aide), {@code s} (sauvegarder), {@code q} (quitter).</p>
 *
 * <p>Lancement : {@code java -cp target/classes com.puissance4.ConsoleApp}</p>
 */
public class ConsoleApp {

    /** Fichier de sauvegarde, créé dans le dossier de lancement. */
    private static final Path SAVE_FILE = Paths.get("savegame.txt");

    /**
     * Point d'entrée : affiche le menu, puis déroule la partie jusqu'à sa fin ou jusqu'à ce que
     * le joueur quitte.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("=== PUISSANCE 4 ===");

        // Paramètres par défaut, remplacés par les choix du menu ou par une sauvegarde
        int mode = 1, depth = 4, helps = 0;
        boolean pruning = true;
        Game g = new Game();
        List<Integer> moves = new ArrayList<>(); // colonnes jouées (0-6), pour la sauvegarde

        int choice = ask(s, "1) Deux joueurs  2) Contre la machine (machine = Jaune)  3) Charger une partie", 1, 3);
        if (choice == 3) {
            // Restauration : on rejoue les coups sauvegardés sur un nouvel objet Game
            try {
                GameSave sv = GameSave.load(SAVE_FILE);
                g = sv.replay();
                mode = sv.mode;
                depth = sv.depth;
                pruning = sv.pruning;
                helps = sv.helps;
                moves.addAll(sv.moves);
                System.out.println("Partie chargée (" + moves.size() + " coups joués).");
            } catch (IOException | RuntimeException e) {
                System.out.println("!! Impossible de charger " + SAVE_FILE + " : " + e.getMessage());
                return;
            }
        } else {
            mode = choice;
            // Difficulté = profondeur de recherche : 2 (facile), 4 (moyen), 6 (difficile)
            depth = new int[]{0, 2, 4, 6}[ask(s, "Difficulté : 1) Facile  2) Moyen  3) Difficile", 1, 3)];
            pruning = ask(s, "Stratégie (machine + aide) : 1) Minimax  2) Alpha-Beta", 1, 2) == 2;
            helps = ask(s, "Nombre d'aides autorisées (0-9)", 0, 9);
        }

        Board b = g.getBoard();
        AIPlayer ai = new AIPlayer(depth, pruning);

        while (!g.isGameOver()) {
            show(b);
            Player cur = g.getCurrentPlayer();

            // Tour de la machine (elle joue toujours les Jaunes)
            if (mode == 2 && cur == Player.YELLOW) {
                int col = ai.bestColumn(b, cur);
                System.out.println("La machine joue la colonne " + (col + 1)
                        + " (nœuds explorés : " + ai.getLastNodes() + ")");
                g.play(col);
                moves.add(col);
                continue;
            }

            // Tour d'un joueur humain : saisie d'une colonne ou d'une commande
            System.out.print(cur.getDisplayName() + " - colonne (1-7, h=aide [" + helps
                    + " restante(s)], s=sauvegarder, q=quitter) : ");
            String line = s.nextLine().trim();

            if (line.equalsIgnoreCase("q")) return;

            if (line.equalsIgnoreCase("s")) {
                try {
                    new GameSave(mode, depth, pruning, helps, moves).save(SAVE_FILE);
                    System.out.println("Partie sauvegardée dans " + SAVE_FILE.toAbsolutePath());
                } catch (IOException e) {
                    System.out.println("!! Sauvegarde impossible : " + e.getMessage());
                }
                continue;
            }

            if (line.equalsIgnoreCase("h")) {
                if (helps == 0) {
                    System.out.println("!! Vous n'avez plus d'aide disponible.");
                } else {
                    helps--;
                    int col = ai.bestColumn(b, cur);
                    System.out.println("Suggestion : colonne " + (col + 1)
                            + " (nœuds explorés : " + ai.getLastNodes() + ")");
                }
                continue;
            }

            // Coup normal : les colonnes sont saisies de 1 à 7, le modèle les numérote de 0 à 6
            try {
                int col = Integer.parseInt(line) - 1;
                if (col < 0 || col >= Board.COLUMNS) {
                    System.out.println("!! Colonne invalide : " + (col + 1)
                            + ". Choisissez un nombre entre 1 et " + Board.COLUMNS + ".");
                } else if (b.isColumnFull(col)) {
                    System.out.println("!! La colonne " + (col + 1) + " est pleine. Choisissez-en une autre.");
                } else {
                    g.play(col);
                    moves.add(col);
                }
            } catch (NumberFormatException e) {
                System.out.println("!! Saisissez un nombre de 1 à 7, ou h, s, q.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("!! " + e.getMessage());
            }
        }

        show(b);
        System.out.println(g.getWinner() == null
                ? "Résultat : MATCH NUL"
                : "Vainqueur : " + g.getWinner().getDisplayName());
    }

    /**
     * Pose une question à choix numérique et répète la saisie jusqu'à obtenir une valeur valide.
     *
     * @param s        lecteur de l'entrée standard
     * @param question texte affiché devant la saisie
     * @param min      plus petite valeur acceptée
     * @param max      plus grande valeur acceptée
     * @return la valeur saisie, comprise entre {@code min} et {@code max}
     */
    private static int ask(Scanner s, String question, int min, int max) {
        while (true) {
            System.out.print(question + " : ");
            try {
                int v = Integer.parseInt(s.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {
                // valeur non numérique : on affiche le message ci-dessous et on redemande
            }
            System.out.println("!! Saisissez un nombre entre " + min + " et " + max + ".");
        }
    }

    /**
     * Affiche la grille dans le terminal (ligne du haut en premier, R = Rouge, Y = Jaune).
     *
     * @param b plateau à afficher
     */
    private static void show(Board b) {
        System.out.println("\n 1 2 3 4 5 6 7");
        for (int r = 0; r < Board.ROWS; r++) {
            StringBuilder sb = new StringBuilder("|");
            for (int c = 0; c < Board.COLUMNS; c++) {
                Player p = b.getCell(r, c);
                sb.append(p == null ? "." : p == Player.RED ? "R" : "Y").append("|");
            }
            System.out.println(sb);
        }
    }
}
