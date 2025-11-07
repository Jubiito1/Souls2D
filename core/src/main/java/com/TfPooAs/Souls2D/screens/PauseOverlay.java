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
 * Ahora muestra un título centrado "JUEGO PAUSADO" y botones con fuente Garamond.
 */
public class PauseOverlay {
    private final Stage stage;
    private final Skin skin;
    private final Texture dimTexture; // 1x1 texture para dimming
    private final GameScreen gameScreen;

    // Fuentes Garamond
    private BitmapFont garamondTitleFont;
    private BitmapFont garamondButtonFont;

    public PauseOverlay(Main game, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        dimTexture = create1x1Texture();

        generateFonts(); // genera las fuentes Garamond
        buildUI();
    }

    /**
     * Genera los BitmapFont a partir del OTF.
     * Ajustá `parameter.size` si querés otro tamaño para título / botones.
     */
    private void generateFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Título (grande)
            parameter.size = 120; // probá 72, 96, 120 según prefieras
            parameter.spaceX = 0;
            parameter.spaceY = 0;
            garamondTitleFont = generator.generateFont(parameter);

            // Botones (más pequeño)
            parameter.size = 40;
            garamondButtonFont = generator.generateFont(parameter);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("PauseOverlay", "No se pudo generar la fuente Garamond: " + e.getMessage());
            garamondTitleFont = new BitmapFont();
            garamondButtonFont = new BitmapFont();
        }
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        // Título centrado en la pantalla
        Label.LabelStyle titleStyle = new Label.LabelStyle(garamondTitleFont, Color.WHITE);
        Label title = new Label("JUEGO PAUSADO", titleStyle);

        // Preparar estilo de botón clonando el style del skin si existe
        TextButtonStyle baseStyle = null;
        try {
            baseStyle = skin.get(TextButtonStyle.class);
        } catch (Exception ignored) {}

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

        // Si antes escalabas con setFontScale(2f), ahora en general no hace falta
        // porque elegimos el tamaño correcto al generar la fuente. Si necesitás
        // escalar adicionalmente, podes volver a usar setFontScale().
        // resume.getLabel().setFontScale(2.0f);

        // Resume: solo despausa el juego
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

        // Layout: título arriba, botones centrados debajo
        float btnW = 300f, btnH = 56f, pad = 8f;
        root.add(title).colspan(1).padBottom(400f);
        root.row();
        root.add(resume).width(btnW).height(btnH).pad(pad);
        root.row();
        root.add(options).width(btnW).height(btnH).pad(pad);
        root.row();
        root.add(quit).width(btnW).height(btnH).pad(pad);

        stage.addActor(root);
    }

    // Dibuja el overlay (dimming + stage)
    public void render(float delta) {
        stage.act(delta);

        // Dimming desactivado
        stage.getBatch().begin();
        stage.getBatch().setColor(0f, 0f, 0f, 0.70f);
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();
        stage.getBatch().draw(dimTexture, 0, 0, w, h);
        stage.getBatch().setColor(1f, 1f, 1f, 1f);
        stage.getBatch().end();

        // Dibujamos los widgets (botones y título)
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public void show() {
        // Actualizamos viewport al mostrar
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.input.setInputProcessor(stage);
        // Iniciar música/loop de pausa
        try { SoundManager.ensureLooping("pausa.wav"); } catch (Exception ignored) {}
    }

    public void hide() {
        Gdx.input.setInputProcessor(null); // o el InputProcessor del juego si lo tenés
        // Detener música/loop de pausa al ocultar
        try { SoundManager.stopLoop("pausa.wav"); } catch (Exception ignored) {}
    }

    public void dispose() {
        // Asegurar detener el loop de pausa si seguía activo
        try { SoundManager.stopLoop("pausa.wav"); } catch (Exception ignored) {}
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
}
