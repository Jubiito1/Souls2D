package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import com.TfPooAs.Souls2D.utils.Constants;

public class Player extends Entity {
    private Body body;
    private World world;

    private float moveSpeed = 3f;
    private float jumpForce = 1f; // aumento para un salto más visible
    private boolean isGrounded = true;

    public Player(World world, float x, float y) {
        super(x, y, "player.png");
        this.world = world;
        createBody(x, y);
    }

    private void createBody(float x, float y) {
        // Definición del cuerpo
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / Constants.PPM, y / Constants.PPM);
        bdef.fixedRotation = true; // evita que el cuerpo rote
        body = world.createBody(bdef);

        // Cuerpo principal
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 / Constants.PPM, height / 2 / Constants.PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0f; // fricción 0 para evitar quedarse pegado a paredes
        fdef.filter.categoryBits = Constants.BIT_PLAYER;
        fdef.filter.maskBits = Constants.BIT_GROUND;

        body.createFixture(fdef).setUserData("player");

        shape.dispose();
    }

    public void update(float delta) {
        handleInput();

        // Sincronizar posición visual con posición del cuerpo
        position.set(
            body.getPosition().x * Constants.PPM - width / 2,
            body.getPosition().y * Constants.PPM - height / 2
        );
    }

    private void handleInput() {
        Vector2 vel = body.getLinearVelocity();

        // Movimiento horizontal
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            body.setLinearVelocity(-moveSpeed, vel.y);
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            body.setLinearVelocity(moveSpeed, vel.y);
        } else {
            body.setLinearVelocity(0, vel.y); // frena horizontal
        }

        // Saltar solo si la velocidad Y es casi 0 (suelo)
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && Math.abs(vel.y) < 0.01f) {
            body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);

        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, width, height);
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public Body getBody() {
        return body;
    }
}
