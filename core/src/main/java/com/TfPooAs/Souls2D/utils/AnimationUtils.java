package com.TfPooAs.Souls2D.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationUtils {

    public static class AnimWithTexture {
        public final Animation<TextureRegion> animation;
        public final Texture texture; // hoja que debe ser liberada
        public AnimWithTexture(Animation<TextureRegion> animation, Texture texture) {
            this.animation = animation;
            this.texture = texture;
        }
    }

    public static AnimWithTexture createFromSpritesheetIfExists(String internalPath, int cols, int rows, float frameDuration, Animation.PlayMode playMode) {
        FileHandle fh = Gdx.files.internal(internalPath);
        if (fh == null || !fh.exists()) return null;
        Texture sheet = new Texture(fh);
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                frames[index++] = tmp[r][c];
            }
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(playMode);
        return new AnimWithTexture(anim, sheet);
    }

    public static Animation<TextureRegion> createSingleFrame(Texture texture, float frameDuration, Animation.PlayMode playMode) {
        TextureRegion region = new TextureRegion(texture);
        Animation<TextureRegion> anim = new Animation<>(frameDuration, region);
        anim.setPlayMode(playMode);
        return anim;
    }
}
