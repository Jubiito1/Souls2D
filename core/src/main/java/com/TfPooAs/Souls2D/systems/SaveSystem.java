package com.TfPooAs.Souls2D.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sistema simple de guardado: almacena la última hoguera activada (posición en píxeles)
 * y un ranking (leaderboard) de las 10 mejores cantidades de almas al finalizar el juego.
 */
public class SaveSystem {
    private static final String SAVE_FILE = "savegame.json";

    public static class LeaderboardEntry {
        public String name; // opcional
        public int souls;
        public long time; // epoch millis para desempate
    }

    public static class SaveData {
        public Float lastBonfireX; // píxeles
        public Float lastBonfireY; // píxeles
        public Boolean iudexDefeated; // estado del jefe
        public List<LeaderboardEntry> leaderboard; // top runs
    }

    private static FileHandle getFile() {
        return Gdx.files.local(SAVE_FILE);
    }

    private static void save(SaveData data) {
        try {
            Json json = new Json();
            getFile().writeString(json.prettyPrint(data), false, "UTF-8");
        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Failed to save: " + e.getMessage(), e);
        }
    }

    public static void saveLastBonfire(float x, float y) {
        try {
            SaveData data = load();
            if (data == null) data = new SaveData();
            data.lastBonfireX = x;
            data.lastBonfireY = y;
            save(data);
            Gdx.app.log("SaveSystem", "Saved last bonfire at (" + x + ", " + y + ")");
        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Failed to save: " + e.getMessage(), e);
        }
    }

    public static boolean isIudexDefeated() {
        SaveData d = load();
        return d != null && d.iudexDefeated != null && d.iudexDefeated;
    }

    public static void setIudexDefeated(boolean defeated) {
        try {
            SaveData data = load();
            if (data == null) data = new SaveData();
            data.iudexDefeated = defeated;
            save(data);
            Gdx.app.log("SaveSystem", "Set Iudex defeated = " + defeated);
        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Failed to set boss flag: " + e.getMessage(), e);
        }
    }

    public static boolean hasLastBonfire() {
        SaveData d = load();
        return d != null && d.lastBonfireX != null && d.lastBonfireY != null;
    }

    public static SaveData load() {
        try {
            FileHandle fh = getFile();
            if (fh == null || !fh.exists()) return null;
            String txt = fh.readString("UTF-8");
            if (txt == null || txt.isEmpty()) return null;
            Json json = new Json();
            return json.fromJson(SaveData.class, txt);
        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Failed to load: " + e.getMessage(), e);
            return null;
        }
    }

    public static float[] loadLastBonfire() {
        SaveData d = load();
        if (d == null || d.lastBonfireX == null || d.lastBonfireY == null) return null;
        return new float[] { d.lastBonfireX, d.lastBonfireY };
    }

    /**
     * Inserta una puntuación de almas en el ranking y mantiene solo las 10 mejores.
     */
    public static void addScoreToLeaderboard(int souls, String name) {
        try {
            SaveData d = load();
            if (d == null) d = new SaveData();
            if (d.leaderboard == null) d.leaderboard = new ArrayList<LeaderboardEntry>();
            LeaderboardEntry e = new LeaderboardEntry();
            e.souls = Math.max(0, souls);
            e.name = (name == null || name.isEmpty()) ? "Anónimo" : name;
            e.time = System.currentTimeMillis();
            d.leaderboard.add(e);
            // ordenar por souls desc, luego time asc
            Collections.sort(d.leaderboard, new Comparator<LeaderboardEntry>() {
                @Override public int compare(LeaderboardEntry a, LeaderboardEntry b) {
                    int s = Integer.compare(b.souls, a.souls);
                    if (s != 0) return s;
                    return Long.compare(a.time, b.time);
                }
            });
            // recortar a 10
            if (d.leaderboard.size() > 10) {
                d.leaderboard = new ArrayList<LeaderboardEntry>(d.leaderboard.subList(0, 10));
            }
            save(d);
            Gdx.app.log("SaveSystem", "Added score to leaderboard: " + e.souls + " almas");
        } catch (Exception ex) {
            Gdx.app.error("SaveSystem", "Failed to add score: " + ex.getMessage(), ex);
        }
    }

    /** Devuelve la lista top 10 (puede ser menor si no hay suficientes entradas). */
    public static List<LeaderboardEntry> getLeaderboardTop10() {
        SaveData d = load();
        if (d == null || d.leaderboard == null) return new ArrayList<LeaderboardEntry>();
        return d.leaderboard;
    }

    public static void clear() {
        try {
            FileHandle fh = getFile();
            if (fh != null && fh.exists()) fh.delete();
        } catch (Exception ignored) { }
    }
}


