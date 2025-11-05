package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import com.TfPooAs.Souls2D.utils.Constants;
import com.TfPooAs.Souls2D.utils.AnimationUtils;

public class Player extends Entity {
    private Body body;
    private World world;

    private float moveSpeed = 0.3f;
    private float jumpForce = 1.7f;
    private boolean isGrounded = true;

    // === Sistema de vida ===
    private int maxHealth = 100;
    private int currentHealth = 100;
    private boolean isDead = false;

    // === Sistema de stamina ===
    private float maxStamina = 100f;
    private float currentStamina = 100f;
    private float staminaRegenPerSecond = 35f; // porcentaje por segundo (35% de la barra por segundo)
    private float staminaRegenDelay = 1.5f; // segundos sin gastar antes de regenerar
    private float staminaRegenDelayTimer = 0f;

    // Costes (como porcentaje de la barra máxima)
    private float STAM_COST_ROLL = 25f;   // %
    private float STAM_COST_ATTACK = 15f; // %
    private float STAM_COST_JUMP = 10f;   // %

    // === Sistema de ataque ===
    private Texture attackTexture;
    private Texture healTexture;
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private final float ATTACK_DURATION = 0.3f;
    private boolean facingRight = true;
    private final float ATTACK_RANGE = 100f; // rango de ataque en píxeles
    private final int ATTACK_DAMAGE = 25; // daño que hace a enemigos
    // Ventana de impacto del ataque (porcentaje del tiempo total)
    private boolean hasDealtDamageThisAttack = false;

    // === Animaciones ===
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> attackAnim;
    private float stateTime = 0f;
    private Texture idleSheetTexture; // solo si cargamos spritesheet
    private Texture attackSheetTexture; // solo si cargamos spritesheet

    // === Curación con Estus ===
    private estus estus = new estus();
    private boolean isHealing = false;
    private float healingTimer = 0f;
    private final float HEAL_DURATION = 4f;

    // === Rodar (esquivar) ===
    private boolean isRolling = false;
    private float rollTimer = 0f;
    private final float ROLL_DURATION = 0.45f; // duración de i-frames
    private final float ROLL_SPEED = 3.2f; // velocidad horizontal durante el rodar
    private Texture rollTexture;

    // Lista de enemigos para detectar golpes
    private Array<Enemy> enemies;

    public Player(World world, float x, float y) {
        super(x, y, "player.png");
        this.world = world;
        this.attackTexture = new Texture("player_attack.png");
        this.healTexture = new Texture("player_attack.png"); // usar temporalmente el mismo sprite
        this.rollTexture = new Texture("player_attack.png"); // placeholder para rodar
        this.enemies = new Array<>();

        // Cargar animaciones desde spritesheets si existen
        AnimationUtils.AnimWithTexture idlePair = AnimationUtils.createFromSpritesheetIfExists(
                "caballeroIdle-Sheet.png", 5, 1, 0.12f, Animation.PlayMode.LOOP);
        if (idlePair != null) {
            this.idleAnim = idlePair.animation;
            this.idleSheetTexture = idlePair.texture;
            // Ajustar tamaño del sprite al tamaño del frame
            TextureRegion first = idleAnim.getKeyFrame(0f);
            this.width = first.getRegionWidth();
            this.height = first.getRegionHeight();
        }
        AnimationUtils.AnimWithTexture attackPair = AnimationUtils.createFromSpritesheetIfExists(
                "caballeroAtaque-Sheet.png", 8, 1, 0.07f, Animation.PlayMode.NORMAL);
        if (attackPair != null) {
            this.attackAnim = attackPair.animation;
            this.attackSheetTexture = attackPair.texture;
            if (this.idleAnim == null) {
                // Si no hay idle, ajustar tamaño según ataque
                TextureRegion firstA = attackAnim.getKeyFrame(0f);
                this.width = firstA.getRegionWidth();
                this.height = firstA.getRegionHeight();
            }
        }

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

    private Fixture currentFixture;
    private float originalHalfWidth;
    private float originalHalfHeight;

    private void createBody(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / Constants.PPM, y / Constants.PPM);
        bdef.fixedRotation = true;
        body = world.createBody(bdef);

        originalHalfWidth = (width / 2f) / Constants.PPM;
        originalHalfHeight = (height / 2f) / Constants.PPM;
        currentFixture = createPlayerFixture(originalHalfWidth, originalHalfHeight, 0f);
    }

    private Fixture createPlayerFixture(float halfW, float halfH, float yOffset) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfW, halfH, new Vector2(0, yOffset), 0f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0f;
        fdef.filter.categoryBits = Constants.BIT_PLAYER;
        fdef.filter.maskBits = Constants.BIT_GROUND;

        Fixture fx = body.createFixture(fdef);
        fx.setUserData("player");
        shape.dispose();
        return fx;
    }

    public void update(float delta) {
        if (isDead) return;

        // Avanzar tiempo de estado para animaciones de bucle (idle)
        stateTime += delta;

        handleInput();
        updateRoll(delta);
        updateAttack(delta);
        updateHealing(delta);
        updateStamina(delta);

        // Sincronizar posición visual con posición del cuerpo
        position.set(
            body.getPosition().x * Constants.PPM - width / 2,
            body.getPosition().y * Constants.PPM - height / 2
        );
    }

    private void handleInput() {
        Vector2 vel = body.getLinearVelocity();

        // Iniciar rodar con ESPACIO si condiciones válidas
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!isAttacking && !isHealing && !isRolling && Math.abs(vel.y) < 0.05f) {
                // comprobar stamina
                if (canSpendPercent(STAM_COST_ROLL)) {
                    spendStaminaPercent(STAM_COST_ROLL);
                    startRoll();
                    return; // no aceptar otras entradas este frame
                } else {
                    // sin stamina suficiente, no rueda
                    // opcional: feedback
                }
            }
        }

        // Si está rodando, ignorar otras entradas (la velocidad se gestiona en updateRoll)
        if (isRolling) {
            return;
        }

        // Usar Estus con Q
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            if (!isAttacking && !isHealing && !isRolling && currentHealth < maxHealth) {
                estus.use(this);
            }
        }

        // Movimiento horizontal: se permite durante curación pero más lento; bloqueado si atacando o rodando
        if (!isAttacking && !isRolling) {
            float speed = isHealing ? moveSpeed * 0.5f : moveSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                body.setLinearVelocity(vel.x - speed, vel.y);
                facingRight = false;
                if (vel.x < -2) {
                    moveSpeed = 0;
                } else {
                    moveSpeed = 0.3f;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                body.setLinearVelocity(vel.x + speed, vel.y);
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

        // Saltar solo si la velocidad Y es casi 0 (suelo) y no está atacando ni rodando
        if (!isAttacking && !isRolling && Gdx.input.isKeyJustPressed(Input.Keys.W) && Math.abs(vel.y) < 0.01f) {
            if (canSpendPercent(STAM_COST_JUMP)) {
                spendStaminaPercent(STAM_COST_JUMP);
                body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
            } else {
                // sin stamina suficiente, no salta
            }
        }

        // Ataque con click izquierdo (si no está curando ni rodando)
        if (!isAttacking && !isHealing && !isRolling && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (canSpendPercent(STAM_COST_ATTACK)) {
                spendStaminaPercent(STAM_COST_ATTACK);
                startAttack();
            } else {
                // sin stamina suficiente, no ataca
            }
        }
    }

    private void updateAttack(float delta) {
        if (isAttacking) {
            attackTimer += delta;
            float duration = (attackAnim != null) ? attackAnim.getAnimationDuration() : ATTACK_DURATION;

            // Aplicar daño al INICIO de la animación (ventana temprana)
            float progress = attackTimer / duration;
            if (!hasDealtDamageThisAttack && progress >= 0.05f && progress <= 0.20f) {
                checkEnemyHits();
                hasDealtDamageThisAttack = true;
            }

            if (attackTimer >= duration) {
                endAttack();
            }
        }
    }

    // === Rodar (esquivar) ===
    private void startRoll() {
        isRolling = true;
        rollTimer = 0f;

        // Cancelar velocidad vertical excesiva; mantener gravedad
        Vector2 vel = body.getLinearVelocity();
        float dir = facingRight ? 1f : -1f;
        body.setLinearVelocity(dir * ROLL_SPEED, vel.y);

        // Reducir hitbox a la mitad de alto manteniendo los pies a la misma altura.
        if (currentFixture != null) {
            body.destroyFixture(currentFixture);
        }
        float newHalfH = originalHalfHeight * 0.5f;
        float yOffset = -(originalHalfHeight - newHalfH); // bajar el centro para conservar la base
        currentFixture = createPlayerFixture(originalHalfWidth, newHalfH, yOffset);
    }

    private void updateRoll(float delta) {
        if (!isRolling) return;
        rollTimer += delta;

        // Mantener empuje horizontal durante el roll (evitar frenado)
        Vector2 vel = body.getLinearVelocity();
        float dir = facingRight ? 1f : -1f;
        body.setLinearVelocity(dir * ROLL_SPEED, vel.y);

        if (rollTimer >= ROLL_DURATION) {
            endRoll();
        }
    }

    private void endRoll() {
        isRolling = false;
        rollTimer = 0f;

        // Restaurar hitbox original
        if (currentFixture != null) {
            body.destroyFixture(currentFixture);
        }
        currentFixture = createPlayerFixture(originalHalfWidth, originalHalfHeight, 0f);
    }

    // === Stamina ===
    private boolean canSpendPercent(float percent) {
        float required = (percent / 100f) * maxStamina;
        return currentStamina >= required - 0.001f; // tolerancia pequeña
    }

    private void spendStaminaPercent(float percent) {
        float amount = (percent / 100f) * maxStamina;
        currentStamina -= amount;
        if (currentStamina < 0f) currentStamina = 0f;
        staminaRegenDelayTimer = staminaRegenDelay; // reiniciar delay de regen
    }

    private void updateStamina(float delta) {
        // No regenerar mientras hay delay; el delay se congela si seguimos gastando
        if (staminaRegenDelayTimer > 0f) {
            staminaRegenDelayTimer -= delta;
            if (staminaRegenDelayTimer < 0f) staminaRegenDelayTimer = 0f;
            return;
        }
        // Regenerar si no estamos en acciones extenuantes
        if (!isAttacking && !isRolling) {
            currentStamina += staminaRegenPerSecond * delta;
            if (currentStamina > maxStamina) currentStamina = maxStamina;
        }
    }

    // === Curación (Estus) ===
    public void startHealing() {
        if (isDead) return;
        isHealing = true;
        healingTimer = 0f;
        System.out.println("Player está bebiendo Estus...");
    }

    private void updateHealing(float delta) {
        if (!isHealing) return;
        healingTimer += delta;
        if (healingTimer >= HEAL_DURATION) {
            endHealing();
        }
    }

    private void endHealing() {
        isHealing = false;
        healingTimer = 0f;
    }


    private void startAttack() {
        isAttacking = true;
        attackTimer = 0f;
        hasDealtDamageThisAttack = false;

        // Detener movimiento horizontal durante el ataque
        Vector2 vel = body.getLinearVelocity();
        body.setLinearVelocity(0, vel.y);

        System.out.println("¡Atacando!");
    }

    private void endAttack() {
        isAttacking = false;
        attackTimer = 0f;
        // Daño ya aplicado al inicio de la animación; no volver a golpear aquí.
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

        // Cuando se está rodando o curando, priorizar las texturas de estado
        if (isRolling) {
            Texture tex = rollTexture != null ? rollTexture : texture;
            if (facingRight) {
                batch.draw(tex, position.x, position.y, width, height);
            } else {
                batch.draw(tex, position.x + width, position.y, -width, height);
            }
            return;
        }
        if (isHealing) {
            Texture tex = healTexture != null ? healTexture : texture;
            if (facingRight) {
                batch.draw(tex, position.x, position.y, width, height);
            } else {
                batch.draw(tex, position.x + width, position.y, -width, height);
            }
            return;
        }

        // Elegir frame según estado (ataque o idle)
        if (isAttacking && attackAnim != null) {
            TextureRegion frame = attackAnim.getKeyFrame(attackTimer);
            if (facingRight) {
                batch.draw(frame, position.x, position.y, width, height);
            } else {
                batch.draw(frame, position.x + width, position.y, -width, height);
            }
        } else if (idleAnim != null) {
            TextureRegion frame = idleAnim.getKeyFrame(stateTime);
            if (facingRight) {
                batch.draw(frame, position.x, position.y, width, height);
            } else {
                batch.draw(frame, position.x + width, position.y, -width, height);
            }
        } else {
            // Fallback a texturas estáticas
            Texture tex;
            if (isAttacking) tex = attackTexture; else tex = texture;
            if (facingRight) {
                batch.draw(tex, position.x, position.y, width, height);
            } else {
                batch.draw(tex, position.x + width, position.y, -width, height);
            }
        }
    }

    // === Métodos del sistema de vida ===
    public void takeDamage(int damage) {
        if (isDead) return;
        // I-frames durante el rodar
        if (isRolling) {
            return;
        }

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
    public int getEstusCharges() { return estus != null ? estus.getCharges() : 0; }

    // Stamina getters
    public int getCurrentStamina() { return Math.round(currentStamina); }
    public int getMaxStamina() { return Math.round(maxStamina); }

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
        if (healTexture != null) {
            healTexture.dispose();
        }
        if (rollTexture != null) {
            rollTexture.dispose();
        }
        // Liberar hojas de sprites si se cargaron
        if (idleSheetTexture != null) {
            idleSheetTexture.dispose();
        }
        if (attackSheetTexture != null) {
            attackSheetTexture.dispose();
        }
    }
}
