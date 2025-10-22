package com.TfPooAs.Souls2D.entities.enemies;

import com.TfPooAs.Souls2D.entities.Entity;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

public class EnemyMelee extends Entity {

    private Player player;
    private World world;
    private Body body;

    private float speed = 100f; // píxeles por segundo (render)
    private float detectionRange = 300f;
    private float attackRange = 40f;
    private float attackCooldown = 1.5f;
    private float attackTimer = 0f;
    private boolean canAttack = true;

    private int damage = 10;

    /** Constructor por defecto usando un asset por nombre (ruta relativa en assets) */
    public EnemyMelee(World world, float xPx, float yPx, Player player) {
        this(world, xPx, yPx, player, "enemy.png");
    }

    /** Constructor que permite especificar la textura usada (útil al leer TMX properties) */
    public EnemyMelee(World world, float xPx, float yPx, Player player, String texturePath) {
        super(xPx, yPx, texturePath); // Entity(String) debe existir (lo tiene la Entity que te pasé)
        this.world = world;
        this.player = player;
        // crear body en unidades Box2D: convertimos píxeles->metros usando Constants.PPM
        createBody(xPx, yPx);
    }

    /** Constructor que acepta directamente un TextureRegion (si usás AssetManager) */
    public EnemyMelee(World world, float xPx, float yPx, Player player, TextureRegion region) {
        super(xPx, yPx, region);
        this.world = world;
        this.player = player;
        createBody(xPx, yPx);
    }

    @Override
    public void update(float delta) {
        if (!active || player == null || !player.isAlive()) return;

        // sincroniza de la física a la posición visual si hay body
        syncFromBody(); // usa la utilidad en Entity (si la implementaste)
        if (body != null) {
            // position ya actualizado por syncFromBody si se desea; en caso contrario:
            position.set(body.getPosition().x * Constants.PPM - width / 2f,
                body.getPosition().y * Constants.PPM - height / 2f);
        }

        Vector2 playerPos = new Vector2(player.getPosition().x, player.getPosition().y);
        Vector2 enemyPos = new Vector2(position.x, position.y);

        float distance = playerPos.dst(enemyPos);

        // Movimiento hacia el jugador (solo a modo de ejemplo, mueve la posición visual y la física)
        if (distance < detectionRange && distance > attackRange) {
            Vector2 direction = playerPos.cpy().sub(enemyPos).nor();
            float movePx = speed * delta;
            position.add(direction.scl(movePx));

            if (body != null) {
                // actualizar body (convertir a metros)
                float bx = (position.x + width / 2f) / Constants.PPM;
                float by = (position.y + height / 2f) / Constants.PPM;
                body.setTransform(bx, by, body.getAngle());
            }
        }

        // Ataque (simple)
        if (distance <= attackRange && canAttack) {
            player.takeDamage(damage);
            canAttack = false;
            attackTimer = 0f;
        }

        if (!canAttack) {
            attackTimer += delta;
            if (attackTimer >= attackCooldown) canAttack = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (active) {
            if (region != null) {
                batch.draw(region, position.x, position.y, width, height);
            } else if (texture != null) {
                batch.draw(texture, position.x, position.y, width, height);
            }
        }
    }

    /** Crea el body usando conversiones PPM coherentes con Player */
    protected void createBody(float xPx, float yPx) {
        if (world == null) return;

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        // Box2D espera metros, convertimos:
        bdef.position.set( (xPx + width / 2f) / Constants.PPM, (yPx + height / 2f) / Constants.PPM );
        bdef.fixedRotation = true;
        this.body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        // half-width/half-height en metros
        float hx = (width / 2f) / Constants.PPM;
        float hy = (height / 2f) / Constants.PPM;
        shape.setAsBox(hx, hy);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0f;
        fdef.filter.categoryBits = Constants.BIT_ENEMY;
        fdef.filter.maskBits = Constants.BIT_GROUND | Constants.BIT_PLAYER;

        Fixture fix = body.createFixture(fdef);
        // marcar fixture para detectar colisiones si lo necesitas
        fix.setUserData("enemy");
        shape.dispose();
    }

    // getters/setters si necesitas cambiar cosas desde fuera
    public void setPlayer(Player player) { this.player = player; }
    public Player getPlayer() { return player; }
}
