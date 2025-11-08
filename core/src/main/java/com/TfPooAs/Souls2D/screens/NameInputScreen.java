package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class NameInputScreen implements Screen {
    private final Main game;
    private final int souls;
    private SpriteBatch batch;
    private BitmapFont font;
    private StringBuilder nameBuilder = new StringBuilder();
    private boolean enterPressed = false; // evita múltiples triggers

    public NameInputScreen(Main game, int souls) {
        this.game = game;
        this.souls = souls;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();

        // Procesar input de texto directamente con un InputProcessor
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (Character.isLetterOrDigit(character) && nameBuilder.length() < 10) {
                    nameBuilder.append(character);
                    return true;
                }
                if (character == '\b' && nameBuilder.length() > 0) {
                    nameBuilder.deleteCharAt(nameBuilder.length() - 1);
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER && !enterPressed && nameBuilder.length() > 0) {
                    enterPressed = true;
                    saveScore(nameBuilder.toString(), souls);
                    game.gsm.setActiveScreenNormal(new RankingScreen(game));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Ingresa tu nombre:", 100, 300);
        font.draw(batch, nameBuilder.toString() + "_", 100, 270);
        font.draw(batch, "Almas recolectadas: " + souls, 100, 230);
        font.draw(batch, "Presiona ENTER para guardar y ver ranking", 100, 200);
        batch.end();
    }

    private void saveScore(String playerName, int score) {
        File file = new File("ranking.txt");
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(playerName + "," + score + "\n");
            Gdx.app.log("NameInputScreen", "Guardado: " + playerName + " = " + score);
        } catch (IOException e) {
            Gdx.app.error("NameInputScreen", "Error guardando ranking", e);
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { batch.dispose(); font.dispose(); }
}
