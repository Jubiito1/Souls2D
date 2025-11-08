package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.*;
import java.util.*;

public class RankingScreen implements Screen {

    private final Main game;
    private Stage stage;
    private BitmapFont titleFont, entryFont, infoFont;
    private List<ScoreEntry> scores;

    private static class ScoreEntry {
        String name;
        int score;
        ScoreEntry(String n, int s) { name = n; score = s; }
    }

    public RankingScreen(Main game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.scores = loadScores();
        generateFonts();
        buildUI();
    }

    /** Carga el ranking desde ranking.txt */
    private List<ScoreEntry> loadScores() {
        List<ScoreEntry> list = new ArrayList<>();
        File file = new File("ranking.txt");
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    list.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (IOException ignored) {}

        list.sort((a, b) -> Integer.compare(b.score, a.score));
        return list;
    }

    /** Genera fuentes Garamond iguales a las del menú principal */
    private void generateFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/Garamond.otf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.size = 110;
            titleFont = generator.generateFont(parameter);

            parameter.size = 50;
            entryFont = generator.generateFont(parameter);

            parameter.size = 40;
            infoFont = generator.generateFont(parameter);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("RankingScreen", "No se pudo cargar Garamond.otf: " + e.getMessage());
            titleFont = new BitmapFont();
            entryFont = new BitmapFont();
            infoFont = new BitmapFont();
        }
    }

    /** Construye la interfaz centrada con tabla */
    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Título
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        Label title = new Label("RANKING DE JUGADORES", titleStyle);
        root.add(title).padBottom(80f);
        root.row();

        // Lista de puntajes
        Label.LabelStyle entryStyle = new Label.LabelStyle(entryFont, Color.LIGHT_GRAY);
        int rank = 1;
        for (ScoreEntry s : scores) {
            if (rank > 10) break;
            Label entry = new Label(rank + ". " + s.name + " - " + s.score + " almas", entryStyle);
            root.add(entry).pad(5f);
            root.row();
            rank++;
        }

        if (scores.isEmpty()) {
            Label empty = new Label("No hay puntuaciones aún.", entryStyle);
            root.add(empty).padBottom(30f);
            root.row();
        }

        // Instrucción
        Label.LabelStyle infoStyle = new Label.LabelStyle(infoFont, new Color(0.8f, 0.8f, 0.8f, 1f));
        Label info = new Label("Presiona ESPACIO para continuar", infoStyle);
        root.add(info).padTop(60f);
        root.row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.gsm.showVictoryScreen();
        }
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
        titleFont.dispose();
        entryFont.dispose();
        infoFont.dispose();
    }
}
