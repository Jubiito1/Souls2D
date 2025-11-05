package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.entities.enemies.EnemyMelee;
import com.TfPooAs.Souls2D.entities.items.Bonfire;
import com.TfPooAs.Souls2D.entities.npcs.FireKeeper;
import com.TfPooAs.Souls2D.ui.DialogBox;
import com.TfPooAs.Souls2D.utils.Constants;
import com.TfPooAs.Souls2D.systems.SaveSystem;
import com.TfPooAs.Souls2D.world.LevelLoader;
import com.TfPooAs.Souls2D.world.TileMapRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
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
    private ArrayList<Bonfire> bonfires;
    private FireKeeper fireKeeper;

    // UI
    private Stage uiStage;
    private DialogBox dialogBox;
    private BitmapFont font;

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

        // UI
        uiStage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
        dialogBox = new DialogBox(uiStage);
        font = new BitmapFont();

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
        // Capa de enemigos puede no existir; evitar NPE
        com.badlogic.gdx.maps.MapLayer enemiesLayer = levelLoader.getMap().getLayers().get("Enemies");
        if (enemiesLayer != null) {
            MapObjects objects = enemiesLayer.getObjects();
            // iterar de forma segura y soportar distintos tipos de MapObject
            for (MapObject mo : objects) {
                if (mo instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) mo).getRectangle();
                    // Instanciar EnemyMelee con la firma: EnemyMelee(World world, float x, float y, Player player)
                    enemies.add(new EnemyMelee(world, rect.x, rect.y, player));
                }
            }
        } else {
            Gdx.app.log("GameScreen", "Layer 'Enemies' no encontrada en el mapa. Continuando sin enemigos.");
        }

        // Crear fogatas (bonfires) para guardar la partida
        bonfires = new ArrayList<>();
        bonfires.add(new Bonfire(300, 300)); // bonfire original cerca del inicio
        bonfires.add(new Bonfire(900, 300)); // segunda bonfire para testear guardado

        // Crear FireKeeper cerca de la primera fogata
        fireKeeper = new FireKeeper(330, 300);
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar mundo Box2D
        world.step(1 / 60f, 6, 2);

        // Entradas para diálogo
        handleDialogInput();

        // Si no hay diálogo, permitir regresar al último guardado con R
        if (!dialogBox.isVisible()) {
            handleReturnToLastSave();
            if (player != null) player.update(delta);
            for (EnemyMelee enemy : enemies) {
                enemy.update(delta);
            }
            if (bonfires != null) {
                for (Bonfire bf : bonfires) {
                    bf.update(player, delta);
                }
            }
        } else {
            // actualizar UI
            uiStage.act(delta);
        }

        // Mover cámara siguiendo al jugador (proteger contra player nulo)
        if (player != null) {
            camera.position.set(player.getPosition().x, player.getPosition().y, 0);
        }
        camera.update();

        // Renderizar mapa
        tileMapRenderer.render(camera);

        // Renderizar player, enemigos, fogatas y FireKeeper
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (player != null) player.render(batch);
        for (EnemyMelee enemy : enemies) {
            enemy.render(batch);
        }
        if (bonfires != null) {
            for (Bonfire bf : bonfires) {
                bf.render(batch);
            }
        }
        if (fireKeeper != null) fireKeeper.render(batch);

        // Mostrar prompt 'E' si el jugador está cerca y no hay diálogo
        if (player != null && fireKeeper != null && !dialogBox.isVisible()) {
            if (fireKeeper.canInteract(player.getPosition())) {
                float x = fireKeeper.getPosition().x;
                float y = fireKeeper.getPosition().y + 50; // sobre la cabeza
                font.draw(batch, "[E] Hablar", x, y);
            }
        }
        batch.end();

        // Dibujar UI (dialog box)
        uiStage.getViewport().apply();
        dialogBox.draw();

        // Renderizar debug de Box2D usando una copia escalada de la matriz de cámara
        debugRenderer.render(world, camera.combined.cpy().scl(Constants.PPM));
    }

    private void handleReturnToLastSave() {
        if (player == null) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // Intentar cargar última posición guardada
            Vector2 saved = SaveSystem.loadLastPlayerPosition();
            Vector2 target = null;
            if (saved != null) {
                target = saved;
            } else if (bonfires != null && !bonfires.isEmpty()) {
                target = bonfires.get(0).getPosition();
            } else {
                // Fallback: posición inicial por defecto
                target = new Vector2(200, 300);
            }
            player.teleportToPixels(target.x, target.y);
        }
    }

    private void handleDialogInput() {
        if (player == null || fireKeeper == null) return;
        boolean closeEnough = fireKeeper.canInteract(player.getPosition());
        if (!dialogBox.isVisible()) {
            if (closeEnough && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                dialogBox.show(fireKeeper.getDialog());
                // Congelar al jugador durante el diálogo
                player.setFrozen(true);
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                dialogBox.next();
                // Si se cerró el diálogo con este avance, descongelar
                if (!dialogBox.isVisible()) {
                    player.setFrozen(false);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
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
        if (bonfires != null) {
            for (Bonfire bf : bonfires) {
                bf.dispose();
            }
        }
        if (fireKeeper != null) fireKeeper.dispose();
        if (uiStage != null) uiStage.dispose();
        if (font != null) font.dispose();
    }
}
