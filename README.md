# Puissance 4 — Mini-Projet Universitaire d'Intelligence Artificielle

Ce projet est une application Java de jeu **Puissance 4** conçue dans le cadre d'un mini-projet universitaire d'Intelligence Artificielle.
Ce dépôt contient le développement complet de la composante **Interface de jeu (Back-End métier & Front-End JavaFX)**, structurée selon le patron d'architecture **MVC (Modèle - Vue - Contrôleur)**.

---

## 1. Présentation et Objectif du Jeu

Le Puissance 4 est un jeu de stratégie à deux joueurs se déroulant sur une grille verticale :
* **Grille** : 7 colonnes et 6 lignes (42 cases au total).
* **Joueurs** : Joueur **Rouge** et Joueur **Jaune**.
* **Départ** : Le joueur Rouge commence toujours la partie.
* **Gravité** : Lorsqu'un joueur sélectionne une colonne, son pion glisse automatiquement jusqu'à la case vide la plus basse disponible.
* **Victoire** : Le premier joueur qui parvient à aligner **4 pions** de sa couleur horizontalement, verticalement ou diagonalement (dans les deux sens) remporte la partie.
* **Match nul** : Si toutes les cases de la grille sont remplies sans qu'aucun alignement de 4 ne soit formé, la partie se termine par un match nul.
* **Fin de partie** : Dès la fin de la partie (victoire ou match nul), plus aucun nouveau coup n'est accepté.

---

## 2. Technologies Utilisées

* **Langage** : Java 21 (LTS)
* **Interface Graphique** : JavaFX 21 (`javafx-controls`, `javafx-fxml`)
* **Gestionnaire de dépendances et build** : Apache Maven 3.10+
* **Framework de tests unitaires** : JUnit 5 (Jupiter 5.10.2)
* **Architecture** : MVC pur (Modèle indépendant de tout composant graphique)
* **Styles visuels** : CSS JavaFX personnalisé avec dégradés radiaux 3D et effets d'ombrage

---

## 3. Prérequis

* **JDK 21** ou version supérieure installée et accessible via la variable d'environnement `JAVA_HOME`.
* **Maven 3.8+** installé et accessible dans le `PATH`.

---

## 4. Structure du Projet

```text
Puissance4/
├── pom.xml                                      # Configuration Maven (Java 21, JavaFX, JUnit 5)
├── README.md                                    # Documentation complète du projet
├── .gitignore                                   # Fichiers exclus du versionnement Git
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/puissance4/
    │   │       ├── Launcher.java                # Point d'entrée pour lancement simplifié en IDE
    │   │       ├── MainApp.java                 # Classe Application JavaFX (initialisation MVC)
    │   │       ├── model/                       # BACK-END MÉTIER (Indépendant de l'UI)
    │   │       │   ├── Player.java              # Enumération des joueurs (RED, YELLOW)
    │   │       │   ├── GameState.java           # États du jeu (EN_COURS, VICTOIRE, NUL)
    │   │       │   ├── MoveResult.java          # Résultat immuable d'un coup joué
    │   │       │   ├── Board.java               # Moteur de la grille 6x7, gravité et victoires
    │   │       │   └── Game.java                # Coordinateur des tours, règles et états
    │   │       ├── view/                        # FRONT-END JAVAFX
    │   │       │   ├── BoardView.java           # Plateau visuel bleu, trous circulaires, animations
    │   │       │   └── GameView.java            # Vue globale (en-tête, plateau, messages de statut)
    │   │       └── controller/                  # CONTRÔLEUR
    │   │           └── GameController.java      # Écouteurs de clics, liaison Model-View, verrou d'animation
    │   └── resources/
    │       └── com/puissance4/
    │           └── style.css                    # Styles CSS pour les boutons, badges et conteneurs
    └── test/
        └── java/
            └── com/puissance4/model/            # TESTS UNITAIRES JUNIT 5
                ├── BoardTest.java               # 14 tests pour Board (grille, gravité, alignements)
                └── GameTest.java                # 6 tests pour Game (tours, victoire, match nul, refus)
```

---

## 5. Description des Classes Développées

### A. Modèle (Back-End)
* **`Player`** : Énumération (`RED`, `YELLOW`) définissant les noms d'affichage, couleurs hexadécimales et méthode d'alternance (`getOpponent()`).
* **`GameState`** : Énumération représentant l'état de la partie (`IN_PROGRESS`, `RED_WON`, `YELLOW_WON`, `DRAW`).
* **`MoveResult`** : Record immuable retourné après chaque coup (`row`, `col`, `player`, `isWin`, `isDraw`, `winningPositions`).
* **`Board`** :
  * Modélisation de la grille 6 × 7 avec indices : ligne 0 = sommet, ligne 5 = fond.
  * Placement des pions selon la gravité physique (`placePiece(col, player)`).
  * Vérification des indices valides et colonnes pleines (`isValidColumn`, `isColumnFull`).
  * Détection universelle des 4 alignements gagnants (horizontal, vertical, diagonale ascendante `/`, diagonale descendante `\`).
  * Sauvegarde des coordonnées exactes des pions gagnants pour le surlignage graphique.
  * Réinitialisation complète (`reset()`).
* **`Game`** :
  * Coordonne le cycle de jeu : départ avec le joueur Rouge, alternance stricte des tours.
  * Déclenche la fin de partie lors d'une victoire ou d'un match nul.
  * Refuse tout coup supplémentaire une fois la partie terminée (`IllegalStateException`).
  * Permet le redémarrage propre (`restart()`).

### B. Vue (Front-End)
* **`BoardView`** :
  * Plateau bleu roi moderne avec coins arrondis et ombre portée.
  * Cases circulaires avec simulation de profondeur (ombres intérieures).
  * Pions avec dégradés radiaux 3D et reflets lumineux réalistes.
  * Détection de survol avec pion fantôme prévisualisant la colonne visée.
  * Animation fluide de chute (`TranslateTransition`) avec accélération gravitationnelle (`Interpolator.EASE_IN`).
  * Animation pulsante dorée sur les 4 pions victorieux en fin de partie.
* **`GameView`** :
  * En-tête : Titre, badge du joueur courant (pastille dynamique), boutons d'action (*Nouvelle partie*, *Recommencer*).
  * Pied de page : Zone de notifications dynamiques (messages informatifs, alertes d'erreurs, célébration de victoire).

### C. Contrôleur
* **`GameController`** :
  * Intercepte les clics sur les colonnes.
  * Vérifie l'état du jeu et rejette les coups invalides ou prématurés.
  * Intègre un **verrou d'animation** (`isAnimationRunning`) évitant les clics intempestifs ou la désynchronisation pendant la chute d'un pion.
  * Met à jour la vue de manière fluide en fin d'animation.

---

## 6. Instructions pour Lancer le Projet

### Méthode 1 : En ligne de commande avec Maven (recommandé)
Pour lancer directement l'interface de jeu :
```bash
mvn javafx:run
```

### Méthode 2 : Depuis votre IDE (Antigravity, IntelliJ, Eclipse, VS Code)
Exécutez la méthode `main` de la classe :
`com.puissance4.Launcher` ou `com.puissance4.MainApp`.

---

## 7. Commandes Maven Utiles

| Commande | Action |
| :--- | :--- |
| `mvn clean compile` | Nettoie et compile l'ensemble des sources Java |
| `mvn clean test` | Exécute la suite complète des tests unitaires JUnit 5 |
| `mvn javafx:run` | Lance l'application graphique JavaFX |
| `mvn package` | Génère l'archive JAR du projet |

---

## 8. Résumé des Tests Unitaires (JUnit 5)

Une suite de **20 tests automatisés** valide scrupuleusement l'ensemble des règles du cahier des charges :

1. **Création d'une grille vide** (dimensions 6x7, 0 pion, toutes cases vides).
2. **Placement d'un pion rouge** au fond de la grille.
3. **Placement d'un pion jaune** au fond de la grille.
4. **Gravité naturelle** : les pions s'empilent du bas (ligne 5) vers le haut (ligne 0).
5. **Placement de plusieurs pions** dans une même colonne.
6. **Refus d'une colonne pleine** (levée d'exception `IllegalStateException`).
7. **Refus des indices de colonne invalides** (`-1`, `7`, `100` via `IllegalArgumentException`).
8. **Détection de victoire horizontale** (4 pions consécutifs).
9. **Détection de victoire verticale** (4 pions empilés).
10. **Détection de victoire diagonale montante** (`/`).
11. **Détection de victoire diagonale descendante** (`\`).
12. **Absence de victoire** avec seulement 3 pions alignés.
13. **Détection d'une grille pleine** (42 pions insérés sans victoire).
14. **Détection du match nul** (`GameState.DRAW`).
15. **Refus de jouer après la fin de la partie** (exception après victoire ou match nul).
16. **Redémarrage et réinitialisation de la partie** (`restart()`).
17. **Alternance stricte des tours** (Rouge -> Jaune -> Rouge).

> Résultat : **20 tests exécutés, 0 échec, 0 erreur.**

