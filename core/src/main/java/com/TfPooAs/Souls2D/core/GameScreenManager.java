package com.TfPooAs.Souls2D.core;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.TfPooAs.Souls2D.screens.GameScreen;

public class GameScreenManager {
    private final Main game;
    private Screen activeScreen;

    public GameScreenManager(Main game) {
        this.game = game;
    }

    public void showGameScreen() {
        if (activeScreen != null) activeScreen.dispose();
        activeScreen = new GameScreen(game);
        game.setScreen(activeScreen);
    }

    public void update(float delta) {
        // lógica global futura (si hace falta)
    }

    public void render() {
        if (activeScreen != null) activeScreen.render(Gdx.graphics.getDeltaTime());
    }

    public void resize(int width, int height) {
        if (activeScreen != null) activeScreen.resize(width, height);
    }

    public void dispose() {
        if (activeScreen != null) activeScreen.dispose(); }

    public void hachatipazo() {

    }
}
