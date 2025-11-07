
package com.TfPooAs.Souls2D.ui;

import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.entities.Enemy;
import com.TfPooAs.Souls2D.systems.SoundManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class HUD {
    private final Player player;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    // Icono Estus
    private Texture estusIcon;

    // Layout constants (en píxeles de pantalla para 1920x1080 virtual)
    private static final float MARGEN = 30f;

    // Tamaños de la barra de vida (ligeramente más chica que antes)
    private static final float ANCHO_BARRA_VIDA = 540f; // antes 600
    private static final float ALTO_BARRA_VIDA = 20f;   // antes 24

    // Tamaños de la barra de stamina
    private static final float ALTO_BARRA_STAMINA = 14f; // antes 16
    private static final float ESPACIO_ENTRE_BARRAS = 10f;

    // Icono de Estus
    private static final float TAM_ESTUS = 64f; // cuadrado
    private static final float SEPARACION_ESTUS_TEXTO = 8f; // espacio entre icono y número

    // Boss bar configs
    private static final float ANCHO_BARRA_BOSS = 900f;
    private static final float ALTO_BARRA_BOSS = 28f;
    private static final float MARGEN_INFERIOR_BOSS = 40f;

    // NUEVO: Control de música del boss
    private boolean bossMusicStarted = false;

    public HUD(Player player) {
        this.player = player;
        this.shapes = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.setUseIntegerPositions(false);
        this.font.setColor(Color.WHITE);
        // Cargar icono de Estus
        try {
            this.estusIcon = new Texture(Gdx.files.internal("estus.png"));
        } catch (Exception e) {
            this.estusIcon = null;
        }
    }

    public void render(OrthographicCamera uiCamera, SpriteBatch uiBatch) {
        if (player == null) return;

        // Proyección para shapes
        shapes.setProjectionMatrix(uiCamera.combined);

        // Coordenadas para la barra de vida superior izquierda
        float hpX = MARGEN;
        float hpY = uiCamera.viewportHeight - MARGEN - ALTO_BARRA_VIDA; // top-left

        // Fondo (oscuro)
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0f, 0f, 0f, 0.6f));
        shapes.rect(hpX - 4, hpY - 4, ANCHO_BARRA_VIDA + 8, ALTO_BARRA_VIDA + 8);

        // Borde
        shapes.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
        shapes.rect(hpX - 2, hpY - 2, ANCHO_BARRA_VIDA + 4, ALTO_BARRA_VIDA + 4);

        // Fondo interior
        shapes.setColor(new Color(0.07f, 0.07f, 0.07f, 1f));
        shapes.rect(hpX, hpY, ANCHO_BARRA_VIDA, ALTO_BARRA_VIDA);

        // Relleno de vida
        float ratio = Math.max(0f, Math.min(1f, (float) player.getCurrentHealth() / (float) player.getMaxHealth()));
        float fillWidth = ANCHO_BARRA_VIDA * ratio;
        Color hpColor = new Color(0.75f, 0.15f, 0.10f, 1f); // rojo profundo

        // base
        shapes.setColor(hpColor);
        shapes.rect(hpX, hpY, fillWidth, ALTO_BARRA_VIDA);
        // franja superior de brillo
        shapes.rect(hpX, hpY + ALTO_BARRA_VIDA - 5f, fillWidth, 5f);

        // Barra de stamina debajo de la de vida
        float stamY = hpY - ESPACIO_ENTRE_BARRAS - ALTO_BARRA_STAMINA;
        // Fondo de stamina
        shapes.setColor(new Color(0.07f, 0.07f, 0.07f, 1f));
        shapes.rect(hpX, stamY, ANCHO_BARRA_VIDA, ALTO_BARRA_STAMINA);
        // Relleno de stamina
        float stamRatio = Math.max(0f, Math.min(1f, (float) player.getCurrentStamina() / (float) player.getMaxStamina()));
        float stamFill = ANCHO_BARRA_VIDA * stamRatio;
        Color stamColor = new Color(0.20f, 0.75f, 0.20f, 1f); // verde
        shapes.setColor(stamColor);
        shapes.rect(hpX, stamY, stamFill, ALTO_BARRA_STAMINA);

        // === Icono de Estus (abajo-izquierda) ===
        int cargas = player.getEstusCharges();
        float estusX = MARGEN;
        float estusY = MARGEN + 20f; // ligeramente por encima del borde inferior

        // Fondo del icono (panel)
        shapes.setColor(new Color(0f, 0f, 0f, 0.6f));
        shapes.rect(estusX - 6, estusY - 6, TAM_ESTUS + 12, TAM_ESTUS + 12);
        // Borde del panel
        shapes.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
        shapes.rect(estusX - 4, estusY - 4, TAM_ESTUS + 8, TAM_ESTUS + 8);

        // Cuadrado del Estus o icono
        if (estusIcon == null) {
            if (cargas > 0) {
                // Ámbar (tipo estus encendido)
                shapes.setColor(new Color(1.0f, 0.62f, 0.12f, 1f));
            } else {
                // Gris apagado
                shapes.setColor(new Color(0.35f, 0.35f, 0.35f, 1f));
            }
            shapes.rect(estusX, estusY, TAM_ESTUS, TAM_ESTUS);
        }

        // === Barra de vida del Boss (inferior, centrada) ===
        boolean bossVisible = boss != null && boss.isActive() && !boss.isDead() && isBossCloseEnough();

        // NUEVO: Gestionar música del boss
        if (bossVisible && !bossMusicStarted) {
            // Iniciar música del boss
            SoundManager.stopBackground(); // Parar música normal
            SoundManager.playBackground("bossmusic.wav", true); // Iniciar música del boss
            bossMusicStarted = true;
            Gdx.app.log("HUD", "Iniciando música del boss");
        } else if (!bossVisible && bossMusicStarted) {
            // El boss ya no está visible, volver a música normal
            SoundManager.stopBackground(); // Parar música del boss
            SoundManager.playBackground("musica.wav", true); // Volver a música normal
            bossMusicStarted = false;
            Gdx.app.log("HUD", "Volviendo a música normal");
        }

        if (bossVisible) {
            float barX = (uiCamera.viewportWidth - ANCHO_BARRA_BOSS) / 2f;
            float barY = MARGEN_INFERIOR_BOSS;
            // panel
            shapes.setColor(new Color(0f, 0f, 0f, 0.65f));
            shapes.rect(barX - 6, barY - 6, ANCHO_BARRA_BOSS + 12, ALTO_BARRA_BOSS + 12);
            // borde
            shapes.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
            shapes.rect(barX - 3, barY - 3, ANCHO_BARRA_BOSS + 6, ALTO_BARRA_BOSS + 6);
            // fondo interior
            shapes.setColor(new Color(0.07f, 0.07f, 0.07f, 1f));
            shapes.rect(barX, barY, ANCHO_BARRA_BOSS, ALTO_BARRA_BOSS);
            // relleno vida boss
            float bossRatio = Math.max(0f, Math.min(1f, (float) boss.getCurrentHealth() / (float) boss.getMaxHealth()));
            shapes.setColor(new Color(0.80f, 0.12f, 0.12f, 1f));
            shapes.rect(barX, barY, ANCHO_BARRA_BOSS * bossRatio, ALTO_BARRA_BOSS);
        }
        // cerrar shapes antes de batch
        shapes.end();

        // Dibujar icono/ textos con el batch de UI
        uiBatch.setProjectionMatrix(uiCamera.combined);
        // Aseguramos que el batch no herede tintes previos
        uiBatch.setColor(Color.WHITE);
        uiBatch.begin();

        // Icono de Estus si existe
        if (estusIcon != null) {
            if (cargas > 0) {
                uiBatch.setColor(Color.WHITE);
            } else {
                // tintar gris para indicar sin cargas
                uiBatch.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
            }
            uiBatch.draw(estusIcon, estusX, estusY, TAM_ESTUS, TAM_ESTUS);
            // resetear color tras dibujar el icono
            uiBatch.setColor(Color.WHITE);
        }

        // Número de Estus centrado debajo del icono
        String numeroEstus = String.valueOf(cargas);
        layout.setText(font, numeroEstus);
        float numeroX = estusX + (TAM_ESTUS - layout.width) / 2f;
        float numeroY = estusY - SEPARACION_ESTUS_TEXTO; // debajo del icono
        font.setColor(Color.WHITE);
        font.draw(uiBatch, numeroEstus, numeroX, numeroY);

        // Texto del Boss centrado sobre la barra (si visible)
        if (bossVisible) {
            String bossName = "Iudex Gundyr";
            layout.setText(font, bossName);
            float textX = (uiCamera.viewportWidth - layout.width) / 2f;
            float textY = MARGEN_INFERIOR_BOSS + ALTO_BARRA_BOSS + 22f;
            font.setColor(Color.WHITE);
            font.draw(uiBatch, bossName, textX, textY);
        }

        uiBatch.end();
        // Reset de color para no afectar a otros renders
        uiBatch.setColor(Color.WHITE);
    }

    // === Boss binding ===
    private Enemy boss;
    public void setBoss(Enemy boss) { this.boss = boss; }
    private boolean isBossCloseEnough() {
        if (boss == null || player == null) return false;
        // distancia euclidiana en coordenadas de mundo (pixeles visuales)
        float dx = boss.getPosition().x - player.getPosition().x;
        float dy = boss.getPosition().y - player.getPosition().y;
        float dist2 = dx*dx + dy*dy;
        float max = 800f; // umbral configurable de proximidad visual
        return dist2 <= max*max;
    }

    // NUEVO: Método para resetear el estado de música del boss (útil cuando se reinicia el juego)
    public void resetBossMusic() {
        bossMusicStarted = false;
    }

    public void dispose() {
        shapes.dispose();
        font.dispose();
        if (estusIcon != null) {
            estusIcon.dispose();
        }
    }
}
