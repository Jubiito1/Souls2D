package com.TfPooAs.Souls2D.entities.items;

import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.systems.SaveSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Fogata estilo Dark Souls: cuando el jugador se acerca, aparece un aviso para presionar E y guardar.
 */
public class Bonfire {

    private final Vector2 position;
    private final float activationRadius;
    private final BitmapFont font;

    private boolean playerNearby;

    public Bonfire(float x, float y) {
        this.position = new Vector2(x, y);
        this.activationRadius = 80f; // en píxeles
        this.font = new BitmapFont(); // fuente por defecto
        this.playerNearby = false;
    }

    public void update(Player player, float delta) {
        if (player == null) return;
        // Distancia 2D simple usando posiciones en píxeles
        float dx = (player.getPosition().x) - position.x;
        float dy = (player.getPosition().y) - position.y;
        playerNearby = (dx * dx + dy * dy) <= (activationRadius * activationRadius);

        if (playerNearby && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            SaveSystem.save(player);
        }
    }

    public void render(SpriteBatch batch) {
        if (!playerNearby) return;
        // Mostrar prompt encima de la posición de la fogata
        String text = "Presiona E para guardar";
        font.draw(batch, text, position.x - 20, position.y + 40);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        font.dispose();
    }
}
