package com.TfPooAs.Souls2D.entities.npcs;

import com.TfPooAs.Souls2D.entities.NPC;

/**
 * Minimal FireKeeper NPC to be placed via Tiled map. Uses default placeholder texture.
 */
public class FireKeeper extends NPC {

    public FireKeeper(float x, float y) {
        super(x, y); // uses default texture from NPC convenience constructor
        this.setInteractionRadius(80f);
    }

    public FireKeeper(float x, float y, String texturePath) {
        super(x, y, texturePath);
        this.setInteractionRadius(80f);
    }

    @Override
    public void update(float delta) {
        // No active behavior for now
public class FireKeeper extends NPC {
    private final String[] dialog = new String[]{
        "Ashen One, welcome to the bonfire.",
        "Speak thine heart’s desire.",
        "Very well. Then touch the darkness within me..."
    };

    public FireKeeper(float x, float y) {
        super(x, y, "player.png"); // Placeholder texture. Replace with FireKeeper asset when available
        this.interactionRadius = 100f;
        this.width = this.width * 0.8f; // opcional: ajustar tamaño
        this.height = this.height * 0.8f;
    }

    public String[] getDialog() {
        return dialog;
    }
}
