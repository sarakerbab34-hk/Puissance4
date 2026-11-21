package com.puissance4.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires JUnit 5 pour la classe Game.
 * Couvre le cycle de jeu, la coordination des tours, le match nul et le refus après fin de partie.
 */
class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    @DisplayName("Le joueur ROUGE commence toujours la partie")
    void testRedPlayerStarts() {
        assertEquals(Player.RED, game.getCurrentPlayer());
        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertFalse(game.isGameOver());
        assertNull(game.getWinner());
    }

    @Test
    @DisplayName("Chaque joueur joue à tour de rôle (alternance des tours)")
    void testAlternatingTurns() {
        assertEquals(Player.RED, game.getCurrentPlayer());

        MoveResult res1 = game.play(0);
        assertEquals(Player.RED, res1.player());
        assertEquals(Player.YELLOW, game.getCurrentPlayer(), "Après le coup de Rouge, c'est au tour de Jaune.");

        MoveResult res2 = game.play(0);
        assertEquals(Player.YELLOW, res2.player());
        assertEquals(Player.RED, game.getCurrentPlayer(), "Après le coup de Jaune, c'est de nouveau à Rouge.");
    }

    @Test
    @DisplayName("Détection de victoire via Game et arrêt de la partie")
    void testWinDetectionInGame() {
        // Rouge joue colonnes 0, 1, 2, 3
        // Jaune joue colonnes 0, 1, 2
        game.play(0); // Rouge (5, 0)
        game.play(0); // Jaune (4, 0)
        game.play(1); // Rouge (5, 1)
        game.play(1); // Jaune (4, 1)
        game.play(2); // Rouge (5, 2)
        game.play(2); // Jaune (4, 2)
        MoveResult winMove = game.play(3); // Rouge (5, 3) -> Victoire horizontale !

        assertTrue(winMove.isWin());
        assertTrue(game.isGameOver());
        assertEquals(GameState.RED_WON, game.getState());
        assertEquals(Player.RED, game.getWinner());
    }

    @Test
    @DisplayName("14. Détection du match nul lorsque la grille est pleine sans vainqueur")
    void testDrawGame() {
        // Grille 7 colonnes, 6 lignes = 42 coups
        // Schéma spécifique garantissant aucune suite de 4 :
        // Pour chaque colonne, on joue 6 coups selon une séquence calculée
        // Colonnes 0, 1, 4, 5 : R, R, J, J, R, R
        // Colonnes 2, 3, 6    : J, J, R, R, J, J
        // On effectue les coups en respectant l'alternance stricte des tours de Game.play()
        int[] moves = {
                // Remplissage progressif sans alignement gagnant
                0, 1, 0, 1, 0, 1, // cols 0 et 1
                2, 3, 2, 3, 2, 3, // cols 2 et 3
                4, 5, 4, 5, 4, 5, // cols 4 et 5
                6, 0, 6, 0, 6, 0, // cols 6 et fin col 0
                1, 2, 1, 2, 1, 2, // fin cols 1 et 2
                3, 4, 3, 4, 3, 4, // fin cols 3 et 4
                5, 6, 5, 6, 5, 6  // fin cols 5 et 6
        };

        // Utilisons directement un Board configuré pour un match nul vérifié
        Board drawBoard = new Board();
        // Remplissage personnalisé sur le plateau pour simuler la situation
        Player[][] pattern = {
                {Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED},
                {Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED},
                {Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW},
                {Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW},
                {Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED},
                {Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW, Player.RED, Player.YELLOW}
        };

        // Remplir 41 cases dans un Game avec le drawBoard
        // On place 41 cases manuellement, puis on joue le dernier coup via game.play()
        Game customGame = new Game(drawBoard);
        for (int r = 5; r >= 0; r--) {
            for (int c = 0; c < 7; c++) {
                if (r == 0 && c == 6) {
                    continue; // On laisse la toute dernière case (ligne 0, col 6) vide
                }
                drawBoard.placePiece(c, pattern[r][c]);
            }
        }

        // Il reste 1 seule place dans la colonne 6
        assertFalse(customGame.isGameOver());
        assertFalse(drawBoard.isGridFull());

        // On joue le dernier coup
        MoveResult lastMove = customGame.play(6);

        assertTrue(lastMove.isDraw(), "Le coup remplissant la grille sans alignement doit être un match nul.");
        assertTrue(customGame.isGameOver());
        assertEquals(GameState.DRAW, customGame.getState());
        assertNull(customGame.getWinner());
    }

    @Test
    @DisplayName("15. Le refus des coups après la fin de la partie")
    void testMoveRefusalAfterGameOver() {
        // Provoquer une victoire rapide
        game.play(0); // R
        game.play(1); // J
        game.play(0); // R
        game.play(1); // J
        game.play(0); // R
        game.play(1); // J
        game.play(0); // R -> Victoire verticale (4 pions en colonne 0)

        assertTrue(game.isGameOver(), "La partie doit être terminée.");

        // Tentative de jouer un coup supplémentaire
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> game.play(2),
                "Tout coup joué après la fin de la partie doit lever une IllegalStateException.");

        assertTrue(exception.getMessage().contains("terminée"));
    }

    @Test
    @DisplayName("Le redémarrage remet à zéro l'état du jeu")
    void testRestartGame() {
        game.play(0);
        game.play(1);
        game.restart();

        assertEquals(Player.RED, game.getCurrentPlayer());
        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertFalse(game.isGameOver());
        assertEquals(0, game.getBoard().getPieceCount());
    }
}

