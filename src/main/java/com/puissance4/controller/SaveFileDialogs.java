package com.puissance4.controller;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;

/** Boîtes de dialogue de choix de fichier pour la sauvegarde / restauration. */
public final class SaveFileDialogs {

    private SaveFileDialogs() {
    }

    private static FileChooser chooser(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Puissance 4 (*.txt)", "*.txt"));
        File dir = new File(System.getProperty("user.dir"));
        if (dir.isDirectory()) fc.setInitialDirectory(dir);
        return fc;
    }

    /** Demande où sauvegarder ; retourne null si l'utilisateur annule. */
    public static Path chooseSaveFile(Window owner) {
        FileChooser fc = chooser("Sauvegarder la partie");
        fc.setInitialFileName("savegame.txt");
        File f = fc.showSaveDialog(owner);
        return f == null ? null : f.toPath();
    }

    /** Demande quel fichier ouvrir ; retourne null si l'utilisateur annule. */
    public static Path chooseOpenFile(Window owner) {
        File f = chooser("Charger une partie").showOpenDialog(owner);
        return f == null ? null : f.toPath();
    }
}
