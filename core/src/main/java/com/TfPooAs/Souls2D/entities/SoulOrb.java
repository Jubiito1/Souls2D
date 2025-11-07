package com.TfPooAs.Souls2D.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.TfPooAs.Souls2D.entities.Player;

public class SoulOrb {
    private Texture texture;
    private Vector2 position;
    private boolean collected = false;
    private float speed = 150f;

    public SoulOrb(float x, float y) {
        this.position = new Vector2(x, y);
        this.texture = new Texture("soul_orb.png");
    }

    public void update(float delta, Player player) {
        if (collected) return;

        float dist = player.getPosition().dst(position);
        if (dist < 100) {
            // se mueve hacia el jugador
            Vector2 dir = new Vector2(player.getPosition()).sub(position).nor();
            position.add(dir.scl(speed * delta));
        }

        // si está muy cerca, se "recoge"
        if (dist < 30) {
            collected = true;
            player.addSouls(50); // suma 50 almas (podés cambiar el valor)
        }
    }

    public void render(SpriteBatch batch) {
        if (!collected)
            batch.draw(texture, position.x, position.y, 32, 32);
    }

    public boolean isCollected() { return collected; }

    public void dispose() { texture.dispose(); }
}
