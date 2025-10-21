package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PauseMenu implements Screen {
    private final Main game;
    private final Screen gameScreen; // para volver
    private Stage stage;
    private Skin skin;

    public PauseMenu(Main game, Screen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
        init();
    }

    private void init() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table t = new Table();
        t.setFillParent(true);
        t.center();

        TextButton resume = new TextButton("Continuar", skin);
        TextButton options = new TextButton("Opciones", skin);
        TextButton quit = new TextButton("Salir al menú", skin);

        resume.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // En vez de game.setScreen(gameScreen), usamos el GSM para restaurar la pantalla previa
                game.gsm.restorePreviousScreen();
            }
        });

        options.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showOptions(PauseMenu.this);
            }
        });

        quit.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showMainMenu();
            }
        });

        t.add(resume).width(300).height(56).pad(6);
        t.row();
        t.add(options).width(300).height(56).pad(6);
        t.row();
        t.add(quit).width(300).height(56).pad(6);

        stage.addActor(t);
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }

    @Override
    public void render(float delta) {
        // Semitransparente sobre el juego (siempre que el GameScreen siga existiendo)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glClearColor(0f, 0f, 0f, 0.4f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
