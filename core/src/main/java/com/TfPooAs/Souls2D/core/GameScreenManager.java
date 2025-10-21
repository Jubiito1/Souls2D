package com.TfPooAs.Souls2D.core;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.TfPooAs.Souls2D.screens.GameScreen;
import com.TfPooAs.Souls2D.screens.MainMenuScreen;
import com.TfPooAs.Souls2D.screens.OptionsMenu;
import com.TfPooAs.Souls2D.screens.PauseMenu;

public class GameScreenManager {
    private final Main game;
    private Screen activeScreen;
    private Screen previousScreen; // <-- guardamos la pantalla previa (si abrimos opciones)

    public GameScreenManager(Main game) {
        this.game = game;
    }

    // Método central que decide si dispose() o no de la pantalla anterior
    private void setActiveScreenInternal(Screen newScreen, boolean keepPrevious) {
        if (activeScreen != null) {
            activeScreen.hide();
            if (!keepPrevious) {
                // eliminamos la pantalla anterior (liberamos recursos)
                activeScreen.dispose();
                previousScreen = null;
            } else {
                // guardamos la anterior para poder volver
                previousScreen = activeScreen;
            }
        }
        activeScreen = newScreen;
        game.setScreen(activeScreen);
    }

    // Mostrar una pantalla normal (destruye la anterior)
    public void setActiveScreenNormal(Screen newScreen) {
        setActiveScreenInternal(newScreen, false);
    }

    // Mostrar una pantalla pero conservar la anterior (uso para Options)
    public void setActiveScreenKeepPrevious(Screen newScreen) {
        setActiveScreenInternal(newScreen, true);
    }

    public void showGameScreen() {
        setActiveScreenNormal(new GameScreen(game));
    }

    public void showMainMenu() {
        setActiveScreenNormal(new MainMenuScreen(game));
    }

    // Abre Options y guarda la pantalla previa para poder volver
    public void showOptions(Screen returnTo) {
        // Creamos OptionsMenu con referencia a 'returnTo' si la querés, pero el GSM se encargará de la restauración
        setActiveScreenKeepPrevious(new OptionsMenu(game, returnTo));
    }

    // Abre PauseMenu (lo tratamos como pantalla normal)
    public void showPauseMenu(Screen gameScreen) {
        // Mantener la pantalla previa en memory para poder "continuar" correctamente
        setActiveScreenKeepPrevious(new PauseMenu(game, gameScreen));
    }

    // Restaurar la pantalla previa (call desde Options -> "Volver")
    public void restorePreviousScreen() {
        if (previousScreen != null) {
            // descartamos la pantalla actual (Options)
            if (activeScreen != null) {
                activeScreen.hide();
                activeScreen.dispose();
            }
            // restauramos la previa
            activeScreen = previousScreen;
            previousScreen = null;
            game.setScreen(activeScreen);
        } else {
            // fallback: si no hay previa, mostramos el MainMenu
            showMainMenu();
        }
    }

    public void update(float delta) {
        // lógica global futura
    }

    public void render() {
        if (activeScreen != null) activeScreen.render(Gdx.graphics.getDeltaTime());
    }

    public void resize(int width, int height) {
        if (activeScreen != null) activeScreen.resize(width, height);
    }

    public void dispose() {
        if (activeScreen != null) activeScreen.dispose();
        if (previousScreen != null) previousScreen.dispose();
    }
}
