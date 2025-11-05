package com.TfPooAs.Souls2D.entities.items;

import com.TfPooAs.Souls2D.entities.Item;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.systems.SaveSystem;

/**
 * Minimal Bonfire item that can be placed from Tiled. Uses a default texture to avoid new assets.
 */
public class Bonfire extends Item {

    public Bonfire(float x, float y) {
        // Reuse existing player.png as placeholder sprite to avoid adding assets
        super(x, y, "player.png");
        this.interactionRange = 64f;
    }

    public Bonfire(float x, float y, String texturePath) {
        super(x, y, texturePath);
        this.interactionRange = 64f;
    }

    @Override
    public void update(float delta) {
        // Entity.update requirement: no-op; GameScreen should call Item.update(delta, player)
    }

    @Override
    protected void onInteract(Player player) {
        // Save the player's current position in pixels as the last checkpoint
        if (player != null) {
            SaveSystem.saveLastBonfire(player.getPosition().x, player.getPosition().y);
        }
        // mark interacted to avoid repeat in same frame
        this.interacted = true;
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
