package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class VictoryScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont hintFont;
    private final GlyphLayout layout = new GlyphLayout();

    // Dimensiones virtuales (ajustables)
    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 480f;


    public VictoryScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch = new SpriteBatch();
        generateFonts();
    }

    /** Genera las fuentes Garamond (intenta ui/Garamond.otf y cae a BitmapFont si falla). */
    private void generateFonts() {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Título grande
            param.size = 56;
            param.color = Color.WHITE;
            titleFont = gen.generateFont(param);

            // Subtítulo / mensaje
            param.size = 28;
            param.color = Color.WHITE;
            subtitleFont = gen.generateFont(param);

            // Hints (R / ESC)
            param.size = 20;
            param.color = Color.LIGHT_GRAY;
            hintFont = gen.generateFont(param);

            gen.dispose();
        } catch (Exception e) {
            Gdx.app.error("VictoryScreen", "No se pudo cargar Garamond (ui/Garamond.otf). Usando BitmapFont por defecto: " + e.getMessage());
            titleFont = new BitmapFont();
            subtitleFont = new BitmapFont();
            hintFont = new BitmapFont();
            titleFont.setColor(Color.WHITE);
            subtitleFont.setColor(Color.WHITE);
            hintFont.setColor(Color.LIGHT_GRAY);
        }
    }

    @Override
    public void show() {
        // Nos aseguramos de captar input del teclado (si necesitás Stage, podemos agregarlo después).
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
    }

    @Override
    public void render(float delta) {
        // Clear
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Mensajes
        String title = "¡Has completado el juego!";
        String subtitle = "Gracias por jugar. La llama ardera eternamente";
        String hintR = "Presiona R para reiniciar";
        String hintEsc = "Presiona ESC para volver al menú principal";

        batch.begin();
        // Título centrado (arriba)
        layout.setText(titleFont, title);
        float x = (VIRTUAL_WIDTH - layout.width) / 2f;
        float y = VIRTUAL_HEIGHT * 0.65f + layout.height / 2f;
        titleFont.draw(batch, layout, x, y);

        // Subtítulo centrado (debajo del título)
        layout.setText(subtitleFont, subtitle);
        x = (VIRTUAL_WIDTH - layout.width) / 2f;
        y = VIRTUAL_HEIGHT * 0.55f + layout.height / 2f;
        subtitleFont.draw(batch, layout, x, y);

        // Hints centrados (más abajo)
        layout.setText(hintFont, hintR);
        x = (VIRTUAL_WIDTH - layout.width) / 2f;
        y = VIRTUAL_HEIGHT * 0.38f + layout.height / 2f;
        hintFont.draw(batch, layout, x, y);

        layout.setText(hintFont, hintEsc);
        x = (VIRTUAL_WIDTH - layout.width) / 2f;
        y = VIRTUAL_HEIGHT * 0.32f + layout.height / 2f;
        hintFont.draw(batch, layout, x, y);

        batch.end();

        // --- Controles: manejar aquí los cambios de pantalla ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // Reiniciar el juego: usar GameScreenManager si está disponible
            try {
                if (game != null && game.gsm != null) {
                    // Pedimos al GSM que abra el GameScreen empezando desde el lastSave
                    game.gsm.showGameScreen(true);
                } else if (game != null) {
                    // Fallback directo
                    game.setScreen(new com.TfPooAs.Souls2D.screens.GameScreen(game, true));
                }
            } catch (Exception e) {
                Gdx.app.error("VictoryScreen", "Error al reiniciar el juego: " + e.getMessage(), e);
                // Intent fallback directo recién por si algo falló
                try { game.setScreen(new com.TfPooAs.Souls2D.screens.GameScreen(game, true)); } catch (Exception ignored) {}
            }
            // No llamamos dispose() aquí: el GSM / setScreen se encargará de hide()/dispose() correctamente.
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            try {
                if (game != null && game.gsm != null) {
                    game.gsm.showMainMenu();
                } else if (game != null) {
                    game.setScreen(new com.TfPooAs.Souls2D.screens.MainMenuScreen(game));
                }
            } catch (Exception e) {
                Gdx.app.error("VictoryScreen", "Error al volver al menú: " + e.getMessage(), e);
                try { game.setScreen(new com.TfPooAs.Souls2D.screens.MainMenuScreen(game)); } catch (Exception ignored) {}
            }
            return;
        }
    }

    @Override
    public void resize(int width, int height) {
        // si querés escalar la cámara/proyección para mantener centrado, podés hacerlo aquí.
        // Actualmente usamos coordenadas virtuales fijas, por eso no hacemos nada especial.
    }

    @Override
    public void pause() { /* no-op */ }

    @Override
    public void resume() { /* no-op */ }

    @Override
    public void hide() {
        // liberamos input catch
        Gdx.input.setCatchKey(Input.Keys.BACK, false);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (titleFont != null) titleFont.dispose();
        if (subtitleFont != null) subtitleFont.dispose();
        if (hintFont != null) hintFont.dispose();
    }
}

