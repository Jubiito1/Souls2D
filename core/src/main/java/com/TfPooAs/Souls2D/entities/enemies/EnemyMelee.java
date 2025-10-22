package com.TfPooAs.Souls2D.entities.enemies;

import com.TfPooAs.Souls2D.entities.Entity;
import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class EnemyMelee extends Entity {

    private Player player;
    private World world;
    private Body body;

    private float speed = 100f; // píxeles por segundo (ajustar según escala del juego)
    private float detectionRange = 300f;
    private float attackRange = 40f;
    private float attackCooldown = 1.5f;
    private float attackTimer = 0f;
    private boolean canAttack = true;

    private int damage = 10;

    // Constructor corregido: nombre de la clase y parámetros claros (world + posición + referencia al jugador)
    public EnemyMelee(World world, float x, float y, Player player) {
        super(x, y, "EnemieMelee.png"); // usa el constructor existente de Entity que recibe String
        this.world = world;
        this.player = player;
        createBody(x, y);
    }

    @Override
    public void update(float delta) {
        if (!active || player == null || !player.isAlive()) return;

        // Posiciones
        Vector2 playerPos = new Vector2(player.getPosition().x, player.getPosition().y);
        Vector2 enemyPos = new Vector2(position.x, position.y);

        float distance = playerPos.dst(enemyPos);

        // Movimiento hacia el jugador
        if (distance < detectionRange && distance > attackRange) {
            Vector2 direction = playerPos.cpy().sub(enemyPos).nor();
            position.add(direction.scl(speed * delta));
            // sincronizar body con posición si existe (opcional según tu integración Box2D)
            if (body != null) {
                body.setTransform(position.x, position.y, body.getAngle());
            }
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
            // Entity tiene un campo `texture` y width/height según tu Entity.java
            batch.draw(texture, position.x, position.y, width, height);
        }
    }

    // Implementación de createBody para evitar "Cannot resolve method 'createBody'"
    protected void createBody(float x, float y) {
        if (world == null) return;

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x, y);
        this.body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        // width/height vienen en píxeles desde Entity; si usas unidades Box2D convierte según tu escala.
        float hx = (width > 0 ? width : 32f) * 0.5f;
        float hy = (height > 0 ? height : 32f) * 0.5f;
        shape.setAsBox(hx, hy);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        body.createFixture(fdef);
        shape.dispose();
    }
}
