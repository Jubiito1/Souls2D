package com.TfPooAs.Souls2D.entities;

import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Proyectil simple disparado por enemigos a distancia.
 * - Sin gravedad (gravityScale = 0)
 * - Colisiona con suelo y jugador
 * - Al impactar se marca para eliminar
 */
public class EnemyProjectile extends Entity {
    private final World world;
    private Body body;
    private final int damage;
    private final float speed;
    private float lifeTime;
    private boolean dead = false;

    // Render básico (textura blanca 1x1 tintada por el batch del juego si se desea)
    private static Texture whiteTex;

    public EnemyProjectile(World world, float startXpx, float startYpx, float dirX, float dirY,
                           float speed, int damage, float lifeTime) {
        super(startXpx, startYpx, (Texture) null);
        this.world = world;
        this.speed = speed;
        this.damage = damage;
        this.lifeTime = lifeTime;
        // tamaño visual sencillo (en pixeles)
        this.width = 12f;
        this.height = 4f;

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
        if (whiteTex == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(1, 1, 1, 1);
            pm.fill();
            whiteTex = new Texture(pm);
            pm.dispose();
        }
        batch.draw(whiteTex, position.x, position.y, width, height);
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
    }
}
