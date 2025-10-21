package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.Preferences;

public class OptionsMenu implements Screen {
    private final Main game;
    private final Screen returnTo;
    private Stage stage;
    private Skin skin;
    private Preferences prefs;

    public OptionsMenu(Main game, Screen returnTo) {
        this.game = game;
        this.returnTo = returnTo;
        prefs = Gdx.app.getPreferences("souls2d_options");
        init();
    }

    private void init() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table t = new Table();
        t.setFillParent(true);
        t.center();

        Label title = new Label("Opciones", skin);
        Slider music = new Slider(0f, 1f, 0.01f, false, skin);
        music.setValue(prefs.getFloat("musicVolume", 1f));
        music.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("musicVolume", music.getValue());
                prefs.flush();
                // si tenés SoundManager: game.getSoundManager().setMusicVolume(music.getValue());
            }
        });

        TextButton back = new TextButton("Volver", skin);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // Restauramos la pantalla previa desde el GSM
                game.gsm.restorePreviousScreen();
            }
        });

        t.add(title).padBottom(10);
        t.row();
        t.add(new Label("Volumen música", skin)).padBottom(6);
        t.row();
        t.add(music).width(300).padBottom(12);
        t.row();
        t.add(back).width(200).height(48);

        stage.addActor(t);
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
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
