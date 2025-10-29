package com.TfPooAs.Souls2D.core;

import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.entities.enemies.Proyectile;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Gestor mínimo de proyectiles para enemigos a distancia.
 * Mantiene, actualiza y dibuja una lista de proyectiles simples.
 */
public class ProjectileManager {

    private final Player player;
    private final List<Proyectile> projectiles = new ArrayList<>();

    public ProjectileManager(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void spawnProjectile(Proyectile p) {
        if (p != null) projectiles.add(p);
    }

    public void removeProjectile(Proyectile p) {
        projectiles.remove(p);
    }

    /**
     * Método simplificado: en este repositorio no hay una malla de colisión del mundo disponible aquí.
     * Por ahora devolvemos false para no eliminar proyectiles por colisión con el mundo.
     * Si deseas usarlo, reemplaza por una consulta a tu gestor de colisiones/tilemap.
     */
    public boolean isSolidAt(float x, float y) {
        return false;
    }

    public void update(float delta) {
        for (Iterator<Proyectile> it = projectiles.iterator(); it.hasNext(); ) {
            Proyectile p = it.next();
            p.update(delta);
            if (p.isDead()) {
                it.remove();
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Proyectile p : projectiles) {
            p.render(batch);
        }
    }

    public List<Proyectile> getProjectiles() { return projectiles; }
}
