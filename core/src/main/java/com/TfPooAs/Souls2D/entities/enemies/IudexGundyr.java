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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Boss Iudex Gundyr:
 * - IA con 2 ataques (ventanas: windup, activo, recovery).
 * - Ataque 1: rápido con desplazamiento (dash) y rango configurable.
 * - Ataque 2: más lento, sin desplazamiento, rango configurable.
 * - Vida base 300 (configurable).
 * - Animaciones de ataque: gundyrAtaque-Sheet.png (6 col), gundyrAtaque2-Sheet.png (7 col).
 * - Usa idle/walk de enemigo estándar como placeholder si no hay sprites propios.
 */
public class IudexGundyr extends Enemy {

    // === Configuración (modificables) ===
    private int MAX_HP = 300;
    private int hp = MAX_HP;

    // Escala visual del boss (ancho/alto). No modifica el collider del cuerpo.
    private float SCALE = 1.7f; // pedido mínimo: 1.7x

    private float ATTACK1_RANGE = 70f;  // en píxeles (base, se escalará visualmente)
    private float ATTACK2_RANGE = 90f;  // en píxeles (base, se escalará visualmente)

    private int ATTACK1_DAMAGE = 0;
    private int ATTACK2_DAMAGE = 0;

    // Ventanas más lentas (aumentadas ~35%)
    private float ATTACK1_WINDUP = 0.34f;
    private float ATTACK1_ACTIVE = 0.27f;
    private float ATTACK1_RECOVERY = 0.9f;
    private float ATTACK1_DASH_SPEED = 4.2f; // un poco más rápido por tamaño

    private float ATTACK2_WINDUP = 0.61f;
    private float ATTACK2_ACTIVE = 0.41f;
    private float ATTACK2_RECOVERY = 0.81f;

    private float CHASE_SPEED = 0.22f; // movimiento básico hacia el jugador
    private float ATTACK_COOLDOWN = 3.0f;
    private float ATTACK_COOLDOWN_JITTER = 0.6f; // variación aleatoria ±0.6s

    // === Estado ===
    private enum State { IDLE, MOVE, A1_WINDUP, A1_ACTIVE, A1_RECOVERY, A2_WINDUP, A2_ACTIVE, A2_RECOVERY, DEAD }
    private State state = State.IDLE;
    private float stateTimer = 0f;
    private boolean facingRight = true;
    private boolean grounded = true; // sincronizado por GameScreen via ContactListener del Enemy base

    private boolean hitApplied = false; // para no aplicar daño múltiple por ventana activa

    // Referencias
    private final Player playerRef; // guardamos referencia propia
    private final Body body;        // reutilizamos el cuerpo creado por Enemy

    // Animations
    private Animation<TextureRegion> attack1Anim; // 6 frames
    private Animation<TextureRegion> attack2Anim; // 7 frames
    private Animation<TextureRegion> idleAnim;    // placeholder
    private Animation<TextureRegion> walkAnim;    // placeholder

    // Control visual
    private float animTime = 0f;

    public IudexGundyr(World world, float x, float y, Player player) {
        super(world, x, y, player);
        this.playerRef = player;
        this.body = getBody();

        // Cargar anims de ataque (si no existen, se ignora y no crashea)
        AnimationUtils.AnimWithTexture a1 = AnimationUtils.createFromSpritesheetIfExists(
                "gundyrAtaque-Sheet.png", 6, 1, 0.06f, Animation.PlayMode.NORMAL);
        if (a1 != null) this.attack1Anim = a1.animation;

        AnimationUtils.AnimWithTexture a2 = AnimationUtils.createFromSpritesheetIfExists(
                "gundyrAtaque2-Sheet.png", 7, 1, 0.06f, Animation.PlayMode.NORMAL);
        if (a2 != null) this.attack2Anim = a2.animation;

        // Placeholders de idle/walk (usar enemigo estándar para mantener visibilidad)
        AnimationUtils.AnimWithTexture idlePair = AnimationUtils.createFromSpritesheetIfExists(
                "gundyrWalk.png", 1, 1, 0.2f, Animation.PlayMode.LOOP);
        if (idlePair != null) this.idleAnim = idlePair.animation;
        AnimationUtils.AnimWithTexture walkPair = AnimationUtils.createFromSpritesheetIfExists(
                "gundyrWalk.png", 4, 1, 0.10f, Animation.PlayMode.LOOP);
        if (walkPair != null) this.walkAnim = walkPair.animation;

        // Asegurar dimensiones básicas a partir de idle o texture base de Enemy
        TextureRegion baseFrame = (idleAnim != null)
                ? idleAnim.getKeyFrame(0)
                : (walkAnim != null ? walkAnim.getKeyFrame(0) : new TextureRegion(texture));
        this.width = baseFrame.getRegionWidth();
        this.height = baseFrame.getRegionHeight();
    }

    // ===== API pública para configuración externa =====
    public void setMaxHp(int value) { this.MAX_HP = value; if (hp > MAX_HP) hp = MAX_HP; }
    public void setAttack1Range(float px) { this.ATTACK1_RANGE = px; }
    public void setAttack2Range(float px) { this.ATTACK2_RANGE = px; }
    public void setScale(float scale) { this.SCALE = Math.max(0.1f, scale); }

    @Override
    public void update(float delta) {
        if (state == State.DEAD) return;

        animTime += delta;
        stateTimer += delta;

        // Sync posicion visual con el cuerpo
        position.set(
                body.getPosition().x * Constants.PPM - width / 2f,
                body.getPosition().y * Constants.PPM - height / 2f
        );

        // Lógica de detección básica
        if (playerRef != null && !playerRef.isDead()) {
            float dist = Vector2.dst(position.x, position.y, playerRef.getPosition().x, playerRef.getPosition().y);
            facingRight = playerRef.getPosition().x > position.x;

            switch (state) {
                case IDLE:
                case MOVE:
                    // Decidir si perseguir/atacar de forma aleatoria (permitiendo repetición)
                    boolean canA1 = dist <= (ATTACK1_RANGE * SCALE) && canUseAttack1();
                    boolean canA2 = dist <= (ATTACK2_RANGE * SCALE) && canUseAttack2();
                    if (canA1 && canA2) {
                        // Ponderar por distancia: más cerca favorece A1, más lejos favorece A2
                        float a1Edge = ATTACK1_RANGE * SCALE;
                        float a2Edge = ATTACK2_RANGE * SCALE;
                        float t = MathUtils.clamp((dist - a1Edge) / Math.max(0.0001f, (a2Edge - a1Edge)), 0f, 1f);
                        float probA2 = 0.35f + 0.65f * t; // cerca: ~35% A2, lejos: ~100% A2
                        if (MathUtils.randomBoolean(probA2)) {
                            enter(State.A2_WINDUP);
                        } else {
                            enter(State.A1_WINDUP);
                        }
                    } else if (canA2) {
                        enter(State.A2_WINDUP);
                    } else if (canA1) {
                        enter(State.A1_WINDUP);
                    } else {
                        // Perseguir
                        pursue(delta);
                    }
                    break;
                case A1_WINDUP:
                    if (stateTimer >= ATTACK1_WINDUP) {
                        enter(State.A1_ACTIVE);
                        // iniciar dash en ACTIVE
                        float dir = facingRight ? 1f : -1f;
                        body.setLinearVelocity(dir * ATTACK1_DASH_SPEED, body.getLinearVelocity().y);
                    }
                    break;
                case A1_ACTIVE:
                    if (!hitApplied) tryApplyHit(ATTACK1_DAMAGE, ATTACK1_RANGE);
                    if (stateTimer >= ATTACK1_ACTIVE) {
                        // frenar
                        body.setLinearVelocity(body.getLinearVelocity().x * 0.2f, body.getLinearVelocity().y);
                        enter(State.A1_RECOVERY);
                    }
                    break;
                case A1_RECOVERY:
                    if (stateTimer >= ATTACK1_RECOVERY) {
                        enter(State.MOVE);
                        nextA1Time = timeSinceStart + ATTACK_COOLDOWN + MathUtils.random(-ATTACK_COOLDOWN_JITTER, ATTACK_COOLDOWN_JITTER);
                        if (nextA1Time < 0f) nextA1Time = 0f;
                    }
                    break;
                case A2_WINDUP:
                    if (stateTimer >= ATTACK2_WINDUP) enter(State.A2_ACTIVE);
                    break;
                case A2_ACTIVE:
                    if (!hitApplied) tryApplyHit(ATTACK2_DAMAGE, ATTACK2_RANGE);
                    if (stateTimer >= ATTACK2_ACTIVE) enter(State.A2_RECOVERY);
                    break;
                case A2_RECOVERY:
                    if (stateTimer >= ATTACK2_RECOVERY) {
                        enter(State.MOVE);
                        nextA2Time = timeSinceStart + ATTACK_COOLDOWN + MathUtils.random(-ATTACK_COOLDOWN_JITTER, ATTACK_COOLDOWN_JITTER);
                        if (nextA2Time < 0f) nextA2Time = 0f;
                    }
                    break;
                case DEAD:
                    break;
            }
        }

        // Contadores de cooldown global
        timeSinceStart += delta;
    }

    private float timeSinceStart = 0f;
    private float nextA1Time = 0f;
    private float nextA2Time = 0f;

    private boolean canUseAttack1() { return timeSinceStart >= nextA1Time; }
    private boolean canUseAttack2() { return timeSinceStart >= nextA2Time; }

    private void pursue(float delta) {
        if (playerRef == null) return;
        Vector2 vel = body.getLinearVelocity();
        float dir = playerRef.getPosition().x > position.x ? 1f : -1f;
        body.setLinearVelocity(dir * CHASE_SPEED, vel.y);
        state = State.MOVE;
    }

    private void enter(State s) {
        state = s;
        stateTimer = 0f;
        hitApplied = false;
    }

    private void tryApplyHit(int damage, float range) {
        if (playerRef == null || playerRef.isDead()) return;
        float dist = Vector2.dst(position.x, position.y, playerRef.getPosition().x, playerRef.getPosition().y);
        float effectiveRange = range * SCALE * 1.1f;
        if (dist <= effectiveRange) {
            playerRef.takeDamage(damage);
            hitApplied = true;
        }
    }

    // Daño recibido del jugador
    @Override
    public void takeDamage(int damage) {
        if (state == State.DEAD) return;
        hp -= Math.max(0, damage);
        if (hp <= 0) {
            hp = 0;
            state = State.DEAD;
            setActive(false);
        }
    }

    @Override
    public boolean isDead() {
        return state == State.DEAD;
    }

    @Override
    public int getCurrentHealth() {
        return hp;
    }

    @Override
    public int getMaxHealth() {
        return MAX_HP;
    }

    // Render personalizado con selección de animación
    @Override
    public void render(SpriteBatch batch) {
        if (!active || state == State.DEAD) return;

        TextureRegion frame = null;
        switch (state) {
            case A1_WINDUP:
            case A1_ACTIVE:
            case A1_RECOVERY:
                if (attack1Anim != null) frame = attack1Anim.getKeyFrame(stateTimer);
                break;
            case A2_WINDUP:
            case A2_ACTIVE:
            case A2_RECOVERY:
                if (attack2Anim != null) frame = attack2Anim.getKeyFrame(stateTimer);
                break;
            case MOVE:
                if (walkAnim != null) frame = walkAnim.getKeyFrame(animTime);
                break;
            case IDLE:
            default:
                if (idleAnim != null) frame = idleAnim.getKeyFrame(animTime);
                break;
        }
        if (frame == null) {
            // fallback: usar textura base del Enemy
            batch.draw(texture, position.x, position.y, width, height);
            return;
        }
        if (facingRight) {
            // dibujar mirando a la derecha (flip X)
            batch.draw(frame, position.x + width, position.y, -width, height);
        } else {
            batch.draw(frame, position.x, position.y, width, height);
        }
    }

    // Integración con el ContactListener del juego
    @Override
    public void setGrounded(boolean grounded) { this.grounded = grounded; }
    @Override
    public boolean isGrounded() { return grounded; }
}
