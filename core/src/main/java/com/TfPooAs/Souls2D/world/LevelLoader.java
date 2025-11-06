package com.TfPooAs.Souls2D.world;

import com.TfPooAs.Souls2D.entities.items.Bonfire;
import com.TfPooAs.Souls2D.utils.Constants;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.List;

public class LevelLoader {

    private TiledMap map;
    private World world;
    private final List<Bonfire> bonfires = new ArrayList<>();

    public LevelLoader(World world, String mapPath) {
        this.world = world;
        this.map = new TmxMapLoader().load(mapPath);
        parseCollisions();
        parseInteractables(); // 🔥 lee hogueras y otros objetos
    }

    private void parseCollisions() {
        MapLayer collisionLayer = map.getLayers().get("Collisions");
        if (collisionLayer == null) return;

        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
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
                fdef.filter.maskBits = Constants.BIT_PLAYER | Constants.BIT_ENEMY;

                body.createFixture(fdef).setUserData("ground");
                shape.dispose();
            }

            if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                float[] vertices = polygon.getTransformedVertices();

                for (int i = 0; i < vertices.length; i++) vertices[i] /= Constants.PPM;

                float minX = vertices[0], minY = vertices[1], maxX = vertices[0], maxY = vertices[1];
                for (int i = 0; i < vertices.length; i += 2) {
                    if (vertices[i] < minX) minX = vertices[i];
                    if (vertices[i] > maxX) maxX = vertices[i];
                    if (vertices[i + 1] < minY) minY = vertices[i + 1];
                    if (vertices[i + 1] > maxY) maxY = vertices[i + 1];
                }
                float centerX = (minX + maxX) / 2f;
                float centerY = (minY + maxY) / 2f;

                for (int i = 0; i < vertices.length; i += 2) {
                    vertices[i] -= centerX;
                    vertices[i + 1] -= centerY;
                }

                BodyDef bdef = new BodyDef();
                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set(centerX, centerY);
                Body body = world.createBody(bdef);

                PolygonShape shape = new PolygonShape();
                shape.set(vertices);
                FixtureDef fdef = new FixtureDef();
                fdef.shape = shape;
                fdef.friction = 0.8f;
                fdef.restitution = 0f;
                fdef.filter.categoryBits = Constants.BIT_GROUND;
                fdef.filter.maskBits = Constants.BIT_PLAYER | Constants.BIT_ENEMY;
                body.createFixture(fdef).setUserData("ground");
                shape.dispose();
            }
        }
    }

    private void parseInteractables() {
        MapLayer layer = map.getLayers().get("Interactables");
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject && "bonfire".equalsIgnoreCase(object.getName())) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                float x = rect.x + rect.width / 2f;
                float y = rect.y + rect.height / 2f;
                bonfires.add(new Bonfire(x, y));
            }
        }
    }

    public List<Bonfire> getBonfires() {
        return bonfires;
    }

    public TiledMap getMap() {
        return map;
    }

    public void dispose() {
        map.dispose();
        for (Bonfire b : bonfires) b.dispose();
    }
}
