package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NPC extends Entity {
    protected float interactionRadius = 80f; // default interaction radius in pixels
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
        // Constructor de conveniencia con textura por defecto para evitar dependencias de assets nuevos
        this(x, y, "player.png");
    }

    @Override
    public void update(float delta) {
        // Por defecto, los NPCs no tienen comportamiento activo
        // Constructor de conveniencia: usa una textura por defecto para no romper llamadas existentes.
        this(x, y, "player.png");
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

    public float getInteractionRadius() { return interactionRadius; }
    public void setInteractionRadius(float interactionRadius) { this.interactionRadius = interactionRadius; }
    public boolean canInteract(Vector2 playerPos) {
        return playerPos.dst(position.x, position.y) <= interactionRadius;
    }

    public float getInteractionRadius() { return interactionRadius; }
    public void setInteractionRadius(float r) { this.interactionRadius = r; }
}
