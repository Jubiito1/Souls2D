
package com.TfPooAs.Souls2D.entities.enemies;

import com.TfPooAs.Souls2D.entities.Enemy;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.utils.AnimationUtils;
import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.TfPooAs.Souls2D.systems.SoundManager;

/**
 * Boss Iudex Gundyr - Boss final del juego
 * - Hitbox más grande apropiada para boss
 * - Animaciones de walk corregidas
 * - Sistema de IA complejo con múltiples ataques
 * - Escala visual ajustable
 * - Barra de vida propia de boss
 */
public class IudexGundyr extends Enemy {

    // === Configuración del Boss ===
    private int MAX_HP = 600;
    private int hp = MAX_HP;

    // Escala visual del boss (más grande que enemigos normales)
    private float SCALE = 1f; // Aumentado un poco más

    // Rangos de ataque (aumentados por hitbox más grande)
    private float ATTACK1_RANGE = 80f; // Aumentado
    private float ATTACK2_RANGE = 90f; // Aumentado

    // Daño de ataques
    private int ATTACK1_DAMAGE = 35;
    private int ATTACK2_DAMAGE = 45;

    // Velocidades de ataque (más lentas para boss)
    private float ATTACK1_WINDUP = 0.7f;
    private float ATTACK1_ACTIVE = 0.3f;
    private float ATTACK1_RECOVERY = 1.0f;
    private float ATTACK1_DASH_SPEED = 3.8f; // Ajustado para hitbox más grande

    private float ATTACK2_WINDUP = 0.8f;
    private float ATTACK2_ACTIVE = 0.3f;
    private float ATTACK2_RECOVERY = 1.2f;

    // Movimiento
    private float CHASE_SPEED = 0.50f; // Ajustado para hitbox más grande
    private float ATTACK_COOLDOWN = 3.5f;
    private float ATTACK_COOLDOWN_JITTER = 0.8f;



    // === Estados del Boss ===
    private enum BossState {
        IDLE, CHASE,
        ATTACK1_WINDUP, ATTACK1_ACTIVE, ATTACK1_RECOVERY,
        ATTACK2_WINDUP, ATTACK2_ACTIVE, ATTACK2_RECOVERY,
        DEAD
    }

    private BossState currentState = BossState.IDLE;
    private float stateTimer = 0f;
    private float totalTime = 0f;
    private boolean facingRight = true;
    private boolean hitApplied = false;

    // Referencias
    private final Player playerRef;
    private final Body body;

    // Animaciones específicas del boss
    private Animation<TextureRegion> bossIdleAnim;
    private Animation<TextureRegion> bossWalkAnim;
    private Animation<TextureRegion> bossAttack1Anim;
    private Animation<TextureRegion> bossAttack2Anim;

    // === Animación de muerte ===
    private Animation<TextureRegion> bossDeathAnim;
    private Texture deathTexture;
    private float deathTimer = 0f;


    // Texturas para disposar
    private Texture idleTexture;
    private Texture walkTexture;
    private Texture attack1Texture;
    private Texture attack2Texture;

    // Control de cooldowns
    private float nextAttack1Time = 0f;
    private float nextAttack2Time = 0f;

    // Textura para barra de vida
    private static Texture whiteTexture;

    // === Control mejorado de animación de walk ===
    private float walkAnimTimer = 0f; // Timer separado para walk
    private boolean isWalking = false; // Estado de caminar

    public IudexGundyr(World world, float x, float y, Player player) {
        super(world, x, y, player);
        this.playerRef = player;
        this.body = getBody();


        loadBossAnimations();
        initializeBossStats();
        createBossHitbox(); // Crear hitbox más grande
    }

    private void loadBossAnimations() {
        // === ANIMACIÓN IDLE ===
        AnimationUtils.AnimWithTexture idlePair = AnimationUtils.createFromSpritesheetIfExists(
            "gundyrWalk-Sheet.png", 1, 1, 0.8f, Animation.PlayMode.LOOP);
        if (idlePair != null) {
            this.bossIdleAnim = idlePair.animation;
            this.idleTexture = idlePair.texture;
            Gdx.app.log("IudexGundyr", "Animación IDLE cargada correctamente");
        } else {
            Gdx.app.error("IudexGundyr", "No se pudo cargar animación IDLE");
        }

        // === ANIMACIÓN WALK - CORREGIDA ===
        AnimationUtils.AnimWithTexture walkPair = AnimationUtils.createFromSpritesheetIfExists(
            "gundyrWalk-Sheet.png", 7, 1, 0.15f, Animation.PlayMode.LOOP); // Velocidad más lenta y clara
        if (walkPair != null) {
            this.bossWalkAnim = walkPair.animation;
            this.walkTexture = walkPair.texture;
            Gdx.app.log("IudexGundyr", "Animación WALK cargada correctamente - 7 frames");
        } else {
            Gdx.app.error("IudexGundyr", "No se pudo cargar animación WALK");
        }

        // === ANIMACIÓN ATAQUE 1 ===
        AnimationUtils.AnimWithTexture attack1Pair = AnimationUtils.createFromSpritesheetIfExists(
            "gundyrAtaque-Sheet.png", 6, 1, 0.08f, Animation.PlayMode.NORMAL);
        if (attack1Pair != null) {
            this.bossAttack1Anim = attack1Pair.animation;
            this.attack1Texture = attack1Pair.texture;
            Gdx.app.log("IudexGundyr", "Animación ATAQUE 1 cargada correctamente");
        } else {
            Gdx.app.error("IudexGundyr", "No se pudo cargar animación ATAQUE 1");
        }

        // === ANIMACIÓN ATAQUE 2 ===
        AnimationUtils.AnimWithTexture attack2Pair = AnimationUtils.createFromSpritesheetIfExists(
            "gundyrAtaque2-Sheet.png", 7, 1, 0.08f, Animation.PlayMode.NORMAL);
        if (attack2Pair != null) {
            this.bossAttack2Anim = attack2Pair.animation;
            this.attack2Texture = attack2Pair.texture;
            Gdx.app.log("IudexGundyr", "Animación ATAQUE 2 cargada correctamente");
        } else {
            Gdx.app.error("IudexGundyr", "No se pudo cargar animación ATAQUE 2");
        }

        // Determinar tamaño visual
        TextureRegion baseFrame = null;
        if (bossWalkAnim != null) {
            baseFrame = bossWalkAnim.getKeyFrame(0);
        } else if (bossIdleAnim != null) {
            baseFrame = bossIdleAnim.getKeyFrame(0);
        } else {
            baseFrame = new TextureRegion(texture);
        }
        if (baseFrame == null) {
            Texture fallback = new Texture("gundyrWalk-Sheet.png"); // o cualquier textura conocida
            baseFrame = new TextureRegion(fallback);
        }

        // === ANIMACIÓN DE MUERTE ===
        AnimationUtils.AnimWithTexture deathPair = AnimationUtils.createFromSpritesheetIfExists(
            "Iudex-Death-Sheet.png", 6, 1, 0.15f, Animation.PlayMode.NORMAL);
        if (deathPair != null) {
            this.bossDeathAnim = deathPair.animation;
            this.deathTexture = deathPair.texture;
            Gdx.app.log("IudexGundyr", "Animación DEATH cargada correctamente");
        } else {
            Gdx.app.error("IudexGundyr", "No se pudo cargar animación DEATH");
        }


        this.width = baseFrame.getRegionWidth() * SCALE;
        this.height = baseFrame.getRegionHeight() * SCALE;

        Gdx.app.log("IudexGundyr", "Boss cargado con dimensiones visuales: " + width + "x" + height);
    }

    private void createBossHitbox() {
        // Crear hitbox más grande específica para el boss
        // Destruir fixture actual del Enemy padre si existe
        if (body.getFixtureList().size > 0) {
            for (Fixture fixture : body.getFixtureList()) {
                body.destroyFixture(fixture);
            }
        }

        // Crear nueva hitbox más grande
        PolygonShape bossShape = new PolygonShape();

        // Hitbox más grande para boss (en metros Box2D)
        float bossHalfWidth = 45f / Constants.PPM;  // ~45 píxeles de ancho = más grande
        float bossHalfHeight = 55f / Constants.PPM; // ~55 píxeles de alto = más grande

        bossShape.setAsBox(bossHalfWidth, bossHalfHeight);

        FixtureDef bossFixtureDef = new FixtureDef();
        bossFixtureDef.shape = bossShape;
        bossFixtureDef.density = 2.0f; // Más denso que enemigos normales
        bossFixtureDef.friction = 0.2f;
        bossFixtureDef.filter.categoryBits = Constants.BIT_ENEMY;
        bossFixtureDef.filter.maskBits = Constants.BIT_GROUND | Constants.BIT_PLAYER;

        Fixture bossFixture = body.createFixture(bossFixtureDef);
        bossFixture.setUserData("enemy"); // Mantener compatibilidad con ContactListener

        bossShape.dispose();

        Gdx.app.log("IudexGundyr", "Hitbox del boss creada: " +
            (bossHalfWidth  * Constants.PPM) + "x" + (bossHalfHeight  * Constants.PPM) + " píxeles");
    }

    private void initializeBossStats() {
        hp = MAX_HP;
        currentState = BossState.IDLE;
        stateTimer = 0f;
        totalTime = 0f;
        walkAnimTimer = 0f;
        isWalking = false;

        Gdx.app.log("IudexGundyr", "Boss inicializado con " + MAX_HP + " HP");
    }



    @Override
    public void update(float delta) {
        if (currentState == BossState.DEAD && bossDeathAnim != null) {
            if (!bossDeathAnim.isAnimationFinished(deathTimer)) {
                deathTimer += delta;
            }
            return; // evita IA y ataques
        }


        stateTimer += delta;
        totalTime += delta;

        // Sincronizar posición visual con física
        position.set(
            body.getPosition().x * Constants.PPM - width / 2f,
            body.getPosition().y * Constants.PPM - height / 2f
        );

        // Detectar jugador
        if (playerRef != null && !playerRef.isDead()) {
            float distanceToPlayer = getDistanceToPlayer();
            facingRight = playerRef.getPosition().x > position.x;

            updateBossAI(delta, distanceToPlayer);
        }

        // Actualizar timer de animación de caminar
        if (isWalking) {
            walkAnimTimer += delta;
        }

    }

    private void updateBossAI(float delta, float distanceToPlayer) {
        // Resetear estado de caminar
        isWalking = false;

        switch (currentState) {
            case IDLE:
                // Transición a perseguir si el jugador está cerca
                if (distanceToPlayer <= 350f) { // Aumentado por hitbox más grande
                    changeState(BossState.CHASE);
                }
                break;

            case CHASE:
                isWalking = true; // Marcar que está caminando

                // Decidir qué ataque usar basado en distancia
                boolean canAttack1 = canUseAttack1() && distanceToPlayer <= ATTACK1_RANGE * SCALE;
                boolean canAttack2 = canUseAttack2() && distanceToPlayer <= ATTACK2_RANGE * SCALE;

                if (canAttack1 && canAttack2) {
                    isWalking = false; // Parar de caminar antes del ataque
                    // Elegir ataque basado en distancia
                    if (distanceToPlayer < ATTACK1_RANGE * SCALE * 0.7f) {
                        changeState(BossState.ATTACK1_WINDUP);
                    } else {
                        changeState(BossState.ATTACK2_WINDUP);
                    }
                } else if (canAttack1) {
                    isWalking = false;
                    changeState(BossState.ATTACK1_WINDUP);
                } else if (canAttack2) {
                    isWalking = false;
                    changeState(BossState.ATTACK2_WINDUP);
                } else {
                    // Perseguir al jugador
                    chasePlayer();
                }
                break;

            case ATTACK1_WINDUP:
                isWalking = false;
                if (stateTimer >= ATTACK1_WINDUP) {
                    changeState(BossState.ATTACK1_ACTIVE);
                    executeAttack1();
                }
                break;

            case ATTACK1_ACTIVE:
                isWalking = false;
                if (!hitApplied) {
                    tryHitPlayer(ATTACK1_DAMAGE, ATTACK1_RANGE);
                }
                if (stateTimer >= ATTACK1_ACTIVE) {
                    changeState(BossState.ATTACK1_RECOVERY);
                    stopDash();
                }
                break;

            case ATTACK1_RECOVERY:
                isWalking = false;
                if (stateTimer >= ATTACK1_RECOVERY) {
                    nextAttack1Time = totalTime + ATTACK_COOLDOWN +
                        MathUtils.random(-ATTACK_COOLDOWN_JITTER, ATTACK_COOLDOWN_JITTER);
                    changeState(BossState.CHASE);
                }
                break;

            case ATTACK2_WINDUP:
                isWalking = false;
                if (stateTimer >= ATTACK2_WINDUP) {
                    changeState(BossState.ATTACK2_ACTIVE);
                }
                break;

            case ATTACK2_ACTIVE:
                isWalking = false;
                if (!hitApplied) {
                    tryHitPlayer(ATTACK2_DAMAGE, ATTACK2_RANGE);
                }
                if (stateTimer >= ATTACK2_ACTIVE) {
                    changeState(BossState.ATTACK2_RECOVERY);
                }
                break;

            case ATTACK2_RECOVERY:
                isWalking = false;
                if (stateTimer >= ATTACK2_RECOVERY) {
                    nextAttack2Time = totalTime + ATTACK_COOLDOWN +
                        MathUtils.random(-ATTACK_COOLDOWN_JITTER, ATTACK_COOLDOWN_JITTER);
                    changeState(BossState.CHASE);
                }
                break;
        }
    }

    private void changeState(BossState newState) {
        currentState = newState;
        stateTimer = 0f;
        hitApplied = false;

        // Log para debug
        Gdx.app.log("IudexGundyr", "Estado cambiado a: " + newState.name());
    }

    private void chasePlayer() {
        Vector2 velocity = body.getLinearVelocity();
        float direction = facingRight ? 1f : -1f;
        body.setLinearVelocity(direction * CHASE_SPEED, velocity.y);
    }

    private void executeAttack1() {
        // Dash hacia el jugador
        float direction = facingRight ? 1f : -1f;
        body.setLinearVelocity(direction * ATTACK1_DASH_SPEED, body.getLinearVelocity().y);

        // Sonido de ataque
        try {
            SoundManager.playSfx("assets/boss_attack1.wav");
        } catch (Exception e) {
            // Ignorar si no hay archivo de sonido
        }
    }

    private void stopDash() {
        // Frenar gradualmente el dash
        Vector2 velocity = body.getLinearVelocity();
        body.setLinearVelocity(velocity.x * 0.3f, velocity.y);
    }

    private void tryHitPlayer(int damage, float range) {
        if (playerRef == null || playerRef.isDead()) return;

        // Centro del boss
        float bossCenterX = position.x + width / 2f;
        float bossCenterY = position.y + height / 2f;

        // Centro del jugador
        float playerCenterX = playerRef.getPosition().x + playerRef.getWidth() / 2f;
        float playerCenterY = playerRef.getPosition().y + playerRef.getHeight() / 2f;

        // Distancias en ejes
        float dx = playerCenterX - bossCenterX;
        float dy = Math.abs(playerCenterY - bossCenterY);

        // Comprobar que el jugador esté al frente del boss
        boolean playerInFront = (facingRight && dx > 0) || (!facingRight && dx < 0);

        // Si está al frente y dentro del rango horizontal + vertical razonable
        if (playerInFront && Math.abs(dx) <= range * SCALE && dy <= height * 0.6f) {
            playerRef.takeDamage(damage);
            hitApplied = true;
            Gdx.app.log("IudexGundyr", "¡Golpeó al jugador (" + damage + " daño) desde " + (facingRight ? "derecha" : "izquierda") + "!");
        }
    }



    private float getDistanceToPlayer() {
        return Vector2.dst(
            position.x + width / 2f, position.y + height / 2f,
            playerRef.getPosition().x + playerRef.getWidth() / 2f,
            playerRef.getPosition().y + playerRef.getHeight() / 2f
        );
    }

    private boolean canUseAttack1() {
        return totalTime >= nextAttack1Time;
    }

    private boolean canUseAttack2() {
        return totalTime >= nextAttack2Time;
    }

    @Override
    public void takeDamage(int damage) {
        if (currentState == BossState.DEAD) return;

        hp = Math.max(0, hp - damage);

        if (hp <= 0) {
            hp = 0;
            currentState = BossState.DEAD; // 🔹 Indicamos que está en muerte
            deathTimer = 0f;                // 🔹 Reseteamos el tiempo de animación
            setActive(false);
            if (player != null) {
                int soulsReward = 50;
                player.addSouls(soulsReward);
            }
            Gdx.app.log("IudexGundyr", "¡Boss derrotado!");
        } else {
            Gdx.app.log("IudexGundyr", "Boss recibió " + damage + " daño. HP: " + hp + "/" + MAX_HP);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!active || currentState == BossState.DEAD) return;

        // Seleccionar animación apropiada - CORREGIDO
        TextureRegion currentFrame = getCurrentAnimationFrame();

        if (currentFrame != null) {
            // Dibujar con escala y orientación correcta
            float drawWidth = currentFrame.getRegionWidth() * SCALE;
            float drawHeight = currentFrame.getRegionHeight() * SCALE;

            if (facingRight) {
                batch.draw(currentFrame, position.x + drawWidth, position.y, -drawWidth, drawHeight);
            } else {
                batch.draw(currentFrame, position.x, position.y, drawWidth, drawHeight);
            }
        } else {
            // Fallback: usar textura base
            Gdx.app.log("IudexGundyr", "Usando fallback texture");
            if (facingRight) {
                batch.draw(texture, position.x + width, position.y, -width, height);
            } else {
                batch.draw(texture, position.x, position.y, width, height);
            }
        }



    }

    private TextureRegion getCurrentAnimationFrame() {
        switch (currentState) {
            case ATTACK1_WINDUP:
            case ATTACK1_ACTIVE:
            case ATTACK1_RECOVERY:
                return bossAttack1Anim != null
                    ? bossAttack1Anim.getKeyFrame(stateTimer, false)
                    : bossIdleAnim.getKeyFrame(totalTime, true);

            case ATTACK2_WINDUP:
            case ATTACK2_ACTIVE:
            case ATTACK2_RECOVERY:
                return bossAttack2Anim != null
                    ? bossAttack2Anim.getKeyFrame(stateTimer, false)
                    : bossIdleAnim.getKeyFrame(totalTime, true);

            case CHASE:
                if (isWalking && bossWalkAnim != null) {
                    return bossWalkAnim.getKeyFrame(walkAnimTimer, true);
                }
                return bossIdleAnim != null ? bossIdleAnim.getKeyFrame(totalTime, true) : null;

            case DEAD:
                return bossDeathAnim != null
                    ? bossDeathAnim.getKeyFrame(deathTimer, false)
                    : bossIdleAnim.getKeyFrame(totalTime, true);


            case IDLE:
            default:
                return bossIdleAnim != null ? bossIdleAnim.getKeyFrame(totalTime, true) : null;


        }
    }




    // === Getters ===
    @Override
    public int getCurrentHealth() {
        return hp;
    }

    @Override
    public int getMaxHealth() {
        return MAX_HP;
    }

    @Override
    public boolean isDead() {
        return currentState == BossState.DEAD;
    }

    public BossState getCurrentState() {
        return currentState;
    }

    // === Configuración externa ===
    public void setMaxHp(int maxHp) {
        this.MAX_HP = maxHp;
        if (hp > MAX_HP) hp = MAX_HP;
    }

    public void setScale(float scale) {
        this.SCALE = Math.max(0.1f, scale);
    }

    public void setAttack1Damage(int damage) {
        this.ATTACK1_DAMAGE = damage;
    }

    public void setAttack2Damage(int damage) {
        this.ATTACK2_DAMAGE = damage;
    }

    @Override
    public void dispose() {
        super.dispose();

        if (idleTexture != null) idleTexture.dispose();
        if (walkTexture != null) walkTexture.dispose();
        if (attack1Texture != null) attack1Texture.dispose();
        if (attack2Texture != null) attack2Texture.dispose();
    }
}
