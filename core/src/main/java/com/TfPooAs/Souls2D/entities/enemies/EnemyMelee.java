package com.TfPooAs.Souls2D.entities.enemies;

import com.TfPooAs.Souls2D.entities.Entity;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class EnemyMelee extends Entity {

    private World world;
    private Body body;

    private Player player; // referencia al jugador

    // Comportamiento
    private float speed = 1.5f;
    private float detectionRange = 3f;
    private float attackRange = 0.7f;
    private float attackCooldown = 2.5f;
    private float attackDuration = 1.2f;
    private float attackTimer = 0f;
    private boolean attacking = false;
    private boolean canAttack = true;

    private int damage = 10;

    public EnemyMelee(World world, float x, float y, Player player) {
        super(x, y, "enemy/melee.png");
        this.world = world;
        this.player = player; // guardamos referencia
        createBody(x, y);
    }

    private void createBody(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / Constants.PPM, y / Constants.PPM);
        bdef.fixedRotation = true;
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 / Constants.PPM, height / 2 / Constants.PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0f;
        fdef.filter.categoryBits = Constants.BIT_ENEMY;
        fdef.filter.maskBits = Constants.BIT_GROUND | Constants.BIT_PLAYER;

        body.createFixture(fdef).setUserData("enemy_melee");
        shape.dispose();
    }

    // IMPLEMENTACIÓN OBLIGATORIA DEL ABSTRACTO
    @Override
    public void update(float delta) {
        if (!active || player == null) return;

        Vector2 playerPos = player.getBody().getPosition();
        Vector2 enemyPos = body.getPosition();
        float distance = playerPos.dst(enemyPos);

        if (attacking) {
            attackTimer += delta;
            if (attackTimer >= attackDuration) {
                attacking = false;
                canAttack = false;
                attackTimer = 0f;
            } else {
                checkAttackHit(distance);
            }
            return;
        }

        // Movimiento
        if (distance < detectionRange && distance > attackRange) {
            Vector2 direction = playerPos.cpy().sub(enemyPos).nor();
            body.setLinearVelocity(direction.scl(speed));
        } else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }

        // Iniciar ataque
        if (distance <= attackRange && canAttack) {
            attacking = true;
            attackTimer = 0f;
        }

        // Cooldown
        if (!attacking && !canAttack) {
            attackTimer += delta;
            if (attackTimer >= attackCooldown) {
                canAttack = true;
                attackTimer = 0;
            }
        }

        // Actualizar posición visual
        position.set(
            body.getPosition().x * Constants.PPM - width / 2,
            body.getPosition().y * Constants.PPM - height / 2
        );
    }

    private void checkAttackHit(float distance) {
        if (distance <= attackRange) {
            System.out.println("¡Player recibió " + damage + " de daño!");
            // Aquí podrías llamar player.takeDamage(damage);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (active) {
            batch.draw(texture, position.x, position.y, width, height);
        }
    }

    public Body getBody() {
        return body;
    }
}
