package com.TfPooAs.Souls2D.entities.npcs;

import com.TfPooAs.Souls2D.entities.NPC;

/**
 * NPC FireKeeper simple, con textura placeholder.
 */
public class FireKeeper extends NPC {

    private final String[] dialog = new String[]{
        "Haz permitido el acceso al santuario del enlace de fuego",
        "La llama primigenia estará eternamente agradecida",
        "Puedes volver a ser parte de ella..."
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
