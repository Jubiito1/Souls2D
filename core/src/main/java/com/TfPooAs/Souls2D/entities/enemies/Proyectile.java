package com.TfPooAs.Souls2D.entities.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.TfPooAs.Souls2D.core.ProjectileManager;
import com.TfPooAs.Souls2D.entities.Player;

/**
 * Proyectil simple basado en posición (no Box2D).
 * - Se mueve a velocidad constante.
 * - Colisiona por distancia contra el Player.
 * - Tiene lifetime y se autodestruye.
 */
public class Proyectile {

    private Vector2 position;
    private final Vector2 velocity;
    private final int damage;
    private float lifetime = 6f; // segundos
    private boolean dead = false;

    // Visual (pon tu textura desde el AssetManager y llama setTexture)
    private static Texture texture;
    private static final float WIDTH = 8f;
    private static final float HEIGHT = 8f;

    private final ProjectileManager manager;

    public Proyectile(Vector2 startPos, Vector2 velocity, int damage, ProjectileManager manager) {
        this.position = new Vector2(startPos);
        this.velocity = new Vector2(velocity);
        this.damage = damage;
        this.manager = manager;
    }

    public void update(float delta) {
        if (dead) return;

        // Movimiento
        position.mulAdd(velocity, delta);

        // Lifetime
        lifetime -= delta;
        if (lifetime <= 0f) {
            kill();
            return;
        }

        // Colisión con player por distancia
        Player player = manager.getPlayer();
        if (player != null && player.isAlive()) {
            Vector2 pPos = player.getPosition();
            if (pPos.dst(position) < Math.max(WIDTH, HEIGHT)) {
                player.takeDamage(damage);
                kill();
                return;
            }
        }

        // Colisión con el mundo (si manager expone isSolidAt)
        if (manager.isSolidAt(position.x, position.y)) {
            kill();
        }
    }

    public void render(SpriteBatch batch) {
        if (dead) return;
        if (texture != null) {
            batch.draw(texture, position.x - WIDTH / 2f, position.y - HEIGHT / 2f, WIDTH, HEIGHT);
        }
    }

    public void kill() {
        if (dead) return;
        dead = true;
        manager.removeProjectile(this);
    }

    public boolean isDead() { return dead; }

    // Texture shared setter (llamar desde tu AssetManager/loader)
    public static void setTexture(Texture tex) { texture = tex; }

    public Vector2 getPosition() { return position; }
}
