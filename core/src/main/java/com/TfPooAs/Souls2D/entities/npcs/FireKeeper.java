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
import com.TfPooAs.Souls2D.screens.GameScreen;

/**
 * FireKeeper animada (4 frames en una fila).
 * Cada frame dura 0.25s, bucle infinito.
 */
public class FireKeeper extends NPC implements Disposable {

    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private Texture spriteSheet;
    private final com.TfPooAs.Souls2D.core.Main game;
    private final GameScreen gameScreen; // Referencia al GameScreen (puede ser null)

    private final String[] dialog = new String[]{
        "Haz permitido el acceso al santuario del enlace de fuego",
        "La llama primigenia estará eternamente agradecida",
        "Puedes volver a ser parte de ella..."
    };

    // Mensajes cuando el boss está vivo
    private final String[] bossAliveMessages = new String[]{
        "El Iudex Gundyr aún vive...",
        "No puedo ayudarte hasta que derrotes al guardián",
        "Debes enfrentar al juez antes de continuar"
    };

    public FireKeeper(float x, float y, com.TfPooAs.Souls2D.core.Main game) {
        super(x, y);
        this.game = game;
        this.gameScreen = null;
        this.setInteractionRadius(100f);
        loadAnimation("firekeeper-Sheet.png", 4, 1, 0.25f);
    }

    // Constructor que acepta GameScreen
    public FireKeeper(float x, float y, com.TfPooAs.Souls2D.core.Main game, GameScreen gameScreen) {
        super(x, y);
        this.game = game;
        this.gameScreen = gameScreen;
        this.setInteractionRadius(100f);
        loadAnimation("firekeeper-Sheet.png", 4, 1, 0.25f);
    }

    private boolean playerNearby = false;
    private BitmapFont font = new BitmapFont();
    private int currentLine = 0;
    private boolean talking = false;
    private int currentBossAliveMessageIndex = 0; // Para rotar mensajes cuando el boss está vivo

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

    // Verifica si el IudexGundyr está vivo
    private boolean isIudexGundyrAlive() {
        if (gameScreen == null) {
            return false; // Si no hay referencia al GameScreen, asumimos que el boss está muerto
        }
        return gameScreen.isBossAlive();
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
            if (isIudexGundyrAlive()) {
                if (!talking) {
                    talking = true;
                    currentBossAliveMessageIndex = (currentBossAliveMessageIndex + 1) % bossAliveMessages.length;
                } else {
                    talking = false;
                }
                return;
            }

            // Diálogo normal (cuando el boss está muerto)
            if (!talking) {
                talking = true;
                currentLine = 0;
            } else {
                currentLine++;

                // Si se terminó el diálogo → solicitar victoria al GameScreen
                if (currentLine >= dialog.length) {
                    talking = false;
                    currentLine = 0;

                    if (gameScreen != null) {
                        gameScreen.requestVictory();
                        Gdx.app.log("FireKeeper", "Solicitud de victoria enviada al GameScreen.");
                    } else if (game != null && game.gsm != null) {
                        game.gsm.setActiveScreenNormal(new com.TfPooAs.Souls2D.screens.VictoryScreen(game));
                    }


                    return;
                }

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
            if (isIudexGundyrAlive()) {
                font.draw(batch, bossAliveMessages[currentBossAliveMessageIndex],
                    position.x - 40, position.y + currentFrame.getRegionHeight() + 40);
            } else if (currentLine < dialog.length) {
                font.draw(batch, dialog[currentLine], position.x - 40, position.y + currentFrame.getRegionHeight() + 40);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (spriteSheet != null) spriteSheet.dispose();
        if (font != null) font.dispose();
    }

    public String[] getDialog() { return dialog; }
}
