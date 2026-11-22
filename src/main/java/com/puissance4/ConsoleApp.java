package com.puissance4;

import com.puissance4.model.*;
import java.util.Scanner;

/** Terminal version of Puissance 4 (no JavaFX), reusing the model classes. */
public class ConsoleApp {
    public static void main(String[] args) {
        Game g = new Game();
        Board b = g.getBoard();
        Scanner s = new Scanner(System.in);
        while (!g.isGameOver()) {
            show(b);
            System.out.print(g.getCurrentPlayer().getDisplayName() + " - column (1-7, q=quit): ");
            String line = s.nextLine().trim();
            if (line.equalsIgnoreCase("q")) return;
            try {
                g.play(Integer.parseInt(line) - 1);
            } catch (NumberFormatException e) {
                System.out.println("!! Enter a number 1-7.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("!! " + e.getMessage());
            }
        }
        show(b);
        System.out.println("Result: " + g.getState());
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
