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

        // === Escalado responsivo ===
        float scaleX = Gdx.graphics.getWidth() / 1920f;
        float scaleY = Gdx.graphics.getHeight() / 1080f;
        float scale = Math.min(scaleX, scaleY); // Mantiene proporción
        float baseBtnW = 220f * scale;
        float baseBtnH = 60f * scale;
        float baseWidth = 300f * scale;
        float basePad = 20f * scale;

        // === Estilo de título ===
        Label.LabelStyle titleStyle = new Label.LabelStyle(garamondTitleFont, Color.WHITE);
        Label title = new Label("Opciones", titleStyle);

        // === Slider de volumen ===
        Slider music = new Slider(0f, 1f, 0.01f, false, skin);
        music.setValue(prefs.getFloat("musicVolume", 1f));
        music.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("musicVolume", music.getValue());
                prefs.flush();
            }
        });

        // === Estilo de labels ===
        Label.LabelStyle labelStyle = new Label.LabelStyle(garamondLabelFont, Color.WHITE);

        // === Estilo de botones ===
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

        // === Organización ===
        t.add(title).padBottom(basePad);
        t.row();
        t.add(new Label("Volumen música", labelStyle)).padBottom(6f * scale);
        t.row();
        t.add(music).width(baseWidth).padBottom(12f * scale);
        t.row();
        t.add(back).width(baseBtnW).height(baseBtnH);

        stage.addActor(t);
    }

    private void generateFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Escalado de fuentes responsivo
            float scaleX = Gdx.graphics.getWidth() / 1920f;
            float scaleY = Gdx.graphics.getHeight() / 1080f;
            float scale = Math.min(scaleX, scaleY);

            // Tamaños relativos
            param.size = Math.round(80 * scale);
            garamondTitleFont = generator.generateFont(param);

            param.size = Math.round(36 * scale);
            garamondLabelFont = generator.generateFont(param);

            param.size = Math.round(48 * scale);
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
