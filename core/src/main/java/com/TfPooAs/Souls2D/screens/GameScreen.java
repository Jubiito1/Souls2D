package com.TfPooAs.Souls2D.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import com.TfPooAs.Souls2D.core.Main;
import com.TfPooAs.Souls2D.world.LevelLoader;
import com.TfPooAs.Souls2D.world.TileMapRenderer;
import com.TfPooAs.Souls2D.entities.Player;

public class GameScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;

    private final int VIRTUAL_WIDTH = 1920;
    private final int VIRTUAL_HEIGHT = 1080;

    // Mundo y mapa
    private World world;
    private LevelLoader levelLoader;
    private TileMapRenderer tileMapRenderer;

    private Player player;

    public GameScreen(Main game) {
        this.game = game;

        // Cámara y viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();

        batch = new SpriteBatch();

        // Inicializar mundo Box2D con gravedad
        world = new World(new Vector2(0, -9.8f), true);

        // Cargar nivel y mapa
        levelLoader = new LevelLoader(world, "maps/cemetery.tmx");

        // Inicializar renderer para dibujar el mapa
        tileMapRenderer = new TileMapRenderer(levelLoader.getMap());
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar cámara
        camera.update();

        // Dibujar mapa
        tileMapRenderer.render(camera);

        // Dibujar entidades (player, enemigos, NPCs)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        player.render(batch);

        batch.end();

        player.update(Gdx.graphics.getDeltaTime());

        // Actualizar mundo físico
        world.step(1 / 60f, 6, 2);

        // TODO: actualizar player, enemigos, partículas, efectos, etc.
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void show() {
        player = new Player(100, 100);
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
    }
}
