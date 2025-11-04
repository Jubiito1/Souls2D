package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.entities.Enemy;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.TfPooAs.Souls2D.utils.Constants;

import com.TfPooAs.Souls2D.core.Main;
import com.TfPooAs.Souls2D.world.LevelLoader;
import com.TfPooAs.Souls2D.world.TileMapRenderer;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.ui.HUD;

public class GameScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private OrthographicCamera uiCamera;
    private FitViewport uiViewport;
    private SpriteBatch batch;
    private Box2DDebugRenderer debugRenderer;

    private final int VIRTUAL_WIDTH = 1920;
    private final int VIRTUAL_HEIGHT = 1080;

    // Mundo y mapa
    private World world;
    private LevelLoader levelLoader;
    private TileMapRenderer tileMapRenderer;

    private Player player;
    private Enemy enemy;
    private HUD hud;

    public GameScreen(Main game) {
        this.game = game;

        try {
            camera = new OrthographicCamera();
            viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
            viewport.apply();

            // UI camera/viewport for screen-space HUD
            uiCamera = new OrthographicCamera();
            uiViewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, uiCamera);
            uiViewport.apply();
            uiCamera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
            uiCamera.update();

            batch = new SpriteBatch();
            debugRenderer = new Box2DDebugRenderer();

            // Crear mundo Box2D PRIMERO
            world = new World(new Vector2(0, -9.8f), true);
            System.out.println("Mundo Box2D creado correctamente");

            // Cargar mapa y colisiones
            levelLoader = new LevelLoader(world, "maps/cemetery.tmx");
            tileMapRenderer = new TileMapRenderer(levelLoader.getMap());
            System.out.println("Mapa cargado correctamente");

            // Contact listener mejorado para detectar contacto tanto del player como del enemy
            world.setContactListener(new ContactListener() {
                @Override
                public void beginContact(Contact contact) {
                    Fixture a = contact.getFixtureA();
                    Fixture b = contact.getFixtureB();

                    // Detectar si player toca el suelo
                    if ((a.getUserData() != null && a.getUserData().equals("player") &&
                        b.getUserData() != null && b.getUserData().equals("ground")) ||
                        (b.getUserData() != null && b.getUserData().equals("player") &&
                            a.getUserData() != null && a.getUserData().equals("ground"))) {
                        if (player != null) {
                            player.setGrounded(true);
                            System.out.println("Player tocando suelo");
                        }
                    }

                    // Detectar si enemy toca el suelo
                    if ((a.getUserData() != null && a.getUserData().equals("enemy") &&
                        b.getUserData() != null && b.getUserData().equals("ground")) ||
                        (b.getUserData() != null && b.getUserData().equals("enemy") &&
                            a.getUserData() != null && a.getUserData().equals("ground"))) {
                        if (enemy != null) {
                            enemy.setGrounded(true);
                            System.out.println("Enemy tocando suelo");
                        }
                    }
                }

                @Override
                public void endContact(Contact contact) {
                    Fixture a = contact.getFixtureA();
                    Fixture b = contact.getFixtureB();

                    // Player deja de tocar el suelo
                    if ((a.getUserData() != null && a.getUserData().equals("player") &&
                        b.getUserData() != null && b.getUserData().equals("ground")) ||
                        (b.getUserData() != null && b.getUserData().equals("player") &&
                            a.getUserData() != null && a.getUserData().equals("ground"))) {
                        if (player != null) {
                            player.setGrounded(false);
                        }
                    }

                    // Enemy deja de tocar el suelo
                    if ((a.getUserData() != null && a.getUserData().equals("enemy") &&
                        b.getUserData() != null && b.getUserData().equals("ground")) ||
                        (b.getUserData() != null && b.getUserData().equals("enemy") &&
                            a.getUserData() != null && a.getUserData().equals("ground"))) {
                        if (enemy != null) {
                            enemy.setGrounded(false);
                        }
                    }
                }

                @Override
                public void preSolve(Contact contact, Manifold oldManifold) {}
                @Override
                public void postSolve(Contact contact, ContactImpulse impulse) {}
            });

            System.out.println("GameScreen constructor completado");
        } catch (Exception e) {
            System.err.println("Error en constructor GameScreen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        try {
            System.out.println("Iniciando show()...");

            // Crear player PRIMERO
            player = new Player(world, 200, 300);
            System.out.println("Player creado: " + player.getPosition());

            // Crear enemy DESPUÉS del player
            enemy = new Enemy(world, 300, 300, player);
            System.out.println("Enemy creado: " + enemy.getPosition());

            // Registrar enemigo en el player para detección de golpes
            player.addEnemy(enemy);
            System.out.println("Enemy registrado en Player");

            // Crear HUD después de instanciar el jugador
            hud = new HUD(player);

            System.out.println("show() completado correctamente");
        } catch (Exception e) {
            System.err.println("Error en show(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        try {
            // Limpiar pantalla
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            // Actualizar mundo
            world.step(1 / 60f, 6, 2);

            // Actualizar entidades
            if (player != null) {
                player.update(delta);
            }
            if (enemy != null) {
                enemy.update(delta);
            }

            // Seguir al jugador
            if (player != null) {
                camera.position.set(player.getPosition().x, player.getPosition().y, 0);
            }
            camera.update();

            // Dibujar mapa
            if (tileMapRenderer != null) {
                tileMapRenderer.render(camera);
            }

            // Dibujar entidades
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            if (player != null) {
                player.render(batch);
            }
            if (enemy != null) {
                enemy.render(batch);
            }
            batch.end();

            // Render HUD en espacio de pantalla
            uiViewport.apply();
            uiCamera.update();
            if (hud != null) {
                hud.render(uiCamera, batch);
            }

            // Debug de colisiones
            Matrix4 debugMatrix = new Matrix4(camera.combined).scl(Constants.PPM);
            debugRenderer.render(world, debugMatrix);

        } catch (Exception e) {
            System.err.println("Error en render(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (uiViewport != null) {
            uiViewport.update(width, height, true);
            uiCamera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
            uiCamera.update();
        }
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        try {
            if (batch != null) batch.dispose();
            if (tileMapRenderer != null) tileMapRenderer.dispose();
            if (levelLoader != null) levelLoader.dispose();
            if (world != null) world.dispose();
            if (debugRenderer != null) debugRenderer.dispose();

            // Dispose de entidades y HUD
            if (player != null) player.dispose();
            if (enemy != null) enemy.dispose();
            if (hud != null) hud.dispose();

            System.out.println("GameScreen disposed correctamente");
        } catch (Exception e) {
            System.err.println("Error en dispose(): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
