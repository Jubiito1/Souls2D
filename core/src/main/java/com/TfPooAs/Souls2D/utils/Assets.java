package com.TfPooAs.Souls2D.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class Assets {

    public static BitmapFont generateGaramond(int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/Garamond.otf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size; // tamaño de la fuente, ej: 24
        parameter.borderWidth = 0;
        parameter.color = com.badlogic.gdx.graphics.Color.WHITE; // color del texto
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }
}
