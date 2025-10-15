package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    protected Vector2 position;
    protected float width, height;
    protected Texture texture;
    protected boolean active = true; // útil para eliminar entidades

    public Entity(float x, float y, String texturePath) {
        this.position = new Vector2(x, y);
        this.texture = new Texture(texturePath);
        this.width = texture.getWidth();
        this.height = texture.getHeight();
    }

    // Método para actualizar la lógica
    public abstract void update(float delta);

    // Método para dibujar
    public void render(SpriteBatch batch) {
        if (active && texture != null) {
            batch.draw(texture, position.x, position.y, width, height);
        }
    }

    // Getters y setters útiles
    public Vector2 getPosition() { return position; }
    public void setPosition(float x, float y) { position.set(x, y); }

    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
