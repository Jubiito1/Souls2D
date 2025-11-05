package com.TfPooAs.Souls2D.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * DialogBox muy simple basado en Scene2D. Muestra líneas de diálogo secuenciales.
 */
public class DialogBox {
    private final Stage stage;
    private final Skin skin;
    private final Window window;
    private String[] lines = new String[0];
    private int index = -1;

    public DialogBox(Stage stage) {
        this.stage = stage;
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        window = new Window("", skin);
        window.setMovable(false);
        window.setVisible(false);

        // Usamos una tabla interna para el texto
        Table content = new Table();
        Label textLabel = new Label("", skin, "default");
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.topLeft);
        content.add(textLabel).expand().fill().pad(15f);
        window.add(content).expand().fill();

        // Posicionar y dimensionar: ocupando la parte baja de la pantalla
        Viewport vp = stage.getViewport();
        float w = vp.getWorldWidth();
        float h = vp.getWorldHeight();
        window.setSize(w * 0.8f, h * 0.3f);
        window.setPosition(w * 0.1f, h * 0.05f);

        stage.addActor(window);
    }

    public boolean isVisible() { return window.isVisible(); }

    public void show(String[] dialogLines) {
        if (dialogLines == null || dialogLines.length == 0) return;
        this.lines = dialogLines;
        this.index = 0;
        updateText();
        window.setVisible(true);
    }

    public void hide() {
        window.setVisible(false);
        index = -1;
        lines = new String[0];
    }

    public void next() {
        if (!isVisible()) return;
        index++;
        if (index >= lines.length) {
            hide();
        } else {
            updateText();
        }
    }

    private void updateText() {
        // Window -> first child is Table -> first cell actor is Label
        Table table = (Table) window.getChildren().first();
        Label label = (Label) table.getChildren().first();
        label.setText(lines[index]);
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public Stage getStage() { return stage; }
}
