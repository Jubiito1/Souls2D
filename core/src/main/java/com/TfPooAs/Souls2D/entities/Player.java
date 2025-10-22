package com.TfPooAs.Souls2D.entities;

import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player extends Entity {

    private Body body;
    private World world;

    private float moveSpeed = 0.3f;
    private float jumpForce = 1f;

    // Estado de suelo (usado por el ContactListener en GameScreen)
    private boolean grounded = false;

    public Player(World world, float x, float y) {
        super(x, y, "player.png" );
        this.world = world;
        createBody(x, y);
    }

    private void createBody(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / Constants.PPM, y / Constants.PPM);
        bdef.fixedRotation = true;
        body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 / Constants.PPM, height / 2 / Constants.PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0f;
        fdef.filter.categoryBits = Constants.BIT_PLAYER;
        fdef.filter.maskBits = Constants.BIT_GROUND;

        // Asegurarse de marcar la fixture del player para que el ContactListener la identifique
        body.createFixture(fdef).setUserData("player");
        shape.dispose();
    }

    @Override
    public void update(float delta) {
        handleInput();

        // Sincronizar posición visual con física
        position.set(
            body.getPosition().x * Constants.PPM - width / 2,
            body.getPosition().y * Constants.PPM - height / 2
        );
    }

    private void handleInput() {
        Vector2 vel = body.getLinearVelocity();

        // Movimiento horizontal
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            body.setLinearVelocity(vel.x - moveSpeed, vel.y);
            if (vel.x < -2) moveSpeed = 0;
            else moveSpeed = 0.3f;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            body.setLinearVelocity(vel.x + moveSpeed, vel.y);
            if (vel.x > 2) moveSpeed = 0;
            else moveSpeed = 0.3f;
        } else if (Math.abs(vel.y) < 0.01f){
            body.setLinearVelocity(0, vel.y);
        }

        // Saltar solo si está en el suelo (usamos grounded en lugar de chequear la velocidad vertical)
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && grounded) {
            body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
            grounded = false; // evitamos dobles saltos hasta que el ContactListener vuelva a setearlo
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (active) batch.draw(texture, position.x, position.y, width, height);
    }

    // --- Métodos añadidos para la integración con ContactListener ---

    /**
     * Llamar desde el ContactListener cuando el player entre/salga de contacto con el suelo.
     */
    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    /**
     * Consultar si el player está apoyado en el suelo.
     */
    public boolean isGrounded() {
        return grounded;
    }

    // Exponer body si alguna lógica externa necesita accederlo (opcional)
    public Body getBody() {
        return body;
    }
}
