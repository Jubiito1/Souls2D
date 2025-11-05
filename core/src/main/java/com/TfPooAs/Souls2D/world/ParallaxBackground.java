package com.TfPooAs.Souls2D.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class ParallaxBackground {
    private Texture[] layers;
    private float[] speedFactors; // cuán rápido se mueve cada capa
    private float[] positions;
    private OrthographicCamera camera;

    public ParallaxBackground(Texture[] layers, float[] speedFactors, OrthographicCamera camera) {
        this.layers = layers;
        this.speedFactors = speedFactors;
        this.camera = camera;
        this.positions = new float[layers.length];
    }

    public void update(float delta) {
        for (int i = 0; i < layers.length; i++) {
            positions[i] += speedFactors[i] * delta;
            positions[i] %= layers[i].getWidth(); // bucle infinito del fondo
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < layers.length; i++) {
            float x = camera.position.x - camera.viewportWidth / 2;
            float y = camera.position.y - camera.viewportHeight / 2;

            // Dibuja dos veces la textura para que parezca infinita
            batch.draw(layers[i], x - positions[i], y);
            batch.draw(layers[i], x - positions[i] + layers[i].getWidth(), y);
        }
    }
}
