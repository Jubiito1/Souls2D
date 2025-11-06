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
 * DeathOverlay: Overlay de UI que se dibuja sobre GameScreen al morir.
 * Muestra un título grande centrado "HAS MUERTO" y dos botones:
 *  - Reintentar: reinicia la partida (vuelve a cargar GameScreen)
 *  - Volver al menú principal
 *
 * Ahora usa Adobe Garamond Bold (bitmap generado en runtime) para título y botones.
 */
public class DeathOverlay {
    private final Stage stage;
    private final Skin skin;
    private final Texture dimTexture; // 1x1 para dimming
    private final GameScreen gameScreen;

    // Fuentes Garamond
    private BitmapFont garamondTitleFont;
    private BitmapFont garamondButtonFont;

    public DeathOverlay(Main game, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.dimTexture = create1x1Texture();

        generateFonts();
        buildUI(game);
    }

    private void generateFonts() {
        try {
            // Ruta esperada: core/assets/fonts/AdobeGaramond-Bold.otf
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("assets/ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Fuente para el título (muy grande)
            param.size = 120; // ajustá si querés más/menos
            param.spaceX = 0;
            param.spaceY = 0;
            garamondTitleFont = generator.generateFont(param);

            // Fuente para los botones / labels (más chica)
            param.size = 48; // ajustá según cómo se vea en tus botones
            garamondButtonFont = generator.generateFont(param);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("DeathOverlay", "No se pudo generar Garamond: " + e.getMessage());
            // Fallback a fuente por defecto para evitar crashes
            garamondTitleFont = new BitmapFont();
            garamondButtonFont = new BitmapFont();
        }
    }

    private void buildUI(Main game) {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        // Estilo de label (título) usando Garamond grande
        Label.LabelStyle titleStyle = new Label.LabelStyle(garamondTitleFont, Color.RED);
        Label title = new Label("HAS MUERTO", titleStyle);
        // NO usamos setFontScale gigantesco; ya controlamos por size al generar la fuente.

        // Estilo de botones: clonamos drawables del skin si existen; si no, dejamos sin fondo
        TextButtonStyle baseStyle = null;
        try {
            baseStyle = skin.get(TextButtonStyle.class);
        } catch (Exception ignored) { }

        TextButtonStyle garamondBtnStyle = new TextButtonStyle();
        if (baseStyle != null) {
            garamondBtnStyle.up = baseStyle.up;
            garamondBtnStyle.down = baseStyle.down;
            garamondBtnStyle.checked = baseStyle.checked;
            garamondBtnStyle.over = baseStyle.over;
            garamondBtnStyle.disabled = baseStyle.disabled;
            garamondBtnStyle.fontColor = baseStyle.fontColor;
        } else {
            // Si no hay drawables en el skin, definimos al menos un fontColor
            garamondBtnStyle.fontColor = Color.WHITE;
        }
        garamondBtnStyle.font = garamondButtonFont;

        TextButton retry = new TextButton("Reintentar", garamondBtnStyle);
        TextButton toMenu = new TextButton("Menú principal", garamondBtnStyle);

        // Si antes escalabas texto en labels de botones, ya no hace falta:
        // retry.getLabel().setFontScale(2.0f);

        // Listeners
        retry.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showGameScreen();
            }
        });

        toMenu.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showMainMenu();
            }
        });

        float btnW = 400f, btnH = 64f, pad = 14f;
        root.add(title).padBottom(40f);
        root.row();
        root.add(retry).width(btnW).height(btnH).pad(pad);
        root.row();
        root.add(toMenu).width(btnW).height(btnH).pad(pad);

        stage.addActor(root);
    }

    public void render(float delta) {
        stage.act(delta);
        // Dimming desactivado
        stage.getBatch().begin();
        stage.getBatch().setColor(0f, 0f, 0f, 0.45f);
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
