package com.TfPooAs.Souls2D.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple audio manager:
 * - Background music controls (safe if file missing).
 * - One-shot and looping SFX with simple caching.
 */
public class SoundManager {
    private static Music currentMusic;
    private static float musicVolume = 0.5f; // default 50%
    private static float sfxVolume = 1.0f;   // default 100%
    private static String currentPath;

    // SFX cache and loop tracking
    private static final Map<String, Sound> sfxCache = new HashMap<>();
    private static final Map<String, Long> loopingSfxIds = new HashMap<>();

    private SoundManager() {}

    /**
     * Plays a background music by path. If the same path is already playing, it just ensures looping/volume.
     * Path is resolved with Gdx.files.internal(). Use the same convention as the rest of the project
     * (e.g., "assets/musica.wav").
     */
    public static void playBackground(String internalPath, boolean loop) {
        try {
            if (currentMusic != null && internalPath != null && internalPath.equals(currentPath)) {
                // Already loaded the same track: ensure settings and play if not playing
                currentMusic.setLooping(loop);
                currentMusic.setVolume(musicVolume);
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
            currentMusic.setVolume(musicVolume);
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

    public static void setMusicVolume(float v) {
        musicVolume = Math.max(0f, Math.min(1f, v));
        if (currentMusic != null) currentMusic.setVolume(musicVolume);
    }

    public static float getMusicVolume() { return musicVolume; }

    public static void setSfxVolume(float v) { sfxVolume = Math.max(0f, Math.min(1f, v)); }

    public static float getSfxVolume() { return sfxVolume; }

    public static String getCurrentPath() { return currentPath; }

    // ===== SFX =====
    private static Sound getOrLoadSound(String internalPath) {
        try {
            if (internalPath == null || internalPath.isEmpty()) return null;
            Sound s = sfxCache.get(internalPath);
            if (s != null) return s;
            FileHandle fh = Gdx.files.internal(internalPath);
            if (!fh.exists()) {
                Gdx.app.error("SoundManager", "SFX file not found: " + internalPath);
                return null;
            }
            s = Gdx.audio.newSound(fh);
            sfxCache.put(internalPath, s);
            return s;
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to load SFX: " + internalPath + ": " + e.getMessage());
            return null;
        }
    }

    /** Play a one-shot sound effect. */
    public static void playSfx(String internalPath) {
        try {
            Sound s = getOrLoadSound(internalPath);
            if (s != null) s.play(sfxVolume);
        } catch (Exception ignored) { }
    }

    /** Ensure a looping SFX is playing under the given key (uses the path as key). */
    public static void ensureLooping(String internalPath) {
        try {
            if (loopingSfxIds.containsKey(internalPath)) return; // already looping
            Sound s = getOrLoadSound(internalPath);
            if (s == null) return;
            long id = s.loop(sfxVolume);
            loopingSfxIds.put(internalPath, id);
        } catch (Exception ignored) { }
    }

    /** Stop a previously started loop for this path. */
    public static void stopLoop(String internalPath) {
        try {
            Long id = loopingSfxIds.remove(internalPath);
            Sound s = sfxCache.get(internalPath);
            if (s != null && id != null) s.stop(id);
        } catch (Exception ignored) { }
    }

    /** Stop and clear all looping SFX. */
    public static void stopAllLoops() {
        for (Map.Entry<String, Long> e : loopingSfxIds.entrySet()) {
            Sound s = sfxCache.get(e.getKey());
            if (s != null) {
                try { s.stop(e.getValue()); } catch (Exception ignored) {}
            }
        }
        loopingSfxIds.clear();
    }

    /** Dispose all cached SFX (call on shutdown). */
    public static void disposeSfx() {
        stopAllLoops();
        for (Sound s : sfxCache.values()) {
            try { s.dispose(); } catch (Exception ignored) {}
        }
        sfxCache.clear();
    }
}
