package com.TfPooAs.Souls2D.entities;

import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Entity {

    protected Player player; // referencia al jugador
    protected float speed = 100f; // pixeles por segundo
    protected float detectionRange = 300f;
    protected float attackRange = 40f;
    protected float attackCooldown = 1.5f;
    protected float attackTimer = 0f;
    protected boolean canAttack = true;
    protected int damage = 10;

    public Enemy(Player player, float x, float y, TextureRegion texture) {
        super(x, y, texture);
        this.player = player;
    }

    @Override
    public void update(float delta) {
        if (!active || !player.isAlive()) return;

        Vector2 playerPos = new Vector2(player.getPosition().x, player.getPosition().y);
        Vector2 enemyPos = new Vector2(position.x, position.y);

        float distance = playerPos.dst(enemyPos);

        // Movimiento hacia el jugador
        if (distance < detectionRange && distance > attackRange) {
            Vector2 direction = playerPos.cpy().sub(enemyPos).nor();
            position.add(direction.scl(speed * delta));
        }

        // Ataque
        if (distance <= attackRange && canAttack) {
            player.takeDamage(damage);
            canAttack = false;
            attackTimer = 0f;
        }

        // Cooldown de ataque
        if (!canAttack) {
            attackTimer += delta;
            if (attackTimer >= attackCooldown) {
                canAttack = true;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (active) {
            batch.draw(texture, position.x, position.y, width, height);
        }
    }
}
