package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.Color;
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
 * DeathOverlay responsive: Se adapta automáticamente al tamaño de pantalla.
 */
public class DeathOverlay {
    private final Stage stage;
    private final Skin skin;
    private final Texture dimTexture;
    private final GameScreen gameScreen;

    private BitmapFont garamondTitleFont;
    private BitmapFont garamondButtonFont;

    // Guardamos dimensiones para escalar
    private float screenWidth;
    private float screenHeight;

    public DeathOverlay(Main game, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.dimTexture = create1x1Texture();

        updateScreenSize();
        generateFonts();
        buildUI(game);
    }

    private void updateScreenSize() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
    }

    private void generateFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("assets/ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Ajustamos el tamaño proporcionalmente a la altura de pantalla
            param.size = (int)(screenHeight * 0.12f); // título ≈ 12% de altura
            garamondTitleFont = generator.generateFont(param);

            param.size = (int)(screenHeight * 0.05f); // botones ≈ 5% de altura
            garamondButtonFont = generator.generateFont(param);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("DeathOverlay", "No se pudo generar Garamond: " + e.getMessage());
            garamondTitleFont = new BitmapFont();
            garamondButtonFont = new BitmapFont();
        }
    }

    private void buildUI(Main game) {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label.LabelStyle titleStyle = new Label.LabelStyle(garamondTitleFont, Color.RED);
        Label title = new Label("HAS MUERTO", titleStyle);

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

        TextButton retry = new TextButton("Reintentar", garamondBtnStyle);
        TextButton toMenu = new TextButton("Menú principal", garamondBtnStyle);

        retry.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showGameScreen(true);
            }
        });

        toMenu.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showMainMenu();
            }
        });

        // Escalamos botones proporcionalmente
        float btnW = screenWidth * 0.35f;
        float btnH = screenHeight * 0.08f;
        float pad = screenHeight * 0.015f;
        float titlePadBottom = screenHeight * 0.06f;

        root.add(title).padBottom(titlePadBottom);
        root.row();
        root.add(retry).width(btnW).height(btnH).pad(pad);
        root.row();
        root.add(toMenu).width(btnW).height(btnH).pad(pad);

        stage.addActor(root);
    }

    public void render(float delta) {
        // Si la pantalla cambia de tamaño, se ajusta
        if (screenWidth != Gdx.graphics.getWidth() || screenHeight != Gdx.graphics.getHeight()) {
            stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            updateScreenSize();
        }

        stage.act(delta);

        stage.getBatch().begin();
        stage.getBatch().setColor(0f, 0f, 0f, 0.45f);
        float w = stage.getViewport().getWorldWidth();
        float h = stage.getViewport().getWorldHeight();
        stage.getBatch().draw(dimTexture, 0, 0, w, h);
        stage.getBatch().setColor(1f, 1f, 1f, 1f);
        stage.getBatch().end();

        stage.draw();
    }

    public Stage getStage() { return stage; }

    public void show() {
        Gdx.input.setCursorCatched(false);
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
