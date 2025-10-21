package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {

    protected Vector2 position;
    protected TextureRegion texture;
    protected float width, height;

    protected int health = 100;
    protected boolean isAlive = true;
    protected boolean active = true;

    protected boolean facingRight = true;

    public Entity(float x, float y, TextureRegion texture) {
        this.position = new Vector2(x, y);
        this.texture = texture;
        this.width = texture.getRegionWidth();
        this.height = texture.getRegionHeight();
    }

    public abstract void update(float delta);
    public abstract void render(SpriteBatch batch);

    public void takeDamage(int damage) {
        if (!isAlive) return;
        health -= damage;
        if (health <= 0) die();
    }

    protected void die() {
        isAlive = false;
        active = false;
        System.out.println(getClass().getSimpleName() + " murió.");
    }

    // Getters y setters
    public Vector2 getPosition() { return position; }
    public boolean isAlive() { return isAlive; }
    public boolean isActive() { return active; }

    public void setGrounded(boolean b) {
    }
}
