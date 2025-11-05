package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NPC extends Entity {
    protected float interactionRadius = 80f; // default interaction radius in pixels

    public NPC(float x, float y, String texturePath) {
        super(x, y, texturePath);
    }

    public NPC(float x, float y) {
        // Constructor de conveniencia con textura por defecto para evitar dependencias de assets nuevos
        this(x, y, "player.png");
    }

    @Override
    public void update(float delta) {
        // Por defecto, los NPCs no tienen comportamiento activo
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
    }

    public float getInteractionRadius() { return interactionRadius; }
    public void setInteractionRadius(float interactionRadius) { this.interactionRadius = interactionRadius; }
}
