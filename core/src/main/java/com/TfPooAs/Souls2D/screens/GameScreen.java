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

    // Pause overlay
    private PauseOverlay pauseOverlay;
    private boolean isPaused = false;

    // Death overlay (para pruebas manuales sobre GameScreen)
    private DeathOverlay deathOverlay;
    private boolean isDeathShown = false;

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

        // Contact listener
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) {
                Fixture a = contact.getFixtureA();
                Fixture b = contact.getFixtureB();

                if ((a.getUserData() != null && a.getUserData().equals("player") &&
                    b.getUserData() != null && b.getUserData().equals("ground")) ||
                    (b.getUserData() != null && b.getUserData().equals("player") &&
                        a.getUserData() != null && a.getUserData().equals("ground"))) {
                    if (player != null) player.setGrounded(true);
                }
            }

            @Override public void endContact(Contact contact) {
                Fixture a = contact.getFixtureA();
                Fixture b = contact.getFixtureB();

                if ((a.getUserData() != null && a.getUserData().equals("player") &&
                    b.getUserData() != null && b.getUserData().equals("ground")) ||
                    (b.getUserData() != null && b.getUserData().equals("player") &&
                        a.getUserData() != null && a.getUserData().equals("ground"))) {
                    if (player != null) player.setGrounded(false);
                }
            }

            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    @Override
    public void show() {
        // Crear player con fixture sin fricción (si no existe)
        if (player == null) {
            player = new Player(world, 200, 300);
        }
        // Crear overlay (si no existe)
        if (pauseOverlay == null) {
            pauseOverlay = new PauseOverlay(game, this);
        }
        if (deathOverlay == null) {
            deathOverlay = new DeathOverlay(game, this);
        }
    }

    @Override
    public void render(float delta) {
        // Activar overlay de muerte para pruebas con tecla D (solo si no está ya mostrado)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.D)) {
            if (!isDeathShown) {
                isDeathShown = true;
                if (deathOverlay != null) deathOverlay.show();
            }
        }

        // Toggle pausa con ESC (solo si no está el overlay de muerte)
        if (!isDeathShown && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (!isPaused) {
                isPaused = true;
                pauseOverlay.show();
            } else {
                // dejá que el botón "Continuar" haga resume (o si querés, permitir ESC para resume)
                // resumeFromPause();
            }
        }

        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar mundo solo si no estamos en pausa ni mostrando muerte
        if (!isPaused && !isDeathShown) {
            world.step(1 / 60f, 6, 2);
            if (player != null) player.update(delta);
        }

        // Seguir al jugador (incluso si está congelado)
        if (player != null) {
            camera.position.set(player.getPosition().x, player.getPosition().y, 0);
            camera.update();
        }

        // Dibujar mapa y entidades siempre (se verá congelado en pausa)
        tileMapRenderer.render(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (player != null) player.render(batch);
        batch.end();

        // Debug (opcional)
        debugRenderer.render(world, camera.combined.scl(Constants.PPM));

        // Si está pausado, dibujamos overlay
        if (isPaused && pauseOverlay != null) {
            pauseOverlay.render(delta);
        }
        // Si está el overlay de muerte activo, render por encima de todo
        if (isDeathShown && deathOverlay != null) {
            deathOverlay.render(delta);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (pauseOverlay != null) {
            pauseOverlay.getStage().getViewport().update(width, height, true);
        }
        if (deathOverlay != null) {
            deathOverlay.getStage().getViewport().update(width, height, true);
        }
    }

    @Override public void hide() {
        if (pauseOverlay != null) pauseOverlay.hide();
        if (deathOverlay != null) deathOverlay.hide();
    }
    @Override public void pause() {}
    @Override public void resume() {}

    // Permite que PauseOverlay (o botones) llamen para reanudar
    public void resumeFromPause() {
        if (!isPaused) return;
        isPaused = false;
        if (pauseOverlay != null) pauseOverlay.hide();
        // Restaurá el input del juego si usás un InputProcessor (aquí usamos null/polling)
        Gdx.input.setInputProcessor(null);
    }

    // Getter para que PauseOverlay pueda acceder al Game (Main)
    public Main getGame() {
        return game;
    }
    // Getter para saber si el GameScreen está en pausa
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Llamado desde el GameScreenManager cuando restauramos esta pantalla desde Options u otra pantalla.
     * Si el game estaba en pausa, hay que reactivar el overlay (input + viewport).
     */
    public void onOverlayReturned() {
        if (isPaused && pauseOverlay != null) {
            // Reajusta viewport del overlay por si cambió tamaño
            pauseOverlay.getStage().getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            // Asegura que el input vaya al overlay
            pauseOverlay.show();
        }
    }


    @Override
    public void dispose() {
        batch.dispose();
        tileMapRenderer.dispose();
        levelLoader.dispose();
        world.dispose();
        debugRenderer.dispose();
        if (pauseOverlay != null) pauseOverlay.dispose();
        if (deathOverlay != null) deathOverlay.dispose();
    }
}
