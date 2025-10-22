package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * PauseOverlay: overlay de UI que se dibuja sobre GameScreen sin cambiar de Screen.
 * Ahora incluye un headerImage centrado en el borde superior que no oscurece
 * el resto del overlay (solo su área).
 */
public class PauseOverlay {
    private final Stage stage;
    private final Skin skin;
    private final Texture dimTexture; // 1x1 texture para dimming
    private final GameScreen gameScreen;

    // header image
    private Texture headerTexture;
    private Image headerImage;
    private float headerWidth = 700f;  // ajustá si querés
    private float headerHeight = 375f; // ajustá si querés
    private float headerTopMargin = 30f; // margen desde el borde superior

    public PauseOverlay(Main game, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // requiere core/assets/ui/uiskin.json
        dimTexture = create1x1Texture();

        buildUI();
        loadHeader();          // carga y posiciona el header
    }

    private void buildUI() {
        Table t = new Table();
        t.setFillParent(true);
        t.center();

        TextButton resume = new TextButton("Continuar", skin);
        TextButton options = new TextButton("Opciones", skin);
        TextButton quit = new TextButton("Salir al menú", skin);

        // Resume: llama a método de GameScreen para despausar
        resume.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                gameScreen.resumeFromPause();
            }
        });

        // Options: usamos el GSM, pero sin destruir la GameScreen
        options.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // showOptions en gsm está preparado para "keep previous"
                gameScreen.getGame().gsm.showOptions(gameScreen);
            }
        });

        // Quit: volver al menú principal
        quit.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // Si necesitás guardar estado antes de salir lo llamás aquí
                gameScreen.getGame().gsm.showMainMenu();
            }
        });

        float btnW = 300f, btnH = 56f, pad = 6f;
        t.add(resume).width(btnW).height(btnH).pad(pad);
        t.row();
        t.add(options).width(btnW).height(btnH).pad(pad);
        t.row();
        t.add(quit).width(btnW).height(btnH).pad(pad);

        stage.addActor(t);
    }

    private void loadHeader() {
        // Cargar la textura del header. Ruta: core/assets/ui/pause_header.png
        try {
            headerTexture = new Texture(Gdx.files.internal("ui/pause_header.png"));
            headerImage = new Image(new TextureRegion(headerTexture));
            headerImage.setSize(headerWidth, headerHeight);
            positionHeader(); // coloca en viewport actual
            // Insertar el header ANTES de la UI para que no tape botones (si querés lo pongas encima)
            stage.addActor(headerImage);
        } catch (Exception e) {
            // Si falta el asset, evitamos crash y seguimos sin header
            headerTexture = null;
            headerImage = null;
            Gdx.app.log("PauseOverlay", "No se pudo cargar pause_header.png: " + e.getMessage());
        }
    }

    private void positionHeader() {
        if (headerImage == null) return;
        float vw = stage.getViewport().getWorldWidth();
        float vh = stage.getViewport().getWorldHeight();
        float x = (vw - headerWidth) / 2f;
        float y = vh - headerHeight - headerTopMargin;
        headerImage.setPosition(x, y);
    }

    // Dibuja el overlay (dimming + stage)
    public void render(float delta) {
        stage.act(delta);

        // Dibujar dimming usando el batch del stage
        stage.getBatch().begin();
        stage.getBatch().setColor(0f, 0f, 0f, 0.45f); // opacidad 0.45
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();
        stage.getBatch().draw(dimTexture, 0, 0, w, h);
        stage.getBatch().setColor(1f, 1f, 1f, 1f);
        stage.getBatch().end();

        // Dibujamos los widgets (botones y header)
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public void show() {
        // Actualizamos viewport y posición header al mostrar
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        positionHeader();
        Gdx.input.setInputProcessor(stage);
    }

    public void hide() {
        Gdx.input.setInputProcessor(null); // o el InputProcessor del juego si lo tenés
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        dimTexture.dispose();
        if (headerTexture != null) headerTexture.dispose();
    }

    private Texture create1x1Texture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
