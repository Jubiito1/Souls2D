package com.TfPooAs.Souls2D.entities.npcs;

import com.TfPooAs.Souls2D.entities.NPC;

/**
 * NPC FireKeeper simple, con textura placeholder.
 */
public class FireKeeper extends NPC {

    private final String[] dialog = new String[]{
        "Ashen One, welcome to the bonfire.",
        "Speak thine heart’s desire.",
        "Very well. Then touch the darkness within me..."
    };

    public FireKeeper(float x, float y) {
        super(x, y); // utiliza textura por defecto de NPC
        this.setInteractionRadius(100f);
        // pequeño ajuste visual opcional
        this.width = this.width * 0.8f;
        this.height = this.height * 0.8f;
    }

    public FireKeeper(float x, float y, String texturePath) {
        super(x, y, texturePath);
        this.setInteractionRadius(100f);
    }

    public String[] getDialog() { return dialog; }
}
