package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * PauseOverlay: overlay de UI que se dibuja sobre GameScreen sin cambiar de Screen.
 */
public class PauseOverlay {
    private final Stage stage;
    private final Skin skin;
    private final Texture dimTexture; // 1x1 texture para dimming
    private final GameScreen gameScreen;

    public PauseOverlay(Main game, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // ruta: core/assets/ui/uiskin.json
        dimTexture = create1x1Texture();

        buildUI();
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

        t.add(resume).width(300).height(56).pad(6);
        t.row();
        t.add(options).width(300).height(56).pad(6);
        t.row();
        t.add(quit).width(300).height(56).pad(6);

        stage.addActor(t);
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

        // Dibujamos los widgets (botones)
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    public void hide() {
        Gdx.input.setInputProcessor(null); // o el InputProcessor del juego si lo tenés
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        dimTexture.dispose();
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
