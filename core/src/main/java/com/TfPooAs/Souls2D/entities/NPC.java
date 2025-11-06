package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Base para NPCs: entidad estática sin física, con radio de interacción.
 */
public abstract class NPC extends Entity {
    protected float interactionRadius = 80f; // en píxeles

    public NPC(float x, float y, String texturePath) {
        super(x, y, texturePath);
    }

    public NPC(float x, float y) {
        // Conveniencia: textura por defecto para no agregar nuevos assets
        this(x, y, "firekeeper-Sheet.png");
    }

    /** Por defecto los NPC no hacen nada en update */
    @Override
    public void update(float delta) { /* no-op */ }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
    }

    public float getInteractionRadius() { return interactionRadius; }
    public void setInteractionRadius(float r) { this.interactionRadius = r; }

    public boolean canInteract(Vector2 playerPos) {
        return playerPos != null && playerPos.dst(position.x, position.y) <= interactionRadius;
    }
}
