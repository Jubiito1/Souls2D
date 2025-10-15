package com.TfPooAs.Souls2D.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.TfPooAs.Souls2D.utils.Constants;

import com.TfPooAs.Souls2D.core.Main;
import com.TfPooAs.Souls2D.world.LevelLoader;
import com.TfPooAs.Souls2D.world.TileMapRenderer;
import com.TfPooAs.Souls2D.entities.Player;

public class GameScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private Box2DDebugRenderer debugRenderer;

    private final int VIRTUAL_WIDTH = 1920;
    private final int VIRTUAL_HEIGHT = 1080;

    // Mundo y mapa
    private World world;
    private LevelLoader levelLoader;
    private TileMapRenderer tileMapRenderer;

    private Player player;

    public GameScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();

        batch = new SpriteBatch();
        debugRenderer = new Box2DDebugRenderer();

        // Crear mundo Box2D
        world = new World(new Vector2(0, -9.8f), true);

        // Cargar mapa y colisiones
        levelLoader = new LevelLoader(world, "maps/cemetery.tmx");
        tileMapRenderer = new TileMapRenderer(levelLoader.getMap());

        // Contact listener para detectar si el player toca el suelo
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture a = contact.getFixtureA();
                Fixture b = contact.getFixtureB();

                if ((a.getUserData() != null && a.getUserData().equals("player") &&
                    b.getUserData() != null && b.getUserData().equals("ground")) ||
                    (b.getUserData() != null && b.getUserData().equals("player") &&
                        a.getUserData() != null && a.getUserData().equals("ground"))) {
                    player.setGrounded(true);
                }
            }

            @Override
            public void endContact(Contact contact) {
                Fixture a = contact.getFixtureA();
                Fixture b = contact.getFixtureB();

                if ((a.getUserData() != null && a.getUserData().equals("player") &&
                    b.getUserData() != null && b.getUserData().equals("ground")) ||
                    (b.getUserData() != null && b.getUserData().equals("player") &&
                        a.getUserData() != null && a.getUserData().equals("ground"))) {
                    player.setGrounded(false);
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    @Override
    public void show() {
        // Crear player con fixture sin fricción
        player = new Player(world, 400, 300);
        // Dentro del constructor del Player, asegurarse de hacer:
        // fixtureDef.friction = 0f;
        // playerBody.setUserData("player");
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar mundo
        world.step(1 / 60f, 6, 2);

        // Actualizar player (movimiento lateral + salto)
        player.update(delta);

        // Seguir al jugador
        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        camera.update();

        // Dibujar mapa
        tileMapRenderer.render(camera);

        // Dibujar entidades
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch);
        batch.end();

        // Debug de colisiones
        debugRenderer.render(world, camera.combined.scl(Constants.PPM));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        tileMapRenderer.dispose();
        levelLoader.dispose();
        world.dispose();
        debugRenderer.dispose();
    }
}
