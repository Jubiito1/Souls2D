package com.TfPooAs.Souls2D.entities.items;

import com.TfPooAs.Souls2D.entities.Item;
import com.TfPooAs.Souls2D.entities.Player;
import com.TfPooAs.Souls2D.systems.SaveSystem;

/**
 * Minimal Bonfire item that can be placed from Tiled. Uses a default texture to avoid new assets.
 */
public class Bonfire extends Item {

    public Bonfire(float x, float y) {
        // Reuse existing player.png as placeholder sprite to avoid adding assets
        super(x, y, "player.png");
        this.interactionRange = 64f;
    }

    public Bonfire(float x, float y, String texturePath) {
        super(x, y, texturePath);
        this.interactionRange = 64f;
    }

    @Override
    public void update(float delta) {
        // Entity.update requirement: no-op; GameScreen should call Item.update(delta, player)
    }

    @Override
    protected void onInteract(Player player) {
        // Save the player's current position in pixels as the last checkpoint
        if (player != null) {
            SaveSystem.saveLastBonfire(player.getPosition().x, player.getPosition().y);
        }
        // mark interacted to avoid repeat in same frame
        this.interacted = true;
    }
}
