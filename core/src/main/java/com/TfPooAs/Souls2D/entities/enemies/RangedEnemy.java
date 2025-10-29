package com.TfPooAs.Souls2D.entities.enemies;

import com.TfPooAs.Souls2D.core.ProjectileManager;
import com.TfPooAs.Souls2D.entities.Entity;
import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Enemigo a distancia simple que dispara hacia el jugador usando ProjectileManager.
 */
public class RangedEnemy extends Entity {

    private final Player player;
    private final ProjectileManager projectileManager;

    // Parámetros ajustables
    private float fireRate = 1.2f;        // segundos entre disparos
    private float fireCooldown = 0f;
    private float projectileSpeed = 80f;  // píxeles/segundo
    private float fireRange = 800f;       // distancia máxima para disparar (píxeles)
    private int projectileDamage = 10;

    public RangedEnemy(float x, float y, Player player, ProjectileManager projectileManager) {
        super(x, y, "enemy.png");
        this.player = player;
        this.projectileManager = projectileManager;
    }

    @Override
    public void update(float delta) {
        if (!active || player == null || !player.isAlive() || projectileManager == null) return;

        Vector2 myPos = getPosition();
        Vector2 playerPos = player.getPosition();

        float dist = myPos.dst(playerPos);
        if (dist <= fireRange) {
            fireCooldown -= delta;
            if (fireCooldown <= 0f) {
                fireCooldown = fireRate;
                shootAt(playerPos);
            }
        }
    }

    private void shootAt(Vector2 target) {
        Vector2 dir = new Vector2(target).sub(getPosition()).nor();
        Vector2 velocity = dir.scl(projectileSpeed);

        Proyectile p = new Proyectile(new Vector2(getPosition()), velocity, projectileDamage, projectileManager);
        projectileManager.spawnProjectile(p);
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
    }

    // Getters / setters para tunear en tiempo de ejecución
    public void setFireRate(float fireRate) { this.fireRate = fireRate; }
    public void setProjectileSpeed(float projectileSpeed) { this.projectileSpeed = projectileSpeed; }
    public void setFireRange(float fireRange) { this.fireRange = fireRange; }
    public void setProjectileDamage(int projectileDamage) { this.projectileDamage = projectileDamage; }
}
