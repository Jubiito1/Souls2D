package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.TfPooAs.Souls2D.core.GameScreenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MainMenuScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(Main game) {
        this.game = game;
        init();
    }

    private void init() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Cargá un skin en assets/ui/uiskin.json
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        TextButton newGame = new TextButton("Nueva Partida", skin);
        TextButton cont = new TextButton("Continuar", skin);
        TextButton load = new TextButton("Cargar Partida", skin);
        TextButton options = new TextButton("Opciones", skin);
        TextButton exit = new TextButton("Salir", skin);

        // por ahora, Continuar/ Cargar se comportan igual (si no hay saves, podrían deshabilitarse)
        cont.setDisabled(true); // habilitalo cuando implementes saves

        newGame.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showGameScreen();
            }
        });

        cont.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // implementá carga real con SaveSystem si ya lo tenés
                game.gsm.showGameScreen();
            }
        });

        load.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // aquí podrías abrir un diálogo con saves
                game.gsm.showGameScreen();
            }
        });

        options.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showOptions(MainMenuScreen.this);
            }
        });

        exit.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        table.pad(20);
        table.add(newGame).width(360).height(64).pad(6);
        table.row();
        table.add(cont).width(360).height(64).pad(6);
        table.row();
        table.add(load).width(360).height(64).pad(6);
        table.row();
        table.add(options).width(360).height(64).pad(6);
        table.row();
        table.add(exit).width(360).height(64).pad(6);

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Fondo oscuro
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(delta, 1/30f));
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
