package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * NPC base: entidad estática sin física, con radio de interacción.
 */
public abstract class NPC extends Entity {
    protected float interactionRadius = 80f; // en píxeles

    public NPC(float x, float y, String texturePath) {
        super(x, y, texturePath);
    }

    public NPC(float x, float y) {
        super();
        this.position = new Vector2(x, y);
    }

    /** NPCs por defecto no se mueven */
    @Override
    public void update(float delta) {
        // Nada: NPC estático
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
    }

    public boolean canInteract(Vector2 playerPos) {
        return playerPos.dst(position.x, position.y) <= interactionRadius;
    }

    public float getInteractionRadius() { return interactionRadius; }
    public void setInteractionRadius(float r) { this.interactionRadius = r; }
}
