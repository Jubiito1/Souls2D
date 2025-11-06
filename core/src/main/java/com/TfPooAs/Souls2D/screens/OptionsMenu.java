package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.Preferences;

public class OptionsMenu implements Screen {
    private final Main game;
    private final Screen returnTo;
    private Stage stage;
    private Skin skin;
    private Preferences prefs;

    // Fuentes Garamond
    private BitmapFont garamondTitleFont;
    private BitmapFont garamondLabelFont;
    private BitmapFont garamondButtonFont;

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

        generateFonts();

        Table t = new Table();
        t.setFillParent(true);
        t.center();

        // Estilo de título
        Label.LabelStyle titleStyle = new Label.LabelStyle(garamondTitleFont, Color.WHITE);
        Label title = new Label("Opciones", titleStyle);

        // Slider (no usa fuente, se deja con skin)
        Slider music = new Slider(0f, 1f, 0.01f, false, skin);
        music.setValue(prefs.getFloat("musicVolume", 1f));
        music.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("musicVolume", music.getValue());
                prefs.flush();
            }
        });

        // Estilo para labels normales
        Label.LabelStyle labelStyle = new Label.LabelStyle(garamondLabelFont, Color.WHITE);

        // Estilo de botones con Garamond
        TextButtonStyle baseStyle = null;
        try {
            baseStyle = skin.get(TextButtonStyle.class);
        } catch (Exception ignored) {}

        TextButtonStyle garamondBtnStyle = new TextButtonStyle();
        if (baseStyle != null) {
            garamondBtnStyle.up = baseStyle.up;
            garamondBtnStyle.down = baseStyle.down;
            garamondBtnStyle.checked = baseStyle.checked;
            garamondBtnStyle.over = baseStyle.over;
            garamondBtnStyle.disabled = baseStyle.disabled;
            garamondBtnStyle.fontColor = baseStyle.fontColor;
        } else {
            garamondBtnStyle.fontColor = Color.WHITE;
        }
        garamondBtnStyle.font = garamondButtonFont;

        TextButton back = new TextButton("Volver", garamondBtnStyle);
        back.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.restorePreviousScreen();
            }
        });

        t.add(title).padBottom(20);
        t.row();
        t.add(new Label("Volumen música", labelStyle)).padBottom(6);
        t.row();
        t.add(music).width(300).padBottom(12);
        t.row();
        t.add(back).width(220).height(60);

        stage.addActor(t);
    }

    private void generateFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Título grande
            param.size = 80;
            garamondTitleFont = generator.generateFont(param);

            // Labels medianos
            param.size = 36;
            garamondLabelFont = generator.generateFont(param);

            // Botones más grandes
            param.size = 48;
            garamondButtonFont = generator.generateFont(param);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("OptionsMenu", "Error cargando fuente Garamond: " + e.getMessage());
            garamondTitleFont = new BitmapFont();
            garamondLabelFont = new BitmapFont();
            garamondButtonFont = new BitmapFont();
        }
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCursorCatched(false);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        if (garamondTitleFont != null) garamondTitleFont.dispose();
        if (garamondLabelFont != null) garamondLabelFont.dispose();
        if (garamondButtonFont != null) garamondButtonFont.dispose();
    }
}
