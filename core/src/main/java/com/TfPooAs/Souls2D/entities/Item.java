package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;

public abstract class Item extends Entity {
    protected boolean playerInRange = false;   // Si el jugador está cerca
    protected Texture interactIcon;            // Icono de la tecla E
    protected float interactionRange = 48f;    // Distancia de interacción
    protected boolean interacted = false;      // Si ya fue usado

    public Item(float x, float y, String texturePath) {
        super(x, y, texturePath);
        interactIcon = new Texture("ui/interact_e.png");
    }

    // Update ahora recibe el jugador
    public void update(float delta, Player player) {
        if (!active) return;

        // Chequear si está en rango
        checkPlayerInRange(player);

        // Si está cerca y presiona E
        if (playerInRange && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            onInteract(player);
        }
    }

    // Dibuja el ítem y el icono si se puede interactuar
    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);

        if (playerInRange && !interacted) {
            float iconX = position.x + width / 2 - interactIcon.getWidth() / 2;
            float iconY = position.y + height + 8;
            batch.draw(interactIcon, iconX, iconY);
        }
    }

    // Comprueba si el jugador está cerca
    protected void checkPlayerInRange(Player player) {
        if (player == null) return;

        Vector2 playerPos = player.getPosition();
        float distance = position.dst(playerPos);
        playerInRange = distance < interactionRange;
    }

    // Cada ítem define su propio comportamiento
    protected abstract void onInteract(Player player);

    @Override
    public void dispose() {
        super.dispose();
        if (interactIcon != null) interactIcon.dispose();
    }
}
