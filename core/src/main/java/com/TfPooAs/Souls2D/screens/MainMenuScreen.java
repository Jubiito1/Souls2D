package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.TfPooAs.Souls2D.systems.SaveSystem;
import com.TfPooAs.Souls2D.systems.SoundManager;

public class MainMenuScreen implements Screen {
    private final Main game;
    private Stage stage;

    // Fuentes personalizadas
    private BitmapFont garamondTitleFont;
    private BitmapFont garamondSubtitleFont;
    private BitmapFont garamondButtonFont;

    public MainMenuScreen(Main game) {
        this.game = game;
        init();
    }

    private void init() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Genera las fuentes Garamond
        generateFonts();

        // === Título y subtítulo ===
        Table titleTable = new Table();
        titleTable.setFillParent(true);
        titleTable.top();
        titleTable.padTop(80f);

        Label.LabelStyle titleStyle = new Label.LabelStyle(garamondTitleFont, Color.WHITE);
        Label.LabelStyle subtitleStyle = new Label.LabelStyle(garamondSubtitleFont, new Color(0.8f, 0.8f, 0.8f, 1f));

        Label titleLabel = new Label("ECHOES OF ABYSS", titleStyle);
        Label subtitleLabel = new Label("A Dark Souls Story", subtitleStyle);

        titleTable.add(titleLabel).padBottom(10f);
        titleTable.row();
        titleTable.add(subtitleLabel).padBottom(20f);
        stage.addActor(titleTable);

        // === Botones ===
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().padBottom(80f);

        // Crear estilo personalizado de botón con la fuente Garamond
        TextButtonStyle garamondButtonStyle = new TextButtonStyle();
        garamondButtonStyle.font = garamondButtonFont;
        garamondButtonStyle.fontColor = Color.WHITE;

        // Crear botones con estilo nuevo
        TextButton newGame = new TextButton("Nueva Partida", garamondButtonStyle);
        TextButton cont = new TextButton("Continuar", garamondButtonStyle);
        TextButton options = new TextButton("Audio", garamondButtonStyle);
        TextButton exit = new TextButton("Salir", garamondButtonStyle);

        // Ajustar tamaño
        float btnW = 480f, btnH = 72f, pad = 14f;
        table.add(newGame).width(btnW).height(btnH).pad(pad);
        table.row();
        table.add(cont).width(btnW).height(btnH).pad(pad);
        table.row();
        table.add(options).width(btnW).height(btnH).pad(pad);
        table.row();
        table.add(exit).width(btnW).height(btnH).pad(pad);

        stage.addActor(table);

        // === Listeners ===
        newGame.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showGameScreen();
            }
        });

        cont.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showGameScreen(true);
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

        // Deshabilitar "Continuar" si no hay partida
        cont.setDisabled(!SaveSystem.hasLastBonfire());
    }

    /** Genera las fuentes Garamond: título, subtítulo y botones. */
    private void generateFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Fuente del título (muy grande)
            parameter.size = 160;
            garamondTitleFont = generator.generateFont(parameter);

            // Fuente del subtítulo (más pequeña)
            parameter.size = 90;
            garamondSubtitleFont = generator.generateFont(parameter);

            // Fuente de los botones
            parameter.size = 60;
            garamondButtonFont = generator.generateFont(parameter);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "No se pudo generar la fuente AdobeGaramond-Bold.otf: " + e.getMessage());
            garamondTitleFont = new BitmapFont();
            garamondSubtitleFont = new BitmapFont();
            garamondButtonFont = new BitmapFont();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // Play menu background music (loop). Safe if file is missing.
        SoundManager.playBackground("menu.wav", true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() { SoundManager.pauseBackground(); }
    @Override public void resume() { SoundManager.resumeBackground(); }
    @Override public void hide() { SoundManager.stopBackground(); }

    @Override
    public void dispose() {
        stage.dispose();
        garamondTitleFont.dispose();
        garamondSubtitleFont.dispose();
        garamondButtonFont.dispose();
    }
}
