package com.TfPooAs.Souls2D.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.TfPooAs.Souls2D.screens.MainMenuScreen;

public class Main extends Game {
    public GameScreenManager gsm;

    @Override
    public void create() {
        gsm = new GameScreenManager(this);
        // Arrancamos mostrando el menú principal
        gsm.showMainMenu();
    }

    @Override
    public void render() {
        // Limpia la pantalla antes de delegar al GSM
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        gsm.update(Gdx.graphics.getDeltaTime());
        gsm.render();
    }

    @Override
    public void resize(int width, int height) {
        if (gsm != null) {
            gsm.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        if (gsm != null) {
            gsm.dispose();
        }
    }
}
