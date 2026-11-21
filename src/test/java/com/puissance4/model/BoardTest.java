package com.puissance4.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires JUnit 5 pour la classe Board.
 * Couvre les spécifications du Back-End pour la gestion de la grille.
 */
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    @DisplayName("1. La création d'une grille vide comporte 6 lignes et 7 colonnes toutes vides")
    void testEmptyGridCreation() {
        assertFalse(board.isGridFull(), "Une nouvelle grille ne doit pas être pleine.");
        assertEquals(0, board.getPieceCount(), "Une nouvelle grille doit contenir 0 pion.");

        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLUMNS; c++) {
                assertNull(board.getCell(r, c), "La case (" + r + ", " + c + ") doit être vide.");
                assertTrue(board.isCellEmpty(r, c));
            }
        }
    }

    @Test
    @DisplayName("2. Le placement d'un pion rouge se fait correctement")
    void testPlaceRedPiece() {
        int row = board.placePiece(0, Player.RED);
        assertEquals(Board.ROWS - 1, row, "Le premier pion doit tomber tout en bas (ligne 5).");
        assertEquals(Player.RED, board.getCell(row, 0), "La case doit contenir le joueur ROUGE.");
    }

    @Test
    @DisplayName("3. Le placement d'un pion jaune se fait correctement")
    void testPlaceYellowPiece() {
        int row = board.placePiece(3, Player.YELLOW);
        assertEquals(Board.ROWS - 1, row, "Le premier pion doit tomber tout en bas (ligne 5).");
        assertEquals(Player.YELLOW, board.getCell(row, 3), "La case doit contenir le joueur JAUNE.");
    }

    @Test
    @DisplayName("4. La gravité : les pions se placent du bas vers le haut")
    void testGravityPlacement() {
        int col = 2;
        int row1 = board.placePiece(col, Player.RED);
        int row2 = board.placePiece(col, Player.YELLOW);
        int row3 = board.placePiece(col, Player.RED);

        assertEquals(5, row1, "1er pion en bas (ligne 5)");
        assertEquals(4, row2, "2ème pion au-dessus (ligne 4)");
        assertEquals(3, row3, "3ème pion encore au-dessus (ligne 3)");

        assertEquals(Player.RED, board.getCell(5, col));
        assertEquals(Player.YELLOW, board.getCell(4, col));
        assertEquals(Player.RED, board.getCell(3, col));
    }

    @Test
    @DisplayName("5. Le placement de plusieurs pions dans une même colonne")
    void testMultiplePiecesInSameColumn() {
        int col = 4;
        for (int i = 0; i < Board.ROWS; i++) {
            Player p = (i % 2 == 0) ? Player.RED : Player.YELLOW;
            int placedRow = board.placePiece(col, p);
            assertEquals(Board.ROWS - 1 - i, placedRow);
        }

        assertTrue(board.isColumnFull(col), "La colonne contenant 6 pions doit être déclarée pleine.");
    }

    @Test
    @DisplayName("6. Le refus d'une colonne pleine par exception")
    void testFullColumnRefusal() {
        int col = 1;
        for (int i = 0; i < Board.ROWS; i++) {
            board.placePiece(col, Player.RED);
        }

        assertTrue(board.isColumnFull(col));

        // Tenter de placer un 7ème pion dans la même colonne
        assertThrows(IllegalStateException.class, () -> board.placePiece(col, Player.YELLOW),
                "Placer un pion dans une colonne pleine doit lever une IllegalStateException.");
    }

    @Test
    @DisplayName("7. Le refus d'un indice de colonne invalide")
    void testInvalidColumnIndexRefusal() {
        assertThrows(IllegalArgumentException.class, () -> board.placePiece(-1, Player.RED),
                "Un indice négatif (-1) doit lever une IllegalArgumentException.");
        assertThrows(IllegalArgumentException.class, () -> board.placePiece(7, Player.RED),
                "Un indice égal à 7 doit lever une IllegalArgumentException.");
        assertThrows(IllegalArgumentException.class, () -> board.placePiece(100, Player.RED),
                "Un indice trop grand (100) doit lever une IllegalArgumentException.");
    }

    @Test
    @DisplayName("8. La victoire horizontale (4 pions consécutifs sur la même ligne)")
    void testHorizontalWin() {
        // Aligne 4 pions rouges sur la ligne du bas (ligne 5) aux colonnes 0, 1, 2, 3
        board.placePiece(0, Player.RED);
        board.placePiece(1, Player.RED);
        board.placePiece(2, Player.RED);
        int lastRow = board.placePiece(3, Player.RED);

        assertTrue(board.checkWin(lastRow, 3), "Un alignement horizontal de 4 pions rouges doit être victorieux.");
        assertTrue(board.hasWon(Player.RED));
        assertFalse(board.hasWon(Player.YELLOW));
        assertEquals(4, board.getLastWinningPositions().size());
    }

    @Test
    @DisplayName("9. La victoire verticale (4 pions empilés dans la même colonne)")
    void testVerticalWin() {
        int col = 5;
        board.placePiece(col, Player.YELLOW);
        board.placePiece(col, Player.YELLOW);
        board.placePiece(col, Player.YELLOW);
        int lastRow = board.placePiece(col, Player.YELLOW);

        assertTrue(board.checkWin(lastRow, col), "Un empilement vertical de 4 pions jaunes doit être victorieux.");
        assertTrue(board.hasWon(Player.YELLOW));
        assertFalse(board.hasWon(Player.RED));
        assertEquals(4, board.getLastWinningPositions().size());
    }

    @Test
    @DisplayName("10. La victoire diagonale dans le premier sens (montante /)")
    void testDiagonalAscendingWin() {
        // On construit une diagonale montante pour ROUGE :
        // (row 5, col 0)
        // (row 4, col 1)
        // (row 3, col 2)
        // (row 2, col 3)

        // Col 0 : 1 pion R
        board.placePiece(0, Player.RED); // (5, 0)

        // Col 1 : 1 pion J, puis 1 pion R
        board.placePiece(1, Player.YELLOW); // (5, 1)
        board.placePiece(1, Player.RED);    // (4, 1)

        // Col 2 : 2 pions J, puis 1 pion R
        board.placePiece(2, Player.YELLOW); // (5, 2)
        board.placePiece(2, Player.YELLOW); // (4, 2)
        board.placePiece(2, Player.RED);    // (3, 2)

        // Col 3 : 3 pions J, puis 1 pion R
        board.placePiece(3, Player.YELLOW); // (5, 3)
        board.placePiece(3, Player.YELLOW); // (4, 3)
        board.placePiece(3, Player.YELLOW); // (3, 3)
        int lastRow = board.placePiece(3, Player.RED); // (2, 3)

        assertEquals(2, lastRow);
        assertTrue(board.checkWin(lastRow, 3), "La diagonale montante / de 4 pions rouges doit être victorieuse.");
        assertTrue(board.hasWon(Player.RED));
    }

    @Test
    @DisplayName("11. La victoire diagonale dans le deuxième sens (descendante \\)")
    void testDiagonalDescendingWin() {
        // Diagonale descendante pour JAUNE :
        // (row 2, col 0)
        // (row 3, col 1)
        // (row 4, col 2)
        // (row 5, col 3)

        // Col 0 : 3 pions R puis 1 pion J
        board.placePiece(0, Player.RED);    // (5, 0)
        board.placePiece(0, Player.RED);    // (4, 0)
        board.placePiece(0, Player.RED);    // (3, 0)
        board.placePiece(0, Player.YELLOW); // (2, 0)

        // Col 1 : 2 pions R puis 1 pion J
        board.placePiece(1, Player.RED);    // (5, 1)
        board.placePiece(1, Player.RED);    // (4, 1)
        board.placePiece(1, Player.YELLOW); // (3, 1)

        // Col 2 : 1 pion R puis 1 pion J
        board.placePiece(2, Player.RED);    // (5, 2)
        board.placePiece(2, Player.YELLOW); // (4, 2)

        // Col 3 : 1 pion J
        int lastRow = board.placePiece(3, Player.YELLOW); // (5, 3)

        assertEquals(5, lastRow);
        assertTrue(board.checkWin(lastRow, 3), "La diagonale descendante \\ de 4 pions jaunes doit être victorieuse.");
        assertTrue(board.hasWon(Player.YELLOW));
    }

    @Test
    @DisplayName("12. L'absence de victoire avec seulement trois pions alignés")
    void testNoWinWithThreePieces() {
        board.placePiece(0, Player.RED);
        board.placePiece(1, Player.RED);
        int lastRow = board.placePiece(2, Player.RED);

        assertFalse(board.checkWin(lastRow, 2), "3 pions alignés ne doivent pas constituer une victoire.");
        assertFalse(board.hasWon(Player.RED));
        assertFalse(board.hasWon(Player.YELLOW));
    }

    @Test
    @DisplayName("13. La détection d'une grille pleine")
    void testFullGridDetection() {
        // Remplissage complet de la grille (42 pions)
        // Modèle de remplissage évitant les alignements de 4 pour tester isGridFull
        // Pattern alterné :
        // Colonnes 0, 1, 2 : R R J J R R
        // Colonnes 3, 4, 5 : J J R R J J
        // Colonne 6        : R R J J R R
        Player[] colPatternA = {Player.RED, Player.RED, Player.YELLOW, Player.YELLOW, Player.RED, Player.RED};
        Player[] colPatternB = {Player.YELLOW, Player.YELLOW, Player.RED, Player.RED, Player.YELLOW, Player.YELLOW};

        for (int c = 0; c < Board.COLUMNS; c++) {
            Player[] pattern = (c < 3 || c == 6) ? colPatternA : colPatternB;
            for (Player p : pattern) {
                board.placePiece(c, p);
            }
        }

        assertTrue(board.isGridFull(), "La grille entièrement remplie doit être déclarée pleine.");
        assertEquals(Board.ROWS * Board.COLUMNS, board.getPieceCount());
    }

    @Test
    @DisplayName("Test de réinitialisation de la grille")
    void testResetBoard() {
        board.placePiece(0, Player.RED);
        board.placePiece(1, Player.YELLOW);
        assertEquals(2, board.getPieceCount());

        board.reset();

        assertEquals(0, board.getPieceCount());
        assertTrue(board.isCellEmpty(5, 0));
        assertTrue(board.isCellEmpty(5, 1));
        assertFalse(board.isGridFull());
    }
}

