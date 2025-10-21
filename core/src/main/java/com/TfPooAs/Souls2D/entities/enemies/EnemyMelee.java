package com.TfPooAs.Souls2D.entities.enemies;

import com.TfPooAs.Souls2D.entities.Entity;
import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class EnemyMelee extends Entity {

    private Player player;

    private float speed = 100f; // pixeles por segundo
    private float detectionRange = 300f;
    private float attackRange = 40f;
    private float attackCooldown = 1.5f;
    private float attackTimer = 0f;
    private boolean canAttack = true;

    private int damage = 10;

    public Player(World world, float x, float y) {
        super(x, y, "player.png");
        this.world = world;
        createBody(x, y);
    }

    @Override
    public void update(float delta) {
        if (!active || !player.isAlive()) return;

        // Posiciones
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
