package com.TfPooAs.Souls2D.entities.npcs;

import com.TfPooAs.Souls2D.entities.NPC;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;


/**
 * FireKeeper animada (4 frames en una fila).
 * Cada frame dura 0.25s, bucle infinito.
 */
public class FireKeeper extends NPC implements Disposable {

    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private Texture spriteSheet;

    private final String[] dialog = new String[]{
        "Haz permitido el acceso al santuario del enlace de fuego",
        "La llama primigenia estará eternamente agradecida",
        "Puedes volver a ser parte de ella..."
    };

    public FireKeeper(float x, float y) {
        super(x, y);
        this.setInteractionRadius(100f);
        loadAnimation("firekeeper-Sheet.png", 4, 1, 0.25f); // 4 frames horizontales
    }

    private boolean playerNearby = false;
    private BitmapFont font = new BitmapFont();
    private int currentLine = 0;
    private boolean talking = false;

    private void loadAnimation(String path, int cols, int rows, float frameDuration) {
        spriteSheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tmp = TextureRegion.split(
            spriteSheet,
            spriteSheet.getWidth() / cols,
            spriteSheet.getHeight() / rows
        );
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        animation = new Animation<>(frameDuration, frames);
    }


    public void update(float delta, Player player) {
        super.update(delta);
        stateTime += delta;

        // --- Detección de distancia ---
        if (player != null) {
            float dx = player.getPosition().x - position.x;
            float dy = player.getPosition().y - position.y;
            playerNearby = (dx * dx + dy * dy) <= (interactionRadius * interactionRadius);
        } else {
            playerNearby = false;
        }

        // --- Interacción ---
        if (playerNearby && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            talking = true;
            currentLine++;
            if (currentLine >= dialog.length) {
                currentLine = 0;
                talking = false;
            }
        }
    }


    @Override
    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, position.x, position.y, currentFrame.getRegionWidth(), currentFrame.getRegionHeight());

        if (playerNearby && !talking) {
            font.draw(batch, "Presiona E para hablar", position.x - 20, position.y + currentFrame.getRegionHeight() + 20);
        }

        if (talking) {
            font.draw(batch, dialog[currentLine], position.x - 40, position.y + currentFrame.getRegionHeight() + 40);
        }
    }


    @Override
    public void dispose() {
        super.dispose();
        if (spriteSheet != null) spriteSheet.dispose();
    }

    public String[] getDialog() { return dialog; }
}
