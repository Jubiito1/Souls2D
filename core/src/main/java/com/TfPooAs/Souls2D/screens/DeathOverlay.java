package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * DeathOverlay: Overlay de UI que se dibuja sobre GameScreen al morir.
 * Para pruebas, se puede activar con una tecla desde GameScreen.
 * Muestra un título grande centrado "HAS MUERTO" y dos botones:
 *  - Reintentar: reinicia la partida (vuelve a cargar GameScreen)
 *  - Volver al menú principal
 */
public class DeathOverlay {
    private final Stage stage;
    private final Skin skin;
    private final Texture dimTexture; // 1x1 para dimming
    private final GameScreen gameScreen;

    public DeathOverlay(Main game, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.dimTexture = create1x1Texture();

        buildUI(game);
    }

    private void buildUI(Main game) {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("HAS MUERTO", skin);
        // Hacemos el título grande
        title.setFontScale(3.0f);

        TextButton retry = new TextButton("Reintentar", skin);
        TextButton toMenu = new TextButton("Volver al menú principal", skin);

        retry.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // Reiniciar el juego: cargar una nueva GameScreen
                game.gsm.showGameScreen();
            }
        });

        toMenu.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showMainMenu();
            }
        });

        float btnW = 360f, btnH = 64f, pad = 10f;
        root.add(title).padBottom(20f);
        root.row();
        root.add(retry).width(btnW).height(btnH).pad(pad);
        root.row();
        root.add(toMenu).width(btnW).height(btnH).pad(pad);

        stage.addActor(root);
    }

    public void render(float delta) {
        stage.act(delta);
        // dimming por detrás
        stage.getBatch().begin();
        stage.getBatch().setColor(0f, 0f, 0f, 0.55f);
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();
        stage.getBatch().draw(dimTexture, 0, 0, w, h);
        stage.getBatch().setColor(1f, 1f, 1f, 1f);
        stage.getBatch().end();
        // dibujar UI
        stage.draw();
    }

    public Stage getStage() { return stage; }

    public void show() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.input.setInputProcessor(stage);
    }

    public void hide() {
        Gdx.input.setInputProcessor(null);
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
