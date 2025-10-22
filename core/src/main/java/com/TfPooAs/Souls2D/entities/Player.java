package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import com.TfPooAs.Souls2D.utils.Constants;

public class Player extends Entity {
    private Body body;
    private World world;

    private float moveSpeed = 0.3f;
    private float jumpForce = 1f;
    private boolean isGrounded = true;

    // === Sistema de vida ===
    private int maxHealth = 100;
    private int currentHealth = 100;
    private boolean isDead = false;

    // === Sistema de ataque ===
    private Texture attackTexture;
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private final float ATTACK_DURATION = 0.3f;
    private boolean facingRight = true;
    private final float ATTACK_RANGE = 100f; // rango de ataque en píxeles
    private final int ATTACK_DAMAGE = 25; // daño que hace a enemigos

    // Lista de enemigos para detectar golpes
    private Array<Enemy> enemies;

    public Player(World world, float x, float y) {
        super(x, y, "player.png");
        this.world = world;
        this.attackTexture = new Texture("player_attack.png");
        this.enemies = new Array<>();
        createBody(x, y);
    }

    // Método para agregar enemigos a la lista
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    // Método para remover enemigos de la lista
    public void removeEnemy(Enemy enemy) {
        enemies.removeValue(enemy, true);
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
        fdef.filter.categoryBits = Constants.BIT_PLAYER;
        fdef.filter.maskBits = Constants.BIT_GROUND | Constants.BIT_ENEMY;

        body.createFixture(fdef).setUserData("player");
        shape.dispose();
    }

    public void update(float delta) {
        if (isDead) return;

        handleInput();
        updateAttack(delta);

        // Sincronizar posición visual con posición del cuerpo
        position.set(
            body.getPosition().x * Constants.PPM - width / 2,
            body.getPosition().y * Constants.PPM - height / 2
        );
    }

    private void handleInput() {
        Vector2 vel = body.getLinearVelocity();

        // Movimiento horizontal (solo si no está atacando)
        if (!isAttacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                body.setLinearVelocity(vel.x - moveSpeed, vel.y);
                facingRight = false;
                if (vel.x < -2) {
                    moveSpeed = 0;
                } else {
                    moveSpeed = 0.3f;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                body.setLinearVelocity(vel.x + moveSpeed, vel.y);
                facingRight = true;
                if (vel.x > 2) {
                    moveSpeed = 0;
                } else {
                    moveSpeed = 0.3f;
                }
            } else if (Math.abs(vel.y) < 0.01f) {
                body.setLinearVelocity(0, vel.y);
            }
        }

        // Saltar solo si la velocidad Y es casi 0 (suelo) y no está atacando
        if (!isAttacking && Gdx.input.isKeyJustPressed(Input.Keys.W) && Math.abs(vel.y) < 0.01f) {
            body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
        }

        // Ataque con click izquierdo
        if (!isAttacking && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            startAttack();
        }
    }

    private void updateAttack(float delta) {
        if (isAttacking) {
            attackTimer += delta;
            if (attackTimer >= ATTACK_DURATION) {
                endAttack();
            }
        }
    }

    private void startAttack() {
        isAttacking = true;
        attackTimer = 0f;

        // Detener movimiento horizontal durante el ataque
        Vector2 vel = body.getLinearVelocity();
        body.setLinearVelocity(0, vel.y);

        System.out.println("¡Atacando!");
    }

    private void endAttack() {
        isAttacking = false;
        attackTimer = 0f;

        // Verificar si golpeó a algún enemigo
        checkEnemyHits();
    }

    private void checkEnemyHits() {
        for (Enemy enemy : enemies) {
            if (enemy.isDead() || !enemy.isActive()) continue;

            float distanceToEnemy = Vector2.dst(
                position.x + width/2f, position.y + height/2f,
                enemy.getPosition().x + enemy.getWidth()/2f, enemy.getPosition().y + enemy.getHeight()/2f
            );

            // Verificar si el enemigo está en rango y en la dirección correcta
            if (distanceToEnemy <= ATTACK_RANGE) {
                boolean enemyInFront = facingRight ?
                    (enemy.getPosition().x > position.x) :
                    (enemy.getPosition().x < position.x);

                if (enemyInFront) {
                    enemy.takeDamage(ATTACK_DAMAGE);
                    System.out.println("¡Golpeaste al enemigo!");
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!active) return;

        Texture currentTexture = isAttacking ? attackTexture : texture;

        // Renderizar según la dirección que mira
        if (facingRight) {
            batch.draw(currentTexture, position.x, position.y, width, height);
        } else {
            // Voltear horizontalmente cuando mira a la izquierda
            batch.draw(currentTexture, position.x + width, position.y, -width, height);
        }
    }

    // === Métodos del sistema de vida ===
    public void takeDamage(int damage) {
        if (isDead) return;

        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            System.out.println("¡Player ha muerto!");
        } else {
            System.out.println("Player recibió " + damage + " de daño. Vida: " + currentHealth + "/" + maxHealth);
        }
    }

    public void heal(int healAmount) {
        if (isDead) return;

        currentHealth += healAmount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
        System.out.println("Player se curó " + healAmount + " puntos. Vida: " + currentHealth + "/" + maxHealth);
    }

    public void revive() {
        isDead = false;
        currentHealth = maxHealth;
        System.out.println("¡Player ha revivido!");
    }

    // === Getters ===
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDead() { return isDead; }
    public boolean isAttacking() { return isAttacking; }
    public boolean isFacingRight() { return facingRight; }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public Body getBody() {
        return body;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (attackTexture != null) {
            attackTexture.dispose();
        }
    }
}
