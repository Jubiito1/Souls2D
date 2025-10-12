package com.TfPooAs.Souls2D.core;

import com.TfPooAs.Souls2D.screens.MainMenuScreen;
import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new MainMenuScreen());
    }
}
