package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.TfPooAs.Souls2D.systems.SoundManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * PauseOverlay: overlay de UI que se dibuja sobre GameScreen sin cambiar de Screen.
 * Ahora es totalmente responsive y se adapta a distintas resoluciones.
 */
public class PauseOverlay {
    private final Stage stage;
    private final Skin skin;
    private final Texture dimTexture;
    private final GameScreen gameScreen;

    // Fuentes Garamond
    private BitmapFont garamondTitleFont;
    private BitmapFont garamondButtonFont;

    public PauseOverlay(Main game, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        dimTexture = create1x1Texture();

        generateFonts();
        buildUI();
    }

    // ========= GENERACIÓN RESPONSIVE DE FUENTES =========
    private void generateFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Escalado relativo a la altura de pantalla
            float screenH = Gdx.graphics.getHeight();
            parameter.size = (int) (screenH * 0.10f); // ≈10% para título
            garamondTitleFont = generator.generateFont(parameter);

            parameter.size = (int) (screenH * 0.045f); // ≈4.5% para botones
            garamondButtonFont = generator.generateFont(parameter);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("PauseOverlay", "No se pudo generar la fuente Garamond: " + e.getMessage());
            garamondTitleFont = new BitmapFont();
            garamondButtonFont = new BitmapFont();
        }
    }

    // ========= CONSTRUCCIÓN RESPONSIVE DEL UI =========
    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        // Escalado dinámico
        float screenH = Gdx.graphics.getHeight();
        float screenW = Gdx.graphics.getWidth();

        float titlePadBottom = screenH * 0.35f; // separación del título
        float btnW = screenW * 0.25f;
        float btnH = screenH * 0.08f;
        float pad = screenH * 0.015f;

        // Título
        Label.LabelStyle titleStyle = new Label.LabelStyle(garamondTitleFont, Color.WHITE);
        Label title = new Label("JUEGO PAUSADO", titleStyle);

        // Botones
        TextButtonStyle baseStyle = null;
        try { baseStyle = skin.get(TextButtonStyle.class); } catch (Exception ignored) {}

        TextButtonStyle btnStyle = new TextButtonStyle();
        if (baseStyle != null) {
            btnStyle.up = baseStyle.up;
            btnStyle.down = baseStyle.down;
            btnStyle.checked = baseStyle.checked;
            btnStyle.over = baseStyle.over;
            btnStyle.disabled = baseStyle.disabled;
            btnStyle.fontColor = baseStyle.fontColor;
        } else {
            btnStyle.fontColor = Color.WHITE;
        }
        btnStyle.font = garamondButtonFont;

        TextButton resume = new TextButton("Continuar", btnStyle);
        TextButton options = new TextButton("Audio", btnStyle);
        TextButton quit = new TextButton("Salir al menú", btnStyle);

        // Listeners
        resume.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                gameScreen.resumeFromPause();
            }
        });

        options.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                gameScreen.getGame().gsm.showOptions(gameScreen);
            }
        });

        quit.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                gameScreen.getGame().gsm.showMainMenu();
            }
        });

        // Layout responsive
        root.add(title).padBottom(titlePadBottom);
        root.row();
        root.add(resume).width(btnW).height(btnH).pad(pad);
        root.row();
        root.add(options).width(btnW).height(btnH).pad(pad);
        root.row();
        root.add(quit).width(btnW).height(btnH).pad(pad);

        stage.addActor(root);
    }

    // ========= RENDER =========
    public void render(float delta) {
        stage.act(delta);

        // Dimming
        stage.getBatch().begin();
        stage.getBatch().setColor(0f, 0f, 0f, 0.70f);
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();
        stage.getBatch().draw(dimTexture, 0, 0, w, h);
        stage.getBatch().setColor(1f, 1f, 1f, 1f);
        stage.getBatch().end();

        stage.draw();
    }

    public void show() {
        Gdx.input.setCursorCatched(false);
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.input.setInputProcessor(stage);
    }

    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    public void dispose() {
        try { SoundManager.stopLoop("assets/pausa.wav"); } catch (Exception ignored) {}
        stage.dispose();
        skin.dispose();
        dimTexture.dispose();
        if (garamondTitleFont != null) garamondTitleFont.dispose();
        if (garamondButtonFont != null) garamondButtonFont.dispose();
    }

    private Texture create1x1Texture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    public Stage getStage() { return stage; }
}
