package com.TfPooAs.Souls2D.screens;

import com.TfPooAs.Souls2D.core.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MainMenuScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Image backgroundImage;

    public MainMenuScreen(Main game) {
        this.game = game;
        init();
    }

    private void init() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Carga skin (asegurate de tener uiskin.json en core/assets/ui/)
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Cargar imagen de fondo (asegurate de poner menu_bg.png en core/assets/ui/)
        Texture bgTex = new Texture(Gdx.files.internal("ui/menu_bg.png"));
        backgroundImage = new Image(bgTex);
        backgroundImage.setFillParent(true); // hace que ocupe toda la viewport del stage
        backgroundImage.setScaling(com.badlogic.gdx.utils.Scaling.fill); // rellena la pantalla (puede recortar)
        stage.addActor(backgroundImage); // añadimos primero el fondo (detrás)

        // Tabla para botones
        Table table = new Table();
        table.setFillParent(true);
        table.bottom(); // alineamos al fondo de la pantalla
        table.padBottom(50f); // separacion desde el borde inferior (ajusta este valor)

        // Botones
        TextButton newGame = new TextButton("Nueva Partida", skin);
        TextButton cont = new TextButton("Continuar", skin);
        TextButton options = new TextButton("Opciones", skin);
        TextButton exit = new TextButton("Salir", skin);

        newGame.getLabel().setFontScale(2.0f);
        cont.getLabel().setFontScale(2.0f);
        options.getLabel().setFontScale(2.0f);
        exit.getLabel().setFontScale(2.0f);

        // Ejemplo: deshabilitar Continuar si no hay save
        cont.setDisabled(true);

        // Listeners
        newGame.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showGameScreen();
            }
        });

        cont.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.gsm.showGameScreen(); // más adelante conectar con SaveSystem
            }
        });


        options.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // Abrir opciones y mantener esta pantalla guardada por el GSM
                game.gsm.showOptions(MainMenuScreen.this);
            }
        });

        exit.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        // Layout: podemos apilar verticalmente los botones con espaciado
        float btnW = 420f, btnH = 64f, pad = 8f;
        table.add(newGame).width(btnW).height(btnH).pad(pad);
        table.row();
        table.add(cont).width(btnW).height(btnH).pad(pad);
        table.row();
        table.add(options).width(btnW).height(btnH).pad(pad);
        table.row();
        table.add(exit).width(btnW).height(btnH).pad(pad);

        // Añadimos la tabla **después** del background (para que quede en frente)
        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(delta, 1/30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        // si querés forzar reescalado del background manual:
        if (backgroundImage != null) {
            backgroundImage.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        // IMPORTANTE: Si reutilizás ese texture en otros lugares NO hagas dispose() aquí;
        // si solo se usa aquí, está bien:
      //  backgroundImage.getDrawable().getTexture().dispose();
    }
}
