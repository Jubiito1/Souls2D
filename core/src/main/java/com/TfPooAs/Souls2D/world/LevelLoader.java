package com.TfPooAs.Souls2D.world;

import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

public class LevelLoader {

    private TiledMap map;
    private World world;

    public LevelLoader(World world, String mapPath) {
        this.world = world;
        this.map = new TmxMapLoader().load(mapPath);
        parseCollisions();
    }

    private void parseCollisions() {
        MapLayer collisionLayer = map.getLayers().get("Collisions");
        if (collisionLayer == null) return;

        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                // Cuerpo estático
                BodyDef bdef = new BodyDef();
                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set(
                    (rect.x + rect.width / 2) / Constants.PPM,
                    (rect.y + rect.height / 2) / Constants.PPM
                );

                Body body = world.createBody(bdef);

                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width / 2 / Constants.PPM, rect.height / 2 / Constants.PPM);

                FixtureDef fdef = new FixtureDef();
                fdef.shape = shape;
                fdef.friction = 0.8f;
                fdef.restitution = 0f;
                fdef.filter.categoryBits = Constants.BIT_GROUND;

                body.createFixture(fdef);
                shape.dispose();
            }
        }
    }

    public TiledMap getMap() {
        return map;
    }

    public void dispose() {
        map.dispose();
    }
}
