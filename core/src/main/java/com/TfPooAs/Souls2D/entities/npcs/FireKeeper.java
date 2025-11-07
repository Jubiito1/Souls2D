
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
import com.TfPooAs.Souls2D.screens.GameScreen;
import com.TfPooAs.Souls2D.systems.SaveSystem;


/**
 * FireKeeper animada (4 frames en una fila).
 * Cada frame dura 0.25s, bucle infinito.
 */
public class FireKeeper extends NPC implements Disposable {

    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private Texture spriteSheet;
    private final com.TfPooAs.Souls2D.core.Main game;
    private final GameScreen gameScreen; // Nueva referencia al GameScreen

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
        this.gameScreen = null; // Se establecerá después
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
    // Mostrar ranking al interactuar
    private boolean showLeaderboard = false;

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

        // Accede al boss desde GameScreen y verifica si está vivo
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
            // Verificar si el IudexGundyr está vivo
            if (isIudexGundyrAlive()) {
                // Si el boss está vivo, mostrar mensajes alternativos
                if (!talking) {
                    talking = true;
                    // Rotar entre los mensajes del boss vivo
                    currentBossAliveMessageIndex = (currentBossAliveMessageIndex + 1) % bossAliveMessages.length;
                } else {
                    talking = false; // Terminar el diálogo del boss vivo
                }
                return; // No continuar con el diálogo normal
            }

            // Diálogo normal (cuando el boss está muerto)
            if (!talking) {
                talking = true;
                currentLine = 0;
            } else {
                currentLine++;

                // Si se terminó el diálogo → pantalla de victoria
                if (currentLine >= dialog.length) {
                    talking = false;
                    currentLine = 0;
                    // Guardar ranking al pasar el juego con las almas obtenidas
                    int souls = player != null ? player.getSouls() : 0;
                    game.showVictoryScreen(souls);
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
            if (isIudexGundyrAlive()) {
                font.draw(batch, "Presiona E para hablar", position.x - 20, position.y + currentFrame.getRegionHeight() + 20);
            } else {
                font.draw(batch, "Presiona E para hablar", position.x - 20, position.y + currentFrame.getRegionHeight() + 20);
            }
        }

        if (talking) {
            if (isIudexGundyrAlive()) {
                // Mostrar mensaje cuando el boss está vivo
                font.draw(batch, bossAliveMessages[currentBossAliveMessageIndex],
                    position.x - 40, position.y + currentFrame.getRegionHeight() + 40);
            } else if (currentLine < dialog.length) {
                // Mostrar diálogo normal cuando el boss está muerto
                font.draw(batch, dialog[currentLine], position.x - 40, position.y + currentFrame.getRegionHeight() + 40);
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (spriteSheet != null) spriteSheet.dispose();
    }

    public String[] getDialog() { return dialog; }
}
