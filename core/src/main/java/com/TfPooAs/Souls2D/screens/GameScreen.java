package com.TfPooAs.Souls2D.screens;

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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.TfPooAs.Souls2D.entities.Enemy;

import com.TfPooAs.Souls2D.utils.Constants;
import com.TfPooAs.Souls2D.core.Main;
import com.TfPooAs.Souls2D.world.LevelLoader;
import com.TfPooAs.Souls2D.world.TileMapRenderer;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.entities.items.Bonfire;
import com.TfPooAs.Souls2D.entities.npcs.FireKeeper;
import com.TfPooAs.Souls2D.world.Background;
import com.TfPooAs.Souls2D.systems.SaveSystem;
import com.TfPooAs.Souls2D.ui.HUD;

public class GameScreen implements Screen {

    private final Main game;
    private boolean startAtLastSave; // default false
    private OrthographicCamera camera;
    private FitViewport viewport;

    // Cámara/viewport para UI (HUD)
    private OrthographicCamera uiCamera;
    private FitViewport uiViewport;

    private SpriteBatch batch;
    private Box2DDebugRenderer debugRenderer;

    private Background parallax;
    // Entities loaded from Tiled
    private java.util.List<Bonfire> bonfires;
    private java.util.ArrayList<FireKeeper> fireKeepers = new java.util.ArrayList<>();

    private final int VIRTUAL_WIDTH = 1920;
    private final int VIRTUAL_HEIGHT = 1080;

    // Mundo y mapa
    private World world;
    private LevelLoader levelLoader;
    private TileMapRenderer tileMapRenderer;

    private Player player;
    private java.util.List<Enemy> enemies = new java.util.ArrayList<>();
    private HUD hud;


    // Overlays
    private PauseOverlay pauseOverlay;
    private boolean isPaused = false;
    private DeathOverlay deathOverlay;
    private boolean isDeathShown = false;

    public GameScreen(Main game) {
        this.game = game;
        this.startAtLastSave = false;
        init();

    }

    public GameScreen(Main game, boolean startAtLastSave) {
        this.game = game;
        this.startAtLastSave = startAtLastSave;
        init();
    }

    private void init() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        // Acercar la cámara al jugador (zoom in)
        camera.zoom = 0.5f; // menor a 1 = más cerca
        camera.update();

        // UI camera/viewport para dibujar HUD en coordenadas de pantalla
        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, uiCamera);
        uiViewport.apply();
        uiCamera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        uiCamera.update();

        batch = new SpriteBatch();
        debugRenderer = new Box2DDebugRenderer();

        // Crear mundo Box2D
        world = new World(new Vector2(0, -9.8f), true);

        // Cargar mapa y colisiones
        levelLoader = new LevelLoader(world, "maps/cemetery.tmx");
        tileMapRenderer = new TileMapRenderer(levelLoader.getMap());
        bonfires = levelLoader.getBonfires();

        // Cargar Bonfires y NPCs (FireKeeper) desde el mapa si existen las capas

        loadNPCsFromMap();

        // Contact listener
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture a = contact.getFixtureA();
                Fixture b = contact.getFixtureB();

                // Player vs Ground
                if (("player".equals(a.getUserData()) && "ground".equals(b.getUserData())) ||
                    ("player".equals(b.getUserData()) && "ground".equals(a.getUserData()))) {
                    if (player != null) player.setGrounded(true);
                }

                // Enemy vs Ground
                if (("enemy".equals(a.getUserData()) && "ground".equals(b.getUserData())) ||
                    ("enemy".equals(b.getUserData()) && "ground".equals(a.getUserData()))) {
                    Body enemyBody = "enemy".equals(a.getUserData()) ? a.getBody() : b.getBody();
                    for (Enemy e : enemies) {
                        if (e.getBody() == enemyBody) {
                            e.setGrounded(true);
                            break;
                        }
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                Fixture a = contact.getFixtureA();
                Fixture b = contact.getFixtureB();

                // Player vs Ground
                if (("player".equals(a.getUserData()) && "ground".equals(b.getUserData())) ||
                    ("player".equals(b.getUserData()) && "ground".equals(a.getUserData()))) {
                    if (player != null) player.setGrounded(false);
                }

                // Enemy vs Ground
                if (("enemy".equals(a.getUserData()) && "ground".equals(b.getUserData())) ||
                    ("enemy".equals(b.getUserData()) && "ground".equals(a.getUserData()))) {
                    Body enemyBody = "enemy".equals(a.getUserData()) ? a.getBody() : b.getBody();
                    for (Enemy e : enemies) {
                        if (e.getBody() == enemyBody) {
                            e.setGrounded(false);
                            break;
                        }
                    }
                }
            }

            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });

        // --- Fondo Parallax ---
        Texture fondo = new Texture("backgrounds/fondo.png");

        Texture[] layers = { fondo };
        float[] speeds = { 1f }; // Menor = más lejos, Mayor = más cercano

        parallax = new Background(layers, speeds, camera);
    }

    private void loadNPCsFromMap() {
        MapLayer layer = levelLoader.getMap().getLayers().get("NPCs");
        if (layer == null) {
            Gdx.app.log("GameScreen", "Layer 'NPCs' no encontrada. Continuando sin NPCs.");
            return;
        }
        MapObjects objects = layer.getObjects();
        for (MapObject mo : objects) {
            if (mo instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) mo).getRectangle();
                // En el futuro, leer propiedad 'type' para diferentes NPCs
                fireKeepers.add(new FireKeeper(rect.x, rect.y));
            }
        }
    }

    @Override
    public void show() {

        if (player == null) player = new Player(world, 1200, 2170);
        if (startAtLastSave && SaveSystem.hasLastBonfire()) {
            float[] pos = SaveSystem.loadLastBonfire();
            if (pos != null) {
                player.teleportToPixels(pos[0], pos[1]);
                Gdx.app.log("GameScreen", "Spawned at last bonfire from main menu: (" + pos[0] + ", " + pos[1] + ")");
            }
        }
        if (pauseOverlay == null) pauseOverlay = new PauseOverlay(game, this);
        if (deathOverlay == null) deathOverlay = new DeathOverlay(game, this);

        // Crear varios enemigos después de crear el player (solo una vez)
        if (enemies.isEmpty()) {
            float[][] spawnPoints = new float[][]{
                {1600, 2170},
                {1900, 2170},
                {2100, 2170},
                {2600, 3170},
                {3200, 2170},
            };
            for (float[] sp : spawnPoints) {
                Enemy e = new Enemy(world, sp[0], sp[1], player);
                enemies.add(e);
                if (player != null) player.addEnemy(e);
            }
        }




        // Crear HUD después de instanciar el player (y opcionalmente el enemy)
        if (hud == null) {
            hud = new HUD(player); // si tu HUD necesita también enemy, ajusta el constructor
        }

    }

    @Override
    public void render(float delta) {
        // --- Trigger automático de DeathOverlay cuando el jugador muere ---
        if (!isDeathShown && player != null && player.isDead()) {
            isDeathShown = true;
            if (deathOverlay != null) deathOverlay.show();
        }

        if (!isDeathShown && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (!isPaused) {
                isPaused = true;
                pauseOverlay.show();
            }
        }

        // --- Lógica de actualización ---
        if (!isPaused && !isDeathShown) {
            world.step(1 / 60f, 6, 2);
            if (player != null) player.update(delta);
            // Actualizar entidades del mapa
            for (Bonfire b : bonfires) {
                b.update(delta, player);
            }
            for (FireKeeper fk : fireKeepers) {
                fk.update(delta);
            }
            // Actualizar enemigos
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy e = enemies.get(i);
                e.update(delta);
                if (e.isDead()) {
                    // Quitar enemigos muertos
                    if (player != null) player.removeEnemy(e);
                    e.dispose();
                    enemies.remove(i);
                }
            }
        }

        // --- Actualizar cámara ---
        if (player != null) {
            float cx = player.getPosition().x + player.getWidth() / 2f;
            float cy = player.getPosition().y + player.getHeight() / 2f;
            camera.position.set(cx, cy, 0);
            camera.update();
        }

        // --- Actualizar Parallax ---
        parallax.update(delta);

        // --- Renderizado ---
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        parallax.render(batch); // 👈 Fondo primero
        batch.end();

        // Luego mapa y entidades
        tileMapRenderer.render(camera);

        batch.begin();
        // Render entidades del mapa
        for (Bonfire b : bonfires) b.render(batch);
        for (FireKeeper fk : fireKeepers) fk.render(batch);
        if (player != null) player.render(batch);
        for (Enemy e : enemies) e.render(batch); // render de todos los enemigos
        batch.end();

        // Debug opcional
        Matrix4 debugMatrix = new Matrix4(camera.combined).scl(Constants.PPM);
        debugRenderer.render(world, camera.combined.scl(Constants.PPM));


        // Overlays primero (se dibujan sobre el juego)
        if (isPaused && pauseOverlay != null) pauseOverlay.render(delta);
        if (isDeathShown && deathOverlay != null) deathOverlay.render(delta);

        // Render HUD en espacio de pantalla (usa uiViewport/uiCamera) SIEMPRE ENCIMA
        uiViewport.apply();
        uiCamera.update();
        if (hud != null) {
            // HUD visible incluso con overlays (pausa/muerte)
            hud.render(uiCamera, batch);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);

        if (pauseOverlay != null) pauseOverlay.getStage().getViewport().update(width, height, true);
        if (deathOverlay != null) deathOverlay.getStage().getViewport().update(width, height, true);
    }

    @Override public void hide() {
        if (pauseOverlay != null) pauseOverlay.hide();
        if (deathOverlay != null) deathOverlay.hide();
    }
    @Override public void pause() {}
    @Override public void resume() {}

    public void resumeFromPause() {
        if (!isPaused) return;
        isPaused = false;
        if (pauseOverlay != null) pauseOverlay.hide();
        Gdx.input.setInputProcessor(null);
    }

    // Resume and teleport player to last bonfire if a save exists
    public void resumeFromPauseToLastSave() {
        if (!isPaused) return;
        try {
            float[] pos = SaveSystem.loadLastBonfire();
            if (pos != null && player != null) {
                player.teleportToPixels(pos[0], pos[1]);
                Gdx.app.log("GameScreen", "Teleported player to last bonfire: (" + pos[0] + ", " + pos[1] + ")");
            } else {
                Gdx.app.log("GameScreen", "No saved bonfire. Resuming without teleport.");
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to resume to last save: " + e.getMessage(), e);
        }
        isPaused = false;
        if (pauseOverlay != null) pauseOverlay.hide();
        Gdx.input.setInputProcessor(null);
    }

    public Main getGame() { return game; }
    public boolean isPaused() { return isPaused; }

    public void onOverlayReturned() {
        if (isPaused && pauseOverlay != null) {
            pauseOverlay.getStage().getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
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
        for (Bonfire b : bonfires) b.dispose();
        for (FireKeeper fk : fireKeepers) fk.dispose();
        bonfires.clear();
        fireKeepers.clear();
        if (player != null) player.dispose();
        for (Enemy e : enemies) e.dispose();
        enemies.clear();
        if (hud != null) hud.dispose();
    }
}
