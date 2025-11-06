package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SoulOrb {
    private Vector2 position;
    private int soulsAmount;
    private TextureRegion texture;
    private boolean collected;
    private float floatTimer;
    private float baseY; // <- NUEVO
    private Rectangle bounds;

    public SoulOrb(Vector2 position, int soulsAmount, TextureRegion texture) {
        this.position = new Vector2(position);
        this.baseY = position.y; // Guarda la Y base
        this.soulsAmount = soulsAmount;
        this.texture = texture;
        this.collected = false;
        this.bounds = new Rectangle(position.x, position.y,
            texture.getRegionWidth(), texture.getRegionHeight());
    }

    public void update(float delta) {
        if (collected) return;
        floatTimer += delta;
        // Oscila alrededor de baseY
        position.y = baseY + (float)Math.sin(floatTimer * 2f) * 5f; // 5 píxeles de amplitud
        bounds.setPosition(position.x, position.y);
    }

    public void render(SpriteBatch batch) {
        if (!collected) {
            batch.draw(texture, position.x, position.y);
        }
    }

    public boolean checkCollision(Rectangle playerBounds) {
        if (!collected && playerBounds.overlaps(bounds)) {
            collected = true;
            return true;
        }
        return false;
    }

    public boolean isCollected() {
        return collected;
    }

    public int getSoulsAmount() {
        return soulsAmount;
    }

    public Vector2 getPosition() {
        return position;
    }
}
