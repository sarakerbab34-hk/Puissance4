package com.puissance4;

import com.puissance4.ai.Connect4Position;
import com.puissance4.ai.Connect4Search;
import com.puissance4.ai.GameSearch;
import com.puissance4.model.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Version terminal de Puissance 4 : 2 joueurs ou contre la machine, aide, sauvegarde. */
public class ConsoleApp {

    private static final Path SAVE_FILE = Paths.get("savegame.txt");

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.println("=== PUISSANCE 4 ===");
        int mode = 1, depth = 4, helps = 0;
        boolean pruning = true;
        Game g = new Game();
        List<Integer> moves = new ArrayList<>();

        int choice = ask(s, "1) 2 players  2) vs machine (machine = Jaune)  3) Load saved game", 1, 3);
        if (choice == 3) {
            try {
                GameSave sv = GameSave.load(SAVE_FILE);
                g = sv.replay();
                mode = sv.mode;
                depth = sv.depth;
                pruning = sv.pruning;
                helps = sv.helps;
                moves.addAll(sv.moves);
                System.out.println("Game loaded (" + moves.size() + " moves played).");
            } catch (IOException | RuntimeException e) {
                System.out.println("!! Cannot load " + SAVE_FILE + ": " + e.getMessage());
                return;
            }
        } else {
            mode = choice;
            depth = new int[]{0, 2, 4, 6}[ask(s, "Difficulty: 1) Easy  2) Medium  3) Hard", 1, 3)];
            pruning = ask(s, "Strategy (machine + help): 1) Minimax  2) Alpha-Beta", 1, 2) == 2;
            helps = ask(s, "Number of helps allowed (0-9)", 0, 9);
        }

        Board b = g.getBoard();
        Connect4Search search = new Connect4Search(depth, pruning);

        while (!g.isGameOver()) {
            show(b);
            Player cur = g.getCurrentPlayer();
            if (mode == 2 && cur == Player.YELLOW) {
                int col = pick(search, b, cur);
                System.out.println("Machine plays column " + (col + 1)
                        + " (nodes explored: " + search.getNodesVisited() + ")");
                g.play(col);
                moves.add(col);
                continue;
            }
            System.out.print(cur.getDisplayName() + " - column (1-7, h=help [" + helps
                    + " left], s=save, q=quit): ");
            String line = s.nextLine().trim();
            if (line.equalsIgnoreCase("q")) return;
            if (line.equalsIgnoreCase("s")) {
                try {
                    new GameSave(mode, depth, pruning, helps, moves).save(SAVE_FILE);
                    System.out.println("Game saved to " + SAVE_FILE.toAbsolutePath());
                } catch (IOException e) {
                    System.out.println("!! Cannot save: " + e.getMessage());
                }
                continue;
            }
            if (line.equalsIgnoreCase("h")) {
                if (helps == 0) {
                    System.out.println("!! No help left.");
                } else {
                    helps--;
                    int col = pick(search, b, cur);
                    System.out.println("Suggestion: column " + (col + 1)
                            + " (nodes explored: " + search.getNodesVisited() + ")");
                }
                continue;
            }
            try {
                int col = Integer.parseInt(line) - 1;
                g.play(col);
                moves.add(col);
            } catch (NumberFormatException e) {
                System.out.println("!! Enter a number 1-7, h, s or q.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("!! " + e.getMessage());
            }
        }
        show(b);
        System.out.println(g.getWinner() == null ? "Result: DRAW" : "Winner: " + g.getWinner().getDisplayName());
    }

    /** Meilleure colonne pour "cur" : cur joue le rôle de PROGRAM dans la recherche. */
    private static int pick(Connect4Search search, Board b, Player cur) {
        Connect4Position pos = new Connect4Position();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLUMNS; c++) {
                Player p = b.getCell(r, c);
                if (p != null) {
                    pos.setCell(r, c, Connect4Position.piece(p == cur ? GameSearch.PROGRAM : GameSearch.HUMAN));
                }
            }
        }
        return search.bestColumn(pos, GameSearch.PROGRAM);
    }

    private static int ask(Scanner s, String question, int min, int max) {
        while (true) {
            System.out.print(question + " : ");
            try {
                int v = Integer.parseInt(s.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("!! Enter a number between " + min + " and " + max + ".");
        }
    }

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
