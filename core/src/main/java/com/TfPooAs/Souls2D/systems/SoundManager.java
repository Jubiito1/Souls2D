package com.TfPooAs.Souls2D.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

/**
 * Simple background music manager.
 * - Safe to call even if the audio file is missing (will log and do nothing).
 * - Centralizes play/pause/resume/stop and volume.
 */
public class SoundManager {
    private static Music currentMusic;
    private static float volume = 0.5f; // default 50%
    private static String currentPath;

    private SoundManager() {}

    /**
     * Plays a background music by path. If the same path is already playing, it just ensures looping/volume.
     * Path is resolved with Gdx.files.internal(). Use the same convention as the rest of the project
     * (e.g., "assets/audio/bgm.ogg").
     */
    public static void playBackground(String internalPath, boolean loop) {
        try {
            if (currentMusic != null && internalPath != null && internalPath.equals(currentPath)) {
                // Already loaded the same track: ensure settings and play if not playing
                currentMusic.setLooping(loop);
                currentMusic.setVolume(volume);
                if (!currentMusic.isPlaying()) currentMusic.play();
                return;
            }

            // Different track or nothing loaded: stop and dispose previous
            stopBackground();
            disposeBackground();

            currentPath = internalPath;
            if (internalPath == null || internalPath.isEmpty()) {
                Gdx.app.log("SoundManager", "No background path provided.");
                return;
            }

            FileHandle fh = Gdx.files.internal(internalPath);
            if (!fh.exists()) {
                Gdx.app.error("SoundManager", "Audio file not found: " + internalPath);
                currentPath = null;
                return;
            }

            currentMusic = Gdx.audio.newMusic(fh);
            currentMusic.setLooping(loop);
            currentMusic.setVolume(volume);
            currentMusic.play();
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to play background: " + e.getMessage());
        }
    }

    public static void pauseBackground() {
        try {
            if (currentMusic != null && currentMusic.isPlaying()) currentMusic.pause();
        } catch (Exception ignored) { }
    }

    public static void resumeBackground() {
        try {
            if (currentMusic != null && !currentMusic.isPlaying()) currentMusic.play();
        } catch (Exception ignored) { }
    }

    public static void stopBackground() {
        try {
            if (currentMusic != null) {
                currentMusic.stop();
            }
        } catch (Exception ignored) { }
    }

    public static void disposeBackground() {
        try {
            if (currentMusic != null) {
                currentMusic.dispose();
                currentMusic = null;
                currentPath = null;
            }
        } catch (Exception ignored) { }
    }

    public static void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        if (currentMusic != null) currentMusic.setVolume(volume);
    }

    public static float getVolume() {
        return volume;
    }

    public static String getCurrentPath() {
        return currentPath;
    }
}
