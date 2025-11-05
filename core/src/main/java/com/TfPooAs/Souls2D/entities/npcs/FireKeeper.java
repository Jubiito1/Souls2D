package com.TfPooAs.Souls2D.entities.npcs;

import com.TfPooAs.Souls2D.entities.NPC;

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
