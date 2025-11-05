package com.TfPooAs.Souls2D.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class Background {

    private Texture[] layers;
    private float[] speeds;
    private OrthographicCamera camera;

    public Background(Texture[] layers, float[] speeds, OrthographicCamera camera) {
        this.layers = layers;
        this.speeds = speeds;
        this.camera = camera;
    }

    public void update(float delta) {
        // En este ejemplo no se necesita lógica de actualización extra
        // ya que el desplazamiento depende directamente de la cámara
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < layers.length; i++) {
            float x = camera.position.x * speeds[i];
            batch.draw(layers[i],
                x - layers[i].getWidth() / 2f,
                camera.position.y - layers[i].getHeight() / 2f);
        }
    }
}
