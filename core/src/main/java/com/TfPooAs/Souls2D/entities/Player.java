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
import com.TfPooAs.Souls2D.entities.Enemy2;
import com.TfPooAs.Souls2D.systems.SoundManager;

public class Player extends Entity {
    private Body body;
    private World world;

    private float moveSpeed = 0.01f;
    private float jumpForce = 1f;
    private float maxHorizontalSpeed = 1.7f; // velocidad máxima horizontal (en unidades Box2D)
    private boolean isGrounded = true;

    // === Daño por caída ===
    // Velocidad vertical segura (m/s Box2D). Impactos por debajo no hacen daño.
    private float FALL_SAFE_SPEED = 6.0f;
    // Escala de daño: daño por cada m/s por encima de FALL_SAFE_SPEED
    private float FALL_DAMAGE_SCALE = 5.0f;
    // Tope de daño por caída
    private int FALL_MAX_DAMAGE = 70;
    // Reducción de daño si estabas rodando en el instante del impacto (0.5 = 50%)
    private float FALL_ROLL_REDUCTION = 0.5f;
    // Seguimiento del estado en el aire
    private boolean wasAirborne = false;
    private float minAirborneVelY = 0f; // velocidad Y más negativa alcanzada durante la caída
    private boolean rolledDuringFall = false; // si roló en cualquier momento de la caída

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
    private float STAM_COST_JUMP = 0f;   // %

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

    // Desplazamiento hacia delante durante el ataque (tunable)
    // Velocidad horizontal aplicada al iniciar el ataque (en unidades Box2D)
    private float attackLungeSpeed = 1f;
    // Duración en segundos durante la cual se mantiene ese empuje
    private float attackLungeDuration = 0.22f;

    // === Animaciones ===
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> healAnim;
    private Animation<TextureRegion> rollAnim;
    private Animation<TextureRegion> jumpAnim;
    private float stateTime = 0f;
    private float jumpTimer = 0f;
    private Texture idleSheetTexture;
    private Texture attackSheetTexture;
    private Texture walkSheetTexture;
    private Texture healSheetTexture;
    private Texture rollSheetTexture;
    private Texture jumpSheetTexture;

    // === Curación con Estus ===
    private estus estus = new estus();
    private boolean isHealing = false;
    private float healingTimer = 0f;
    private final float HEAL_DURATION = 4f;

    // === Rodar (esquivar) ===
    private boolean isRolling = false;
    private float rollTimer = 0f;
    private final float ROLL_DURATION = 0.30f; // duración de i-frames
    private final float ROLL_SPEED = 3.2f; // velocidad horizontal durante el rodar
    private Texture rollTexture;

    // Lista de enemigos para detectar golpes
    private Array<Enemy> enemies;
    private Array<Enemy2> rangedEnemies;


    public Player(World world, float x, float y) {
        super(x, y, "caballeroIdle-Sheet.png");
        this.world = world;
        this.enemies = new Array<>();
        this.rangedEnemies = new Array<>();


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
        // Caminata
        AnimationUtils.AnimWithTexture walkPair = AnimationUtils.createFromSpritesheetIfExists(
                "caballeroWalk-Sheet.png", 8, 1, 0.10f, Animation.PlayMode.LOOP);
        if (walkPair != null) {
            this.walkAnim = walkPair.animation;
            this.walkSheetTexture = walkPair.texture;
            if (this.idleAnim == null && this.attackAnim == null) {
                TextureRegion firstW = walkAnim.getKeyFrame(0f);
                this.width = firstW.getRegionWidth();
                this.height = firstW.getRegionHeight();
            }
        }
        // Estus (curación)
        AnimationUtils.AnimWithTexture healPair = AnimationUtils.createFromSpritesheetIfExists(
                "caballeroEstus-Sheet.png", 20, 1, 0.10f, Animation.PlayMode.NORMAL);
        if (healPair != null) {
            this.healAnim = healPair.animation;
            this.healSheetTexture = healPair.texture;
        }
        // Rodar (9 frames)
        AnimationUtils.AnimWithTexture rollPair = AnimationUtils.createFromSpritesheetIfExists(
                "caballeroRoll-Sheet.png", 9, 1, ROLL_DURATION / 9f, Animation.PlayMode.NORMAL);
        if (rollPair != null) {
            this.rollAnim = rollPair.animation;
            this.rollSheetTexture = rollPair.texture;
        }
        // Salto (4 frames)
        AnimationUtils.AnimWithTexture jumpPair = AnimationUtils.createFromSpritesheetIfExists(
                "caballeroJump-Sheet.png", 4, 1, 0.08f, Animation.PlayMode.NORMAL);
        if (jumpPair != null) {
            this.jumpAnim = jumpPair.animation;
            this.jumpSheetTexture = jumpPair.texture;
        }

        createBody(x, y);
    }

    // Método para agregar enemigos a la lista
    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }
    public void addRangedEnemy(Enemy2 enemy) {
        rangedEnemies.add(enemy);
    }

    // Método para remover enemigos de la lista
    public void removeEnemy(Enemy enemy) {
        enemies.removeValue(enemy, true);
    }

    public void removeRangedEnemy(Enemy2 enemy) {
        rangedEnemies.removeValue(enemy, true);
    }


    private Fixture currentFixture;
    // Hitbox base (en metros, Box2D). Personalizable mediante setters
    private float originalHalfWidth;   // half-width en metros
    private float originalHalfHeight;  // half-height en metros
    private float baseYOffset;         // offset vertical del centro en metros (positivo = hacia arriba)

    private void createBody(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / Constants.PPM, y / Constants.PPM);
        bdef.fixedRotation = true;
        body = world.createBody(bdef);

        // Por defecto, la hitbox coincide con el sprite; puede ser personalizada vía setters
        originalHalfWidth = (width / 5f) / Constants.PPM;
        originalHalfHeight = (height / 2f) / Constants.PPM;
        baseYOffset = 0f;
        currentFixture = createPlayerFixture(originalHalfWidth, originalHalfHeight, baseYOffset);
    }

    private Fixture createPlayerFixture(float halfW, float halfH, float yOffset) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfW, halfH, new Vector2(0, yOffset), 0f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0f;
        fdef.filter.categoryBits = Constants.BIT_PLAYER;
        // IMPORTANTE: Agregar BIT_PROJECTILE a la máscara
        fdef.filter.maskBits = Constants.BIT_GROUND | Constants.BIT_PROJECTILE;

        Fixture fx = body.createFixture(fdef);
        fx.setUserData("player");
        shape.dispose();
        return fx;
    }


    // Recrea la fixture según el estado actual (normal o rodando) usando la hitbox base configurable
    private void recreateFixtureForCurrentState() {
        if (body == null) return;
        if (currentFixture != null) {
            body.destroyFixture(currentFixture);
            currentFixture = null;
        }
        float halfW = originalHalfWidth;
        float halfH = originalHalfHeight;
        float yOffset = baseYOffset;
        if (isRolling) {
            float newHalfH = originalHalfHeight * 0.5f;
            float deltaH = originalHalfHeight - newHalfH; // lo que se "agacha"
            halfH = newHalfH;
            yOffset = baseYOffset - deltaH; // bajar el centro para conservar los pies en la misma altura
        }
        currentFixture = createPlayerFixture(halfW, halfH, yOffset);
    }

    // === API pública para configurar la hitbox en píxeles ===
    public void setHitboxSizePixels(float widthPx, float heightPx) {
        setHitboxPixels(widthPx, heightPx, baseYOffset * Constants.PPM);
    }

    public void setHitboxOffsetPixels(float yOffsetPx) {
        setHitboxPixels(originalHalfWidth * 2 * Constants.PPM, originalHalfHeight * 2 * Constants.PPM, yOffsetPx);
    }

    public void setHitboxPixels(float widthPx, float heightPx, float yOffsetPx) {
        if (widthPx <= 0 || heightPx <= 0) return;
        this.originalHalfWidth = (widthPx / 2f) / Constants.PPM;
        this.originalHalfHeight = (heightPx / 2f) / Constants.PPM;
        this.baseYOffset = (yOffsetPx) / Constants.PPM;
        recreateFixtureForCurrentState();
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

        // Seguimiento de caída mientras está en el aire
        if (!isGrounded) {
            Vector2 v = body.getLinearVelocity();
            // Registrar la velocidad Y mínima (más negativa) alcanzada
            if (!wasAirborne) {
                wasAirborne = true;
                minAirborneVelY = 0f;
                rolledDuringFall = isRolling;
            }
            if (v.y < minAirborneVelY) minAirborneVelY = v.y;
            if (isRolling) rolledDuringFall = true;
        }

        // Timer de salto/aire
        if (Math.abs(body.getLinearVelocity().y) > 0.05f) {
            jumpTimer += delta;
        } else {
            jumpTimer = 0f;
        }

        // === Limitar velocidad del cuerpo ===
        Vector2 velocity = body.getLinearVelocity();

        // Limitar velocidad horizontal
        if (velocity.x > maxHorizontalSpeed) velocity.x = maxHorizontalSpeed;
        else if (velocity.x < -maxHorizontalSpeed) velocity.x = -maxHorizontalSpeed;

        // Reaplicar la velocidad limitada al cuerpo
        body.setLinearVelocity(velocity);

        // === Audio de caminar ===
        try {
            boolean movingHoriz = Math.abs(body.getLinearVelocity().x) > 0.08f;
            // Debe sonar si se mueve en una dirección y está tocando el piso
            if (isGrounded && movingHoriz && !isRolling && !isAttacking) {
                SoundManager.ensureLooping("walk.wav");
            } else {
                SoundManager.stopLoop("walk.wav");
            }
        } catch (Exception ignored) { }

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
            if (!isAttacking && !isRolling && currentHealth < maxHealth) {
                estus.use(this);
                isHealing = true;
            }
        }

        // Movimiento horizontal: se permite durante curación pero más lento; bloqueado si atacando o rodando
        if (!isAttacking && !isRolling && !isHealing) {
            float speed = isHealing ? moveSpeed * 0.5f : moveSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                body.setLinearVelocity(vel.x - speed, vel.y);
                facingRight = false;
                if (vel.x < -1.5) {
                    moveSpeed = 0;
                } else {
                    moveSpeed = 0.1f;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                body.setLinearVelocity(vel.x + speed, vel.y);
                facingRight = true;
                if (vel.x > 1.5) {
                    moveSpeed = 0;
                } else {
                    moveSpeed = 0.1f;
                }
            } else if (Math.abs(vel.y) < 0.01f) {
                body.setLinearVelocity(0, vel.y);
            }
        }

        // Saltar solo si la velocidad Y es casi 0 (suelo) y no está atacando ni rodando
        if (!isAttacking && !isRolling && !isHealing && Gdx.input.isKeyJustPressed(Input.Keys.W) && Math.abs(vel.y) < 0.01f) {
            if (canSpendPercent(STAM_COST_JUMP)) {
                spendStaminaPercent(STAM_COST_JUMP);
                body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
                SoundManager.playSfx("jump.wav");
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

            // Empuje hacia delante durante la ventana inicial configurable
            float lungeTime = attackLungeDuration;
            Vector2 vel = body.getLinearVelocity();
            float dir = facingRight ? 1f : -1f;
            if (attackTimer <= lungeTime) {
                body.setLinearVelocity(dir * attackLungeSpeed, vel.y);
            } else {
                // Tras el lunge, bloquear la velocidad X para evitar deslizamiento
                body.setLinearVelocity(0f, vel.y);
            }

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

        // SFX: roll
        SoundManager.playSfx("roll.wav");

        // Reducir hitbox a la mitad de alto manteniendo los pies a la misma altura.
        recreateFixtureForCurrentState();
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

        // Restaurar hitbox base configurada
        recreateFixtureForCurrentState();
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
        float duration = (healAnim != null) ? healAnim.getAnimationDuration() : HEAL_DURATION;
        if (healingTimer >= duration) {
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

        // Aplicar un "lunge" (empuje hacia delante) al iniciar el ataque
        Vector2 vel = body.getLinearVelocity();
        float dir = facingRight ? 1f : -1f;
        // Establecemos velocidad horizontal hacia delante por un corto periodo
        body.setLinearVelocity(dir * attackLungeSpeed, vel.y);

        // SFX: slash del jugador
        SoundManager.playSfx("assets/slash_player.wav");

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


        // Revisar enemigos a distancia (Enemy2)
        for (Enemy2 rangedEnemy : rangedEnemies) {
            if (rangedEnemy.isDead() || !rangedEnemy.isActive()) continue;

            float distanceToEnemy = Vector2.dst(
                position.x + width/2f, position.y + height/2f,
                rangedEnemy.getPosition().x + rangedEnemy.getWidth()/2f,
                rangedEnemy.getPosition().y + rangedEnemy.getHeight()/2f
            );

            // Verificar si el enemigo está en rango y en la dirección correcta
            if (distanceToEnemy <= ATTACK_RANGE) {
                boolean enemyInFront = facingRight ?
                    (rangedEnemy.getPosition().x > position.x) :
                    (rangedEnemy.getPosition().x < position.x);

                if (enemyInFront) {
                    rangedEnemy.takeDamage(ATTACK_DAMAGE);
                    System.out.println("¡Golpeaste al enemigo a distancia!");
                }
            }
        }

    }

    // Teletransporta al jugador a coordenadas en píxeles (tomadas como esquina inferior izquierda del sprite)
    public void teleportToPixels(float pixelX, float pixelY) {
        float centerX = (pixelX + width / 2f) / Constants.PPM;
        float centerY = (pixelY + height / 2f) / Constants.PPM;
        body.setTransform(centerX, centerY, 0f);
        body.setLinearVelocity(0f, 0f);
        // Resetear trackers de caída para evitar daño falso tras teletransportes
        wasAirborne = false;
        minAirborneVelY = 0f;
        rolledDuringFall = false;
        // Actualizar también la posición visual inmediata
        this.position.set(pixelX, pixelY);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!active) return;

        // Prioridad de animación: Rodar > Curar > Atacar > En el aire (salto) > Caminar > Idle
        if (isRolling) {
            if (rollAnim != null) {
                TextureRegion frame = rollAnim.getKeyFrame(rollTimer);
                if (facingRight) {
                    batch.draw(frame, position.x, position.y, width, height);
                } else {
                    batch.draw(frame, position.x + width, position.y, -width, height);
                }
            } else {
                Texture tex = rollTexture != null ? rollTexture : texture;
                if (facingRight) {
                    batch.draw(tex, position.x, position.y, width, height);
                } else {
                    batch.draw(tex, position.x + width, position.y, -width, height);
                }
            }
            return;
        }
        if (isHealing) {
            if (healAnim != null) {
                TextureRegion frame = healAnim.getKeyFrame(healingTimer);
                if (facingRight) {
                    batch.draw(frame, position.x, position.y, width, height);
                } else {
                    batch.draw(frame, position.x + width, position.y, -width, height);
                }
            } else {
                Texture tex = healTexture != null ? healTexture : texture;
                if (facingRight) {
                    batch.draw(tex, position.x, position.y, width, height);
                } else {
                    batch.draw(tex, position.x + width, position.y, -width, height);
                }
            }
            return;
        }

        // Elegir frame según estado (ataque, salto, caminar o idle)
        if (isAttacking && attackAnim != null) {
            TextureRegion frame = attackAnim.getKeyFrame(attackTimer);
            if (facingRight) {
                batch.draw(frame, position.x, position.y, width, height);
            } else {
                batch.draw(frame, position.x + width, position.y, -width, height);
            }
        } else {
            boolean airborne = Math.abs(body.getLinearVelocity().y) > 0.05f;
            boolean moving = Math.abs(body.getLinearVelocity().x) > 0.05f;
            if (airborne && jumpAnim != null) {
                TextureRegion frame = jumpAnim.getKeyFrame(jumpTimer);
                if (facingRight) {
                    batch.draw(frame, position.x, position.y, width, height);
                } else {
                    batch.draw(frame, position.x + width, position.y, -width, height);
                }
            } else if (moving && walkAnim != null) {
                TextureRegion frame = walkAnim.getKeyFrame(stateTime);
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
    }

    // === Métodos del sistema de vida ===
    public void takeDamage(int damage) {
        if (isDead) return;
        // I-frames durante el rodar
        if (isRolling) {
            return;
        }

        if (damage <= 0) return;
        currentHealth -= damage;
        // SFX hurt cuando la vida disminuye
        try { SoundManager.playSfx("assets/hurt.wav"); } catch (Exception ignored) { }
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

    // Aplica daño de caída ignorando i-frames de rodar (ya consideramos reducción antes)
    private void applyFallDamage(int damage) {
        if (isDead) return;
        if (damage <= 0) return;
        currentHealth -= damage;
        // SFX hurt también por daño de caída
        try { SoundManager.playSfx("assets/hurt.wav"); } catch (Exception ignored) { }
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            System.out.println("¡Player murió por daño de caída! (" + damage + ")");
        } else {
            System.out.println("Daño de caída: " + damage + ". Vida: " + currentHealth + "/" + maxHealth);
        }
    }

    public void revive() {
        isDead = false;
        currentHealth = maxHealth;
        // Resetear trackers de caída al revivir
        wasAirborne = false;
        minAirborneVelY = 0f;
        rolledDuringFall = false;
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
        boolean wasGroundedBefore = this.isGrounded;
        this.isGrounded = grounded;

        // Detectar aterrizaje (transición aire -> suelo)
        if (grounded && !wasGroundedBefore && wasAirborne) {
            float impactSpeed = -minAirborneVelY; // velocidad positiva
            int damage = 0;
            if (impactSpeed > FALL_SAFE_SPEED) {
                float over = impactSpeed - FALL_SAFE_SPEED;
                float scaled = over * FALL_DAMAGE_SCALE;
                if (rolledDuringFall) {
                    scaled *= (1f - FALL_ROLL_REDUCTION);
                }
                damage = Math.min(FALL_MAX_DAMAGE, Math.max(0, Math.round(scaled)));
            }
            if (damage > 0 && !isDead) {
                applyFallDamage(damage);
            }
            // Reset trackers tras el aterrizaje
            wasAirborne = false;
            minAirborneVelY = 0f;
            rolledDuringFall = false;
        }

        // Si se marcó como no grounded, comienza la fase aérea
        if (!grounded && wasGroundedBefore) {
            wasAirborne = false; // se activará en update() al detectar !isGrounded
            minAirborneVelY = 0f;
            rolledDuringFall = false;
        }
    }

    public Body getBody() {
        return body;
    }

    // === Configuración del lunge de ataque ===
    public void setAttackLungeSpeed(float speed) {
        this.attackLungeSpeed = speed;
    }
    public float getAttackLungeSpeed() { return attackLungeSpeed; }

    public void setAttackLungeDuration(float seconds) {
        this.attackLungeDuration = Math.max(0f, seconds);
    }
    public float getAttackLungeDuration() { return attackLungeDuration; }

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
        if (walkSheetTexture != null) {
            walkSheetTexture.dispose();
        }
        if (healSheetTexture != null) {
            healSheetTexture.dispose();
        }
        if (rollSheetTexture != null) {
            rollSheetTexture.dispose();
        }
        if (jumpSheetTexture != null) {
            jumpSheetTexture.dispose();
        }
    }
}
