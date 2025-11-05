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
import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Sistema simple de guardado. Guarda datos mínimos del jugador.
 */
public class SaveSystem {

    private static final String SAVE_PATH = "savegame.json"; // almacenamiento local (carpeta de la app)

    public static void save(Player player) {
        if (player == null) return;
        // Posición del jugador en píxeles (según Entity.position)
        float x = player.getPosition().x;
        float y = player.getPosition().y;
        String content = "{\n" +
            "  \"player\": {\n" +
            "    \"x\": " + x + ",\n" +
            "    \"y\": " + y + "\n" +
            "  },\n" +
            "  \"timestamp\": \"" + java.time.Instant.now().toString() + "\"\n" +
            "}";
        FileHandle fh = Gdx.files.local(SAVE_PATH);
        fh.writeString(content, false);
        Gdx.app.log("SaveSystem", "Partida guardada en " + fh.file().getAbsolutePath());
    }


    /**
     * Carga la última posición guardada del jugador. Devuelve null si no existe o hay error.
     */
    public static Vector2 loadLastPlayerPosition() {
        try {
            FileHandle fh = Gdx.files.local(SAVE_PATH);
            if (fh == null || !fh.exists()) return null;
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(fh);
            JsonValue player = root.get("player");
            if (player == null) return null;
            float x = player.getFloat("x");
            float y = player.getFloat("y");
            return new Vector2(x, y);
        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "No se pudo cargar la partida: " + e.getMessage());
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


