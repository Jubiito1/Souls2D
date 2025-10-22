package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.entities.enemies.EnemyMelee;
import com.TfPooAs.Souls2D.utils.Constants;
import com.TfPooAs.Souls2D.world.LevelLoader;
import com.TfPooAs.Souls2D.world.TileMapRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;

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

    // Entidades
    private Player player;
    private ArrayList<EnemyMelee> enemies;

    public GameScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();

        batch = new SpriteBatch();
        debugRenderer = new Box2DDebugRenderer();

        world = new World(new Vector2(0, -9.8f), true);

        levelLoader = new LevelLoader(world, "maps/cemetery.tmx");
        tileMapRenderer = new TileMapRenderer(levelLoader.getMap());

        // Contact listener para detectar si el player toca el suelo
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture a = contact.getFixtureA();
                Fixture b = contact.getFixtureB();

                if (player != null) {
                    if ((a.getUserData() != null && a.getUserData().equals("player") &&
                        b.getUserData() != null && b.getUserData().equals("ground")) ||
                        (b.getUserData() != null && b.getUserData().equals("player") &&
                            a.getUserData() != null && a.getUserData().equals("ground"))) {
                        player.setGrounded(true);
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                Fixture a = contact.getFixtureA();
                Fixture b = contact.getFixtureB();

                if (player != null) {
                    if ((a.getUserData() != null && a.getUserData().equals("player") &&
                        b.getUserData() != null && b.getUserData().equals("ground")) ||
                        (b.getUserData() != null && b.getUserData().equals("player") &&
                            a.getUserData() != null && a.getUserData().equals("ground"))) {
                        player.setGrounded(false);
                    }
                }
            }

            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    @Override
    public void show() {
        // Crear jugador (asegurarse que Player maneje la creación del body y userData)
        player = new Player(world, 200, 300);

        // Crear enemigos
        enemies = new ArrayList<>();
        MapObjects objects = levelLoader.getMap().getLayers().get("Enemies").getObjects();
        // iterar de forma segura y soportar distintos tipos de MapObject
        for (MapObject mo : objects) {
            if (mo instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) mo).getRectangle();
                // Instanciar EnemyMelee con la firma: EnemyMelee(World world, float x, float y, Player player)
                enemies.add(new EnemyMelee(world, rect.x, rect.y, player));
            }
        }
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar mundo Box2D
        world.step(1 / 60f, 6, 2);

        // Actualizar player y enemigos
        if (player != null) player.update(delta);
        for (EnemyMelee enemy : enemies) {
            enemy.update(delta);
        }

        // Mover cámara siguiendo al jugador (proteger contra player nulo)
        if (player != null) {
            camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        }
        camera.update();

        // Renderizar mapa
        tileMapRenderer.render(camera);

        // Renderizar player y enemigos
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (player != null) player.render(batch);
        for (EnemyMelee enemy : enemies) {
            enemy.render(batch);
        }
        batch.end();

        // Renderizar debug de Box2D usando una copia escalada de la matriz de cámara
        debugRenderer.render(world, camera.combined.cpy().scl(Constants.PPM));
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
