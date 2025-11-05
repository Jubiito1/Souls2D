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
 * Clase ligera sin dependencia de Item; cumple con el uso en GameScreen.
 */
public class Bonfire {

    private final Vector2 position;
    private final float activationRadius;
    private final BitmapFont font;

    private boolean playerNearby;
    private boolean justRested; // se activa cuando el jugador guarda en esta hoguera

    public Bonfire(float x, float y) {
        this.position = new Vector2(x, y);
        this.activationRadius = 80f; // en píxeles
        this.font = new BitmapFont(); // fuente por defecto
        this.playerNearby = false;
        this.justRested = false;
    }

    // Firma compatible con GameScreen: update(delta, player)
    public void update(float delta, Player player) {
        if (player == null) return;
        // Distancia 2D simple usando posiciones en píxeles
        float dx = (player.getPosition().x) - position.x;
        float dy = (player.getPosition().y) - position.y;
        playerNearby = (dx * dx + dy * dy) <= (activationRadius * activationRadius);

        if (playerNearby && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            SaveSystem.saveLastBonfire(player.getPosition().x, player.getPosition().y);
            justRested = true; // notificar al GameScreen que se ha descansado en esta hoguera
        }
    }

    public void render(SpriteBatch batch) {
        if (!playerNearby) return;
        // Mostrar prompt encima de la posición de la fogata
        String text = "Presiona E para guardar";
        font.draw(batch, text, position.x - 20, position.y + 40);
    }

    /**
     * Devuelve true solo una vez después de descansar. Sirve para que GameScreen reaccione (curar y resetear enemigos).
     */
    public boolean consumeJustRested() {
        if (justRested) {
            justRested = false;
            return true;
        }
        return false;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        font.dispose();
    }
}
