package com.TfPooAs.Souls2D.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Minimal JSON-based save system to persist last bonfire position locally.
 */
public class SaveSystem {
    private static final String SAVE_FILE = "savegame.json";

    public static class SaveData {
        public Float lastBonfireX; // pixels
        public Float lastBonfireY; // pixels
    }

    private static FileHandle getFile() {
        return Gdx.files.local(SAVE_FILE);
    }

    public static void saveLastBonfire(float x, float y) {
        try {
            SaveData data = load();
            if (data == null) data = new SaveData();
            data.lastBonfireX = x;
            data.lastBonfireY = y;
            Json json = new Json();
            getFile().writeString(json.prettyPrint(data), false, "UTF-8");
            Gdx.app.log("SaveSystem", "Saved last bonfire at (" + x + ", " + y + ")");
        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Failed to save: " + e.getMessage(), e);
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

    public static void clear() {
        try {
            FileHandle fh = getFile();
            if (fh != null && fh.exists()) fh.delete();
        } catch (Exception ignored) { }
    }
}
