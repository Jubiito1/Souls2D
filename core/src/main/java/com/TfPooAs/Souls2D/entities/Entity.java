package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Clase base para entidades jugables y enemigos.
 * He añadido campos y utilidades que suelen necesitar los enemigos:
 * - soporte para Texture o TextureRegion
 * - campos de Box2D (World, Body) y helpers para crear cuerpos
 * - vida, daño, velocidad, rangos de detección/ataque
 * - métodos comunes: takeDamage, heal, isAlive, applyKnockback, setPosition sincronizado con body
 *
 * Comentarios en español y métodos protegidos que pueden sobrescribirse en subclases.
 */
public abstract class Entity {
    // Posición lógica (en coordenadas del juego)
    protected Vector2 position;
    // Tamaño (pixeles)
    protected float width, height;

    // Render
    protected Texture texture;             // usado si se pasa String
    protected TextureRegion region;        // preferible para animaciones/spritesheets
    protected Animation<TextureRegion> animation; // opcional, si la subclase usa animaciones
    protected float stateTime = 0f;        // tiempo usado para animaciones

    // Activación / ciclo de vida
    protected boolean active = true;

    // Box2D
    protected World world;
    protected Body body;

    // Estadísticas y comportamiento básico para enemigos
    protected float speed = 100f;          // px / s (ajustar según escala)
    protected float detectionRange = 300f; // px
    protected float attackRange = 40f;     // px
    protected int damage = 10;
    protected int health = 100;
    protected int maxHealth = 100;

    protected boolean facingRight = true;

    // Constructores

    // No-arg para flexibilidad (evita errores de no-arg constructor)
    public Entity() {
        this.position = new Vector2();
        this.width = 0;
        this.height = 0;
    }

    // Constructor que recibe ruta de textura (rápido, no recomendado para proyectos grandes)
    public Entity(float x, float y, String texturePath) {
        this.position = new Vector2(x, y);
        this.texture = new Texture(Gdx.files.internal(texturePath));
        this.region = new TextureRegion(this.texture);
        this.width = region.getRegionWidth();
        this.height = region.getRegionHeight();
    }

    // Constructor que recibe TextureRegion (recomendado)
    public Entity(float x, float y, TextureRegion region) {
        this.position = new Vector2(x, y);
        this.region = region;
        if (region != null) {
            this.texture = region.getTexture();
            this.width = region.getRegionWidth();
            this.height = region.getRegionHeight();
        } else {
            this.width = 0;
            this.height = 0;
        }
    }

    // Constructor que recibe TextureRegion y World (si querés crear el body en el ctor)
    public Entity(float x, float y, TextureRegion region, World world) {
        this(x, y, region);
        this.world = world;
    }

    // --- Métodos a sobreescribir por subclases ---

    /**
     * Actualizar lógica. Las subclases deben implementar.
     * Recomiendo llamar super.updateCommon(delta) si necesitás que stateTime avance.
     */
    public abstract void update(float delta);

    /**
     * Dibuja la entidad. Subclases pueden sobrescribir, o llamar super.render(batch)
     */
    public void render(SpriteBatch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        if (!active) return;

        if (animation != null) {
            TextureRegion frame = animation.getKeyFrame(stateTime, true);
            batch.draw(frame, position.x, position.y, width, height);
        } else if (region != null) {
            batch.draw(region, position.x, position.y, width, height);
        } else if (texture != null) {
            batch.draw(texture, position.x, position.y, width, height);
        }
    }

    // --- Helpers comunes para enemigos y otras entidades ---

    /**
     * Crea un body simple rectangular dinámico con centro en position.
     * hx/hy son la mitad del ancho/alto que quieras en unidades del mundo (convierte pix->metros si hace falta).
     * Devuelve el Body creado y lo guarda en this.body.
     */
    protected Body createBody(float hx, float hy, BodyDef.BodyType type, boolean fixedRotation) {
        if (world == null) return null;

        BodyDef bdef = new BodyDef();
        bdef.type = type;
        bdef.position.set(position.x, position.y);
        bdef.fixedRotation = fixedRotation;
        this.body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(hx, hy);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        this.body.createFixture(fdef);

        shape.dispose();
        return this.body;
    }

    /**
     * Sincroniza la posición lógica con el body (si lo hay).
     * Llamar desde update() si la física mueve el body.
     */
    protected void syncFromBody() {
        if (body != null) {
            Vector2 pos = body.getPosition();
            position.set(pos.x, pos.y);
        }
    }

    /**
     * Coloca la posición (y sincroniza el body si existe).
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        if (body != null) {
            body.setTransform(x, y, body.getAngle());
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    // --- Salud y daño ---

    public void takeDamage(int amount) {
        if (!active) return;
        health -= amount;
        if (health <= 0) {
            health = 0;
            die();
        }
    }

    public void heal(int amount) {
        if (!active) return;
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    public boolean isAlive() {
        return active && health > 0;
    }

    protected void die() {
        active = false;
        // por defecto solo desactiva; las subclases pueden sobreescribir para efectos/loot/etc.
        // Si usás Box2D, considera programar la destrucción del body en el world-step.
    }

    // --- Movimiento / física auxiliar ---

    /**
     * Aplica un impulso/knockback si existe body.
     * dir asumida normalizada.
     */
    public void applyKnockback(Vector2 dir, float force) {
        if (body != null && dir != null) {
            body.applyLinearImpulse(dir.scl(force), body.getWorldCenter(), true);
        } else {
            // fallback: mover la posición manualmente
            position.add(dir.scl(force));
        }
    }

    // --- Setters/Getters de campos útiles ---

    public void setWorld(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public Body getBody() {
        return body;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setDetectionRange(float detectionRange) {
        this.detectionRange = detectionRange;
    }

    public float getDetectionRange() {
        return detectionRange;
    }

    public void setAttackRange(float attackRange) {
        this.attackRange = attackRange;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getHealth() {
        return health;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (this.health > maxHealth) this.health = maxHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // --- Limpieza de recursos ---

    /**
     * Si la textura es manejada por esta entidad, la libera.
     * Si usás AssetManager global, no llames a dispose() aquí o asegúrate de que texture no sea administrada.
     */
    public void dispose() {
        if (texture != null) {
            try {
                texture.dispose();
            } catch (Exception ignored) {}
            texture = null;
        }
    }
}
