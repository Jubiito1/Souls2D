
package com.TfPooAs.Souls2D.entities;

import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;


public class EnemyProjectile extends Entity {
    private final World world;
    private Body body;
    private final int damage;
    private final float speed;
    private float lifeTime;
    private boolean dead = false;

    // Textura del proyectil - usa una imagen específica
    private static Texture projectileTexture;
    // Fallback: textura blanca 1x1 si no se encuentra la imagen
    private static Texture whiteTex;

    public EnemyProjectile(World world, float startXpx, float startYpx, float dirX, float dirY,
                           float speed, int damage, float lifeTime) {
        super(startXpx, startYpx, (Texture) null);
        this.world = world;
        this.speed = speed;
        this.damage = damage;
        this.lifeTime = lifeTime;

        // Cargar textura del proyectil si no está cargada
        if (projectileTexture == null) {
            try {
                // CAMBIAR "proyectil.png" por el nombre de tu archivo de imagen
                projectileTexture = new Texture("flecha.png");
            } catch (Exception e) {
                // Si no se encuentra la imagen, usar fallback
                projectileTexture = null;
                System.out.println("No se pudo cargar 'proyectil.png', usando textura blanca como fallback");
            }
        }

        // Ajustar tamaño según la textura cargada
        if (projectileTexture != null) {
            this.width = projectileTexture.getWidth();
            this.height = projectileTexture.getHeight();
            // Opcional: escalar si la imagen es muy grande
            if (width > 32 || height > 32) {
                float scale = Math.min(32f / width, 32f / height);
                this.width = width * scale;
                this.height = height * scale;
            }
        } else {
            // tamaño visual sencillo (en pixeles) si no hay textura
            this.width = 12f;
            this.height = 4f;
        }

        createBody(startXpx, startYpx, dirX, dirY);
    }

    private void createBody(float startXpx, float startYpx, float dirX, float dirY) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(startXpx / Constants.PPM, startYpx / Constants.PPM);
        bdef.fixedRotation = true;
        body = world.createBody(bdef);

        // Pequeño rectángulo como hitbox del proyectil
        PolygonShape shape = new PolygonShape();
        float halfW = (width / 2f) / Constants.PPM;
        float halfH = (height / 2f) / Constants.PPM;
        shape.setAsBox(halfW, halfH);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 0.001f;
        fdef.friction = 0f;
        fdef.restitution = 0f;
        fdef.filter.categoryBits = Constants.BIT_PROJECTILE;
        fdef.filter.maskBits = (short)(Constants.BIT_GROUND | Constants.BIT_PLAYER);
        body.createFixture(fdef).setUserData("enemyProjectile");
        shape.dispose();

        // Sin gravedad y con velocidad inicial
        body.setGravityScale(0f);
        Vector2 dir = new Vector2(dirX, dirY);
        if (dir.isZero()) dir.set(1, 0);
        dir.nor().scl(speed);
        body.setLinearVelocity(dir);

        // posicionar visual
        this.position.set(startXpx - width / 2f, startYpx - height / 2f);
    }

    @Override
    public void update(float delta) {
        if (dead) return;
        lifeTime -= delta;
        if (lifeTime <= 0f) {
            dead = true;
        }
        // sync visual
        position.set(
            body.getPosition().x * Constants.PPM - width / 2f,
            body.getPosition().y * Constants.PPM - height / 2f
        );
    }

    @Override
    public void render(SpriteBatch batch) {
        if (dead) return;

        // Usar la textura del proyectil si está disponible
        if (projectileTexture != null) {
            batch.draw(projectileTexture, position.x, position.y, width, height);
        } else {
            // Fallback a textura blanca si no se pudo cargar la imagen
            if (whiteTex == null) {
                Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pm.setColor(1, 0.5f, 0, 1); // Color naranja para distinguir de otros elementos
                pm.fill();
                whiteTex = new Texture(pm);
                pm.dispose();
            }
            batch.draw(whiteTex, position.x, position.y, width, height);
        }
    }

    public Body getBody() { return body; }
    public int getDamage() { return damage; }
    public boolean isDead() { return dead; }
    public void markForRemoval() { this.dead = true; }

    @Override
    public void dispose() {
        super.dispose();
        if (body != null && world != null) {
            world.destroyBody(body);
            body = null;
        }
        // No disponer la textura estática aquí ya que es compartida
    }

    // Método estático para limpiar recursos cuando ya no se necesiten
    public static void disposeStaticResources() {
        if (projectileTexture != null) {
            projectileTexture.dispose();
            projectileTexture = null;
        }
        if (whiteTex != null) {
            whiteTex.dispose();
            whiteTex = null;
        }
    }
}
