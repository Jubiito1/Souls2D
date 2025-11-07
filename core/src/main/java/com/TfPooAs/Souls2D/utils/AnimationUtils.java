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

    /**
     * Tries to create an animation from a horizontal spritesheet where rows==1 and columns are unknown.
     * It tests a list of candidate columns and picks the first that evenly divides the sheet width.
     * Returns null if file doesn't exist. If no candidate fits, returns a single-frame animation.
     */
    public static AnimWithTexture createFromHorizontalSheetAutoCols(String internalPath, int[] candidateCols, float frameDuration, Animation.PlayMode playMode) {
        FileHandle fh = Gdx.files.internal(internalPath);
        if (fh == null || !fh.exists()) return null;
        Texture sheet = new Texture(fh);
        int sheetW = sheet.getWidth();
        int colsFound = -1;
        for (int cols : candidateCols) {
            if (cols > 0 && sheetW % cols == 0) {
                colsFound = cols;
                break;
            }
        }
        Animation<TextureRegion> anim;
        if (colsFound > 0) {
            TextureRegion[][] tmp = TextureRegion.split(sheet, sheetW / colsFound, sheet.getHeight());
            TextureRegion[] frames = new TextureRegion[colsFound];
            for (int c = 0; c < colsFound; c++) {
                frames[c] = tmp[0][c];
            }
            anim = new Animation<>(frameDuration, frames);
        } else {
            // fall back to single frame
            anim = new Animation<>(frameDuration, new TextureRegion(sheet));
        }
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
