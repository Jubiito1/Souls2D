package com.TfPooAs.Souls2D.systems;

import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
}
