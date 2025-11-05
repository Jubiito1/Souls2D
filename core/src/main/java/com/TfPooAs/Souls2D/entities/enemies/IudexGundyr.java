package com.TfPooAs.Souls2D.entities.enemies;

import com.TfPooAs.Souls2D.entities.Enemy;
import com.TfPooAs.Souls2D.entities.Player;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Boss Iudex Gundyr (versión mínima):
 * - Subclase del Enemy base para poder instanciarlo desde el mapa al final del nivel.
 * - Hereda todo el comportamiento del Enemy estándar para no introducir cambios abruptos.
 * - Forzamos su sprite a usar player.png para asegurar que siempre sea visible.
 */
public class IudexGundyr extends Enemy {
    public IudexGundyr(World world, float x, float y, Player player) {
        super(world, x, y, player);
        // Reemplazar la textura por la del jugador (player.png)
        if (this.texture != null) {
            this.texture.dispose(); // evitar fuga de memoria de enemy.png
        }
        this.texture = new Texture("player.png");
        // Actualizar dimensiones visuales al nuevo sprite
        this.width = texture.getWidth();
        this.height = texture.getHeight();
        // Nota: El collider ya fue creado en Enemy con el tamaño anterior.
        // Para cambios no abruptos, mantenemos el collider actual.
    }
}
