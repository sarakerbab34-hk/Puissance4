# Puissance 4 (Connect 4) — Minimax et Alpha-Beta

Mini-projet d'Intelligence Artificielle (module « Stratégies de recherche »), FSTT Tanger, cycle ingénieur LSI.
Application Java du jeu Puissance 4 qui met en œuvre les algorithmes **Minimax** et **Alpha-Beta**,
avec une interface graphique JavaFX et une version console.

**Groupe :** Khadija DALHI, Sara KERBAB, Christommy INDRAY — **Encadrant :** M'hamed AIT KBIR

---

## 1. Règles du jeu

Grille verticale de 7 colonnes et 6 lignes. Les joueurs Rouge et Jaune placent à tour de rôle un pion
dans une colonne non pleine ; le pion tombe jusqu'à la case libre la plus basse. **Rouge commence.**
Le premier joueur qui aligne quatre pions (horizontalement, verticalement ou en diagonale) gagne ;
si la grille est pleine sans alignement, la partie est nulle.

## 2. Fonctionnalités

| Exigence de l'énoncé | Réalisation |
| :--- | :--- |
| Aide limitée | Nombre d'aides fixé au menu (0 à 9) ; bouton « Aide (n) » en interface graphique, commande `h` en console |
| Complexité du jeu | Difficulté = profondeur de recherche : Facile (2), Moyen (4), Difficile (6) |
| Stratégie de la machine et de l'aide | Minimax ou Alpha-Beta, au choix dans le menu |
| Deux joueurs ou contre la machine | Deux modes ; contre la machine, l'humain joue Rouge et la machine Jaune |
| Refus des positionnements non autorisés | Colonne pleine, saisie invalide, partie terminée, aide épuisée, clic pendant une animation |
| Sauvegarde et restauration | Classe `GameSave` : fichier texte, restauration par rejeu des coups |
| Hiérarchie de classes du cours | `Position`, `Move`, `GameSearch` et leurs sous-classes `Connect4Position`, `Connect4Move`, `Connect4Search` |
| `alphaBetaHelper` remplacée | `GameSearch` utilise `max_Value(...)` et `min_Value(...)` |

## 3. Technologies

Java 21, JavaFX 21, Maven, JUnit 5 (5.10.2).

## 4. Prérequis

JDK 21 et Maven 3.8+ (`java -version` doit afficher 21). Sous Debian / Kali :
`sudo apt install git maven openjdk-21-jdk`.

## 5. Lancer le projet

```bash
git clone https://github.com/sarakerbab34-hk/Puissance4.git
cd Puissance4
mvn -q compile

# Interface graphique JavaFX (session graphique requise)
mvn javafx:run

# Version console
java -cp target/classes com.puissance4.ConsoleApp

# Tests unitaires du modèle
mvn clean test
```

Depuis un IDE, exécuter `com.puissance4.Launcher` (interface graphique) ou `com.puissance4.ConsoleApp`.

### Version console : commandes

Pendant la partie, saisir un numéro de colonne (**1 à 7**) ou une commande :

| Saisie | Action |
| :--- | :--- |
| `1` … `7` | Jouer dans cette colonne |
| `h` | Demander une aide (consomme une aide) |
| `s` | Sauvegarder la partie dans `savegame.txt` |
| `q` | Quitter |

Le menu de départ permet aussi de charger la partie enregistrée dans `savegame.txt`.
La machine affiche le nombre de nœuds explorés à chaque coup.

### Format du fichier de sauvegarde

```text
P4SAVE v1
mode=2
depth=4
pruning=true
helps=3
moves=3,3,2,4
```

`mode` : 1 = deux joueurs, 2 = contre la machine ; `pruning` : `true` = Alpha-Beta ;
`moves` : colonnes jouées (0 à 6) dans l'ordre. La restauration rejoue les coups et refuse tout
fichier absent, illisible ou contenant un coup illégal.

## 6. Architecture (MVC)

```text
src/main/java/com/puissance4/
├── MainApp.java, Launcher.java, ConsoleApp.java   # application JavaFX, lanceur, version console
├── model/        # règles du jeu, indépendant de toute interface
│   ├── Board, Game, Player, GameState, MoveResult
│   └── GameSave, GameSettings                      # sauvegarde et paramètres du menu
├── ai/           # stratégies de recherche (hiérarchie du cours)
│   ├── Position, Move                              # classes abstraites du cours
│   ├── GameSearch                                  # max_Value / min_Value, test d'arrêt, compteur de nœuds
│   ├── Connect4Position, Connect4Move, Connect4Search
│   └── AIPlayer, SearchResult                      # adaptateur plateau -> position, résultat de recherche
├── view/         # JavaFX : MenuView, GameView, BoardView
└── controller/   # GameController, SaveFileDialogs
```

- **`Board`** : grille 6×7 (ligne 0 en haut), gravité, refus des colonnes pleines ou invalides, détection des alignements.
- **`Game`** : joueur courant (Rouge commence), état de la partie, refus de tout coup après la fin.
- **`GameController`** : clics, machine dans un thread séparé (`Task`), aide, sauvegarde, chargement, verrou contre les clics pendant une animation.
- **`AIPlayer`** : convertit le plateau en position de recherche ; le joueur qui cherche un coup joue toujours le rôle de `PROGRAM`, ce qui permet de réutiliser le même code pour la machine et pour l'aide.

## 7. Algorithmes

- **Minimax / Alpha-Beta** : `GameSearch.max_Value` et `min_Value` sont mutuellement récursives. Le drapeau `pruning` active ou désactive les coupures α/β : avec `pruning = false`, le même code se comporte comme un Minimax pur.
- **Test d'arrêt** : victoire (`1 000 000 − profondeur`), défaite (`−1 000 000 + profondeur`), nul (0), profondeur maximale (évaluation heuristique). Le terme de profondeur fait préférer une victoire rapide.
- **Évaluation** : bonus pour la colonne centrale (+3 / −3 par pion) et analyse des 69 fenêtres de 4 cases (3 pions + 1 vide : +5, 2 pions + 2 vides : +2 ; menaces adverses : −4 et −1).
- **Ordre d'exploration** : colonnes 4, 3, 5, 2, 6, 1, 7 (centre d'abord) pour améliorer l'élagage.

## 8. Tests

20 tests unitaires JUnit 5 pour le modèle (`BoardTest` : 14, `GameTest` : 6), exécutés par `mvn clean test`.
Les classes de recherche (`GameSearch`, `Connect4Search`, `AIPlayer`) et `GameSave` n'ont pas encore de test
unitaire dédié ; elles ont été validées par des parties jouées et par la comparaison Minimax / Alpha-Beta
décrite dans le compte rendu.
