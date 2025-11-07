package com.TfPooAs.Souls2D.entities;

import com.TfPooAs.Souls2D.screens.GameScreen;
import com.TfPooAs.Souls2D.utils.AnimationUtils;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

import com.TfPooAs.Souls2D.utils.Constants;
import com.TfPooAs.Souls2D.systems.SoundManager;


public class Enemy extends Entity {
    private Body body;
    private World world;
    private Player player; // referencia al jugador para detectarlo
    private boolean dead;
    protected GameScreen gameScreen;

    // Textura 1x1 para dibujar barras con SpriteBatch (compartida)
    private static Texture whiteTex;

    // === Sistema de vida ===
    private int maxHealth = 70;
    private int currentHealth = 70;
    private boolean isDead = false;

    // === Almas ===
    private int soulValue = 20;


    // === Físicas similares al jugador ===
    private boolean isGrounded = true;
    private float jumpForce = 0.8f; // un poco menos que el player
    private final float MAX_VELOCITY_X = 1.5f; // límite de velocidad horizontal

    // === Sistema de IA y detección ===
    private final float DETECTION_RANGE = 200f; // rango de detección en píxeles
    private final float ATTACK_RANGE = 60f; // rango de ataque en píxeles
    private boolean playerDetected = false;
    private boolean facingRight = true;

    // === Sistema de ataque ===
    private Texture attackTexture;
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private final float ATTACK_DURATION = 0.5f; // duración del ataque
    private final float ATTACK_COOLDOWN = 1.5f; // tiempo entre ataques
    private float attackCooldownTimer = 0f;
    private final float ATTACK_FORCE = 2.5f; // fuerza del dash de ataque
    private final int ATTACK_DAMAGE = 15; // daño que hace al jugador

    // === Movimiento mejorado ===
    private final float MOVE_SPEED = 0.25f; // velocidad de persecución
    private final float JUMP_THRESHOLD = 0.5f; // altura mínima para intentar saltar
    private float jumpCooldown = 0f; // cooldown para saltos
    private final float JUMP_COOLDOWN_TIME = 1f; // tiempo entre saltos

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> walkAnim;
    private Texture idleSheetTexture;
    private Texture walkSheetTexture;
    private Texture attackSheetTexture;

    // Timers de animación
    private float stateTime = 0f;      // para idle/caminar
    // attackTimer ya existe arriba y se usa para el ataque

    // Dirección bloqueada durante el ataque
    private int attackDirection = 1;   // -1 izquierda, 1 derecha


    public Enemy(World world, float x, float y, Player player) {
        super(x, y, "enemy-idle.png");
        this.world = world;
        this.player = player;
        this.attackTexture = new Texture("enemy-attack.png");
        this.gameScreen = gameScreen;
        this.soulValue = soulValue;


        // === Animaciones ===

        AnimationUtils.AnimWithTexture idlePair = AnimationUtils.createFromSpritesheetIfExists(
            "enemy-idle.png", 1, 1, 0.2f, Animation.PlayMode.LOOP);
        if (idlePair != null) {
            this.idleAnim = idlePair.animation;
            this.idleSheetTexture = idlePair.texture;
        }

        // Ataque (11 columnas)
        AnimationUtils.AnimWithTexture attackPair = AnimationUtils.createFromSpritesheetIfExists(
            "enemy-attack.png", 11, 1, 0.07f, Animation.PlayMode.NORMAL);
        if (attackPair != null) {
            this.attackAnim = attackPair.animation;
            this.attackSheetTexture = attackPair.texture;
        }

        // Caminar (4 columnas)
        AnimationUtils.AnimWithTexture walkPair = AnimationUtils.createFromSpritesheetIfExists(
            "enemy-walk.png", 4, 1, 0.10f, Animation.PlayMode.LOOP);
        if (walkPair != null) {
            this.walkAnim = walkPair.animation;
            this.walkSheetTexture = walkPair.texture;
        }


        // Determinar ancho/alto de frame base (usa idle si existe, si no usa walk)
        TextureRegion baseFrame = (idleAnim != null)
            ? idleAnim.getKeyFrame(0)
            : (walkAnim != null ? walkAnim.getKeyFrame(0) : new TextureRegion(texture));

        this.width = baseFrame.getRegionWidth();
        this.height = baseFrame.getRegionHeight();


        // Crear el cuerpo del tamaño exacto del frame
        createBody(x, y, width, height);
    }



    private void createBody(float x, float y, float frameWidth, float frameHeight) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / Constants.PPM, y / Constants.PPM);
        bdef.fixedRotation = true;
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();

        // === Definir tamaño físico exacto (en píxeles del sprite) ===
        // Si tu enemigo mide 64x64 píxeles en el sprite
        float spriteWidth = 62f;
        float spriteHeight = 59f;

        // Convertir de píxeles a metros (Box2D usa metros)
        float halfW = (spriteWidth / 2f) / Constants.PPM;
        float halfH = (spriteHeight / 2f) / Constants.PPM;

        // Crear la forma del cuerpo con esas dimensiones
        shape.setAsBox(halfW, halfH);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0f;
        fdef.filter.categoryBits = Constants.BIT_ENEMY;
        fdef.filter.maskBits = Constants.BIT_GROUND;

        body.createFixture(fdef).setUserData("enemy");
        shape.dispose();
    }

    public int getSoulValue() {
        return soulValue;
    }


    @Override
    public void update(float delta) {
        if (isDead) return;

        // Avanzar timers de animación
        stateTime += delta;

        updateTimers(delta);
        detectPlayer();
        updateAI(delta);
        limitVelocity(); // Limitar velocidad como el player

        // Sincronizar posición visual con física
        position.set(
            body.getPosition().x * Constants.PPM - width / 2f,
            body.getPosition().y * Constants.PPM - height / 2f
        );
    }

    private void updateTimers(float delta) {
        // Actualizar timer de ataque
        if (isAttacking) {
            attackTimer += delta;
            boolean finishedByAnim = (attackAnim != null) && attackAnim.isAnimationFinished(attackTimer);
            boolean finishedByTime = attackTimer >= ATTACK_DURATION; // respaldo por si no hay anim
            if (finishedByAnim || finishedByTime) {
                endAttack();
            }
        }

        // Actualizar cooldown de ataque
        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }

        // Actualizar cooldown de salto
        if (jumpCooldown > 0) {
            jumpCooldown -= delta;
        }
    }

    private void detectPlayer() {
        if (player.isDead()) {
            playerDetected = false;
            return;
        }

        float distanceToPlayer = getDistanceToPlayer();
        playerDetected = distanceToPlayer <= DETECTION_RANGE;

        // Actualizar dirección hacia el jugador (no cambiar durante un ataque)
        if (playerDetected) {
            Vector2 playerPos = player.getPosition();
            if (!isAttacking) {
                facingRight = playerPos.x > position.x;
            }
        }
    }

    private void updateAI(float delta) {
        if (!playerDetected) return;

        float distanceToPlayer = getDistanceToPlayer();
        Vector2 vel = body.getLinearVelocity();

        if (isAttacking) {
            // Durante el ataque, mantener el dash
            return;
        }

        if (distanceToPlayer <= ATTACK_RANGE && attackCooldownTimer <= 0) {
            // Atacar si está en rango y no hay cooldown
            startAttack();
        } else if (distanceToPlayer > ATTACK_RANGE) {
            // Perseguir al jugador con física mejorada
            Vector2 playerPos = player.getPosition();
            float direction = playerPos.x > position.x ? 1 : -1;

            // Movimiento horizontal con control de velocidad
            float targetVelX = direction * MOVE_SPEED;
            body.setLinearVelocity(targetVelX, vel.y);

            // Intentar saltar si el jugador está más alto y hay obstáculos
            boolean shouldJump = shouldJumpToReachPlayer(playerPos);
            if (shouldJump && isGrounded && jumpCooldown <= 0) {
                jump();
            }
        } else {
            // Detenerse si está esperando cooldown pero mantener posición
            body.setLinearVelocity(0, vel.y);
        }
    }

    private boolean shouldJumpToReachPlayer(Vector2 playerPos) {
        // Saltar si el jugador está significativamente más alto
        float heightDifference = playerPos.y - position.y;
        return heightDifference > JUMP_THRESHOLD * Constants.PPM;
    }

    private void jump() {
        body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
        jumpCooldown = JUMP_COOLDOWN_TIME;
        isGrounded = false; // inmediatamente marcar como no en suelo
        System.out.println("¡Enemigo saltando!");
    }

    private void limitVelocity() {
        Vector2 vel = body.getLinearVelocity();

        // Limitar velocidad horizontal
        if (Math.abs(vel.x) > MAX_VELOCITY_X) {
            float limitedVelX = Math.signum(vel.x) * MAX_VELOCITY_X;
            body.setLinearVelocity(limitedVelX, vel.y);
        }

        // Detener movimiento horizontal si la velocidad Y es baja y no se está moviendo
        if (!isAttacking && Math.abs(vel.y) < 0.01f && Math.abs(vel.x) < 0.1f) {
            body.setLinearVelocity(0, vel.y);
        }
    }

    private float getDistanceToPlayer() {
        Vector2 playerPos = player.getPosition();
        return Vector2.dst(position.x, position.y, playerPos.x, playerPos.y);
    }

    private void startAttack() {
        isAttacking = true;
        attackTimer = 0f; // reiniciar anim de ataque
        attackCooldownTimer = ATTACK_COOLDOWN;

        // Bloquear dirección al inicio del ataque
        Vector2 playerPos = player.getPosition();
        attackDirection = playerPos.x > position.x ? 1 : -1;
        facingRight = attackDirection == 1;

        // Dash hacia el jugador (más controlado) usando la dirección bloqueada
        body.setLinearVelocity(attackDirection * ATTACK_FORCE, body.getLinearVelocity().y);

        // SFX: slash del enemigo
        SoundManager.playSfx("assets/enemy_slash.wav");

        System.out.println("¡Enemigo atacando!");
    }

    private void endAttack() {
        isAttacking = false;
        attackTimer = 0f;

        // Detener el dash gradualmente
        Vector2 vel = body.getLinearVelocity();
        body.setLinearVelocity(vel.x * 0.3f, vel.y);

        // Verificar si golpeó al jugador
        checkPlayerHit();
    }

    private void checkPlayerHit() {
        if (player.isDead()) return;

        float distanceToPlayer = getDistanceToPlayer();
        if (distanceToPlayer <= ATTACK_RANGE * 1.2f) { // un poco más de rango para el golpe
            player.takeDamage(ATTACK_DAMAGE);
            System.out.println("¡El enemigo golpeó al jugador!");
        }
    }

    // Método para recibir daño del jugador
    public void takeDamage(int damage) {
        if (isDead) return;

        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            setActive(false); // desactivar la entidad
            onDeath();
            System.out.println("¡Enemigo eliminado!");
        } else {
            System.out.println("Enemigo recibió " + damage + " de daño. Vida: " + currentHealth + "/" + maxHealth);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!active || isDead) return;

        TextureRegion currentFrame;

        if (isAttacking && attackAnim != null) {
            currentFrame = attackAnim.getKeyFrame(attackTimer);
        } else if (playerDetected && walkAnim != null) {
            currentFrame = walkAnim.getKeyFrame(stateTime);
        } else if (idleAnim != null) {
            currentFrame = idleAnim.getKeyFrame(stateTime);
        } else {
            currentFrame = new TextureRegion(texture);
        }

        if (!facingRight)
            batch.draw(currentFrame, position.x, position.y, width, height);
        else
            batch.draw(currentFrame, position.x + width, position.y, -width, height);

        // === Barra de vida encima del enemigo ===
        // Lazy-init de textura blanca 1x1 para rectángulos
        if (whiteTex == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(1, 1, 1, 1);
            pm.fill();
            whiteTex = new Texture(pm);
            pm.dispose();
        }

        float barWidth = Math.max(30f, width); // al menos 30px de ancho
        float barHeight = 6f;
        float barX = position.x + (width - barWidth) / 2f;
        float barY = position.y + height + 6f; // un poco por encima
        float ratio = Math.max(0f, Math.min(1f, (float) currentHealth / (float) maxHealth));

        // Guardar color actual y usar tintes para "pintar" la whiteTex
        Color prev = batch.getColor().cpy();

        // Borde
        batch.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
        batch.draw(whiteTex, barX - 1, barY - 1, barWidth + 2, barHeight + 2);
        // Fondo interior
        batch.setColor(new Color(0.07f, 0.07f, 0.07f, 1f));
        batch.draw(whiteTex, barX, barY, barWidth, barHeight);
        // Relleno de vida
        batch.setColor(new Color(0.75f, 0.15f, 0.10f, 1f));
        batch.draw(whiteTex, barX, barY, barWidth * ratio, barHeight);

        // Restaurar el color del batch para no afectar otros renders
        batch.setColor(prev);
    }

    // === Métodos para el sistema de físicas ===
    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    // === Getters ===
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDead() { return isDead; }
    public boolean isAttacking() { return isAttacking; }
    public boolean isPlayerDetected() { return playerDetected; }
    public Body getBody() { return body; }

    protected void onDeath() {}

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public Vector2 getPosition() {
        return position;
    }


    @Override
    public void dispose() {
        super.dispose();
        if (idleSheetTexture != null) {
            idleSheetTexture.dispose();
        }
        if (attackSheetTexture != null) {
            attackSheetTexture.dispose();
        }
        if (walkSheetTexture != null) {
            walkSheetTexture.dispose();
        }
    }
}
