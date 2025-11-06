package com.TfPooAs.Souls2D.entities;

import com.TfPooAs.Souls2D.utils.AnimationUtils;
import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;


import java.util.List;

/**
 * Enemigo a distancia que dispara proyectiles hacia el jugador.
 */
public class Enemy2 extends Entity {
    private Body body;
    private final World world;
    private final Player player;
    private final List<EnemyProjectile> projectileSink;

    // Textura 1x1 para dibujar barras con SpriteBatch (compartida)
    private static Texture whiteTex;

    // Vida
    private int maxHealth = 30;
    private int currentHealth = 30;
    private boolean isDead = false;

    // Estados
    private boolean isGrounded = true;
    private boolean facingRight = true;
    private boolean playerDetected = false;

    // IA y disparo
    private final float DETECTION_RANGE = 500f; // píxeles
    private final float MIN_SHOOT_DISTANCE = 0f; // no dispara demasiado cerca
    private final float MOVE_SPEED = 0.15f; // se reposiciona suavemente
    private final float SHOOT_COOLDOWN = 3f;
    private float shootCooldownTimer = 0f;
    private final int PROJECTILE_DAMAGE = 25;
    private final float PROJECTILE_SPEED = 5f; // unidades Box2D m/s
    private final float PROJECTILE_LIFETIME = 4.0f;

    // Animaciones
    private Animation<TextureRegion> idleAnim;
    private Texture idleSheetTexture;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;
    private Texture walkSheetTexture;
    private Texture attackSheetTexture;
    private float stateTime = 0f;
    private float attackTimer = 0f;

    private boolean isAttacking = false;

    public Enemy2(World world, float x, float y, Player player, List<EnemyProjectile> projectileSink) {
        super(x, y, "enemy2-walk.png"); // como fallback para tamaño; luego usamos anims
        this.world = world;
        this.player = player;
        this.projectileSink = projectileSink;

        // Animaciones
        AnimationUtils.AnimWithTexture idlePair = AnimationUtils.createFromSpritesheetIfExists(
            "enemy2-idle.png", 1, 1, 0.2f, Animation.PlayMode.LOOP);
        if (idlePair != null) {
            this.idleAnim = idlePair.animation;
            this.idleSheetTexture = idlePair.texture;
        }
        AnimationUtils.AnimWithTexture walkPair = AnimationUtils.createFromSpritesheetIfExists(
            "enemy2-walk.png", 4, 1, 0.12f, Animation.PlayMode.LOOP);
        if (walkPair != null) {
            this.walkAnim = walkPair.animation;
            this.walkSheetTexture = walkPair.texture;
        }
        AnimationUtils.AnimWithTexture attackPair = AnimationUtils.createFromSpritesheetIfExists(
            "enemy2-attack.png", 5, 1, 0.10f, Animation.PlayMode.NORMAL);
        if (attackPair != null) {
            this.attackAnim = attackPair.animation;
            this.attackSheetTexture = attackPair.texture;
        }

        // Determinar tamaño visual a partir de alguna anim
        TextureRegion baseFrame = (walkAnim != null)
            ? walkAnim.getKeyFrame(0)
            : (attackAnim != null ? attackAnim.getKeyFrame(0) : new TextureRegion(texture));
        this.width = baseFrame.getRegionWidth();
        this.height = baseFrame.getRegionHeight();

        createBody(x, y, width, height);
    }


    private void createBody(float x, float y, float frameWidth, float frameHeight) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / Constants.PPM, y / Constants.PPM);
        bdef.fixedRotation = true;
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        float spriteWidth = 61f;
        float spriteHeight = 59f;
        float halfW = (spriteWidth / 2f) / Constants.PPM;
        float halfH = (spriteHeight / 2f) / Constants.PPM;
        shape.setAsBox(halfW, halfH);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0f;
        fdef.filter.categoryBits = Constants.BIT_ENEMY;
        fdef.filter.maskBits = Constants.BIT_GROUND; // solo choca con suelo
        body.createFixture(fdef).setUserData("enemy");
        shape.dispose();
    }

    @Override
    public void update(float delta) {
        if (isDead) return;

        stateTime += delta;
        if (shootCooldownTimer > 0f) shootCooldownTimer -= delta;
        if (isAttacking) {
            attackTimer += delta;
            if (attackAnim == null || attackAnim.isAnimationFinished(attackTimer)) {
                isAttacking = false;
                attackTimer = 0f;
            }
        }

        detectPlayer();
        updateAI(delta);

        // Sync visual con física
        position.set(
                body.getPosition().x * Constants.PPM - width / 2f,
                body.getPosition().y * Constants.PPM - height / 2f
        );
    }

    private void detectPlayer() {
        if (player == null || player.isDead()) { playerDetected = false; return; }
        float dist = getDistanceToPlayer();
        playerDetected = dist <= DETECTION_RANGE;
        if (playerDetected && !isAttacking) {
            facingRight = player.getPosition().x > position.x;
        }
    }

    private float getDistanceToPlayer() {
        return Vector2.dst(
                position.x + width / 2f, position.y + height / 2f,
                player.getPosition().x + player.getWidth() / 2f,
                player.getPosition().y + player.getHeight() / 2f
        );
    }

    private void updateAI(float delta) {
        if (!playerDetected) return;
        float dist = getDistanceToPlayer();
        Vector2 vel = body.getLinearVelocity();

        // mantener distancia: si muy cerca, retrocede; si muy lejos, acércate lento
        float desiredDir = 0f;
        if (dist < MIN_SHOOT_DISTANCE * 0.8f) desiredDir = facingRight ? 1f : -1f; // alejarse
        else if (dist > MIN_SHOOT_DISTANCE * 1.2f) desiredDir = facingRight ? -1f : 1f; // acercarse un poco

        body.setLinearVelocity(desiredDir * MOVE_SPEED, vel.y);

        // Disparo si está a media distancia
        if (dist > MIN_SHOOT_DISTANCE && shootCooldownTimer <= 0f) {
            shoot();
        }
    }

    private void shoot() {
        isAttacking = true;
        attackTimer = 0f;
        shootCooldownTimer = SHOOT_COOLDOWN;

        // CALCULAR DIRECCIÓN HACIA EL JUGADOR (en lugar de solo horizontal)
        float enemyCenterX = position.x + width / 2f;
        float enemyCenterY = position.y + height / 2f;
        float playerCenterX = player.getPosition().x + player.getWidth() / 2f;
        float playerCenterY = player.getPosition().y + player.getHeight() / 2f;

        // Vector dirección normalizado hacia el jugador
        float dirX = playerCenterX - enemyCenterX;
        float dirY = playerCenterY - enemyCenterY;

        // Normalizar el vector para que tenga longitud 1
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length > 0) {
            dirX /= length;
            dirY /= length;
        } else {
            // Si están en la misma posición, disparar horizontalmente
            dirX = facingRight ? 1f : -1f;
            dirY = 0f;
        }

        // spawn cerca de la "mano"
        float spawnX = position.x + width / 2f + (facingRight ? width * 0.3f : -width * 0.3f);
        float spawnY = position.y + height * 0.55f;

        EnemyProjectile p = new EnemyProjectile(
            world,
            spawnX,
            spawnY,
            dirX,  // dirección X normalizada hacia el jugador
            dirY,  // dirección Y normalizada hacia el jugador
            PROJECTILE_SPEED,
            PROJECTILE_DAMAGE,
            PROJECTILE_LIFETIME
        );
        // set user data en el body para localizarlo en ContactListener
        p.getBody().setUserData(p);
        projectileSink.add(p);
    }


    @Override
    public void render(SpriteBatch batch) {
        if (!active || isDead) return;

        TextureRegion frame;
        if (isAttacking && attackAnim != null) frame = attackAnim.getKeyFrame(attackTimer);
        else if (walkAnim != null) frame = walkAnim.getKeyFrame(stateTime);
        else frame = new TextureRegion(texture);

        if (!facingRight) batch.draw(frame, position.x, position.y, width, height);
        else batch.draw(frame, position.x + width, position.y, -width, height);

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


    public void setGrounded(boolean grounded) { this.isGrounded = grounded; }
    public boolean isGrounded() { return isGrounded; }

    public void takeDamage(int amount) {
        if (isDead) return;
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            setActive(false);
            System.out.println("¡Enemigo a distancia eliminado!");
        } else {
            System.out.println("Enemigo a distancia recibió " + amount + " de daño. Vida: " + currentHealth + "/" + maxHealth);
        }
    }


    public boolean isDead() { return isDead; }
    public Body getBody() { return body; }

    @Override
    public void dispose() {
        super.dispose();
        if (walkSheetTexture != null) walkSheetTexture.dispose();
        if (attackSheetTexture != null) attackSheetTexture.dispose();
    }
}
