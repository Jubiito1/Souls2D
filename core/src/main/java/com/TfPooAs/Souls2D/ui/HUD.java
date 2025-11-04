package com.TfPooAs.Souls2D.ui;

import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

    // Layout constants (en píxeles de pantalla para 1920x1080 virtual)
    private static final float MARGEN = 30f;

    // Tamaños de la barra de vida
    private static final float ANCHO_BARRA_VIDA = 600f;
    private static final float ALTO_BARRA_VIDA = 24f;

    // Tamaños de la barra de stamina
    private static final float ALTO_BARRA_STAMINA = 16f;
    private static final float ESPACIO_ENTRE_BARRAS = 10f;

    // Icono de Estus
    private static final float TAM_ESTUS = 64f; // cuadrado
    private static final float SEPARACION_ESTUS_TEXTO = 8f; // espacio entre icono y número

    public HUD(Player player) {
        this.player = player;
        this.shapes = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.setUseIntegerPositions(false);
        this.font.setColor(Color.WHITE);
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

        // Cuadrado del Estus (cambia si no hay cargas)
        if (cargas > 0) {
            // Ámbar (tipo estus encendido)
            shapes.setColor(new Color(1.0f, 0.62f, 0.12f, 1f));
        } else {
            // Gris apagado
            shapes.setColor(new Color(0.35f, 0.35f, 0.35f, 1f));
        }
        shapes.rect(estusX, estusY, TAM_ESTUS, TAM_ESTUS);
        shapes.end();

        // Dibujar textos con el batch de UI
        uiBatch.setProjectionMatrix(uiCamera.combined);
        uiBatch.begin();
        // Texto de vida (opcional)
        font.setColor(0.9f, 0.9f, 0.9f, 0.85f);
        font.draw(uiBatch, "Vida: " + player.getCurrentHealth() + "/" + player.getMaxHealth(), hpX + 10f, hpY + ALTO_BARRA_VIDA - 6f);

        // Número de Estus centrado debajo del icono
        String numeroEstus = String.valueOf(cargas);
        layout.setText(font, numeroEstus);
        float numeroX = estusX + (TAM_ESTUS - layout.width) / 2f;
        float numeroY = estusY - SEPARACION_ESTUS_TEXTO; // debajo del icono
        font.setColor(Color.WHITE);
        font.draw(uiBatch, numeroEstus, numeroX, numeroY);
        uiBatch.end();
    }

    public void dispose() {
        shapes.dispose();
        font.dispose();
    }
}
