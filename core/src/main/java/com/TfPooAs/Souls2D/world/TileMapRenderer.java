package com.TfPooAs.Souls2D.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class TileMapRenderer {

    private OrthogonalTiledMapRenderer renderer;
    private TiledMap map;

    public TileMapRenderer(TiledMap map) {
        this.map = map;
        // unitScale = 1 / PPM
        this.renderer = new OrthogonalTiledMapRenderer(map, 1 / 32f);
    }

    public void render(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render();
    }

    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}
