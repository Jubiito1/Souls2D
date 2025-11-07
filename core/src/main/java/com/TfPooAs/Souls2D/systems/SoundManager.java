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
 * Path convention: pass internal paths relative to the assets root (e.g., "musica.wav" or "ui/menu_bg.png").
 * For backward-compatibility, a leading "assets/" prefix is also accepted and will be stripped automatically.
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

    private static String normalize(String internalPath) {
        if (internalPath == null) return null;
        String p = internalPath.trim();
        if (p.startsWith("assets/")) p = p.substring("assets/".length());
        if (p.startsWith("/")) p = p.substring(1);
        return p;
    }

    /**
     * Plays a background music by path. If the same path is already playing, it just ensures looping/volume.
     * Path is resolved with Gdx.files.internal(). Use the same convention as the rest of the project
     * (e.g., "musica.wav").
     */
    public static void playBackground(String internalPath, boolean loop) {
        try {
            String p = normalize(internalPath);
            if (currentMusic != null && p != null && p.equals(currentPath)) {
                // Already loaded the same track: ensure settings and play if not playing
                currentMusic.setLooping(loop);
                currentMusic.setVolume(musicVolume);
                if (!currentMusic.isPlaying()) currentMusic.play();
                return;
            }

            // Different track or nothing loaded: stop and dispose previous
            stopBackground();
            disposeBackground();

            currentPath = p;
            if (p == null || p.isEmpty()) {
                Gdx.app.log("SoundManager", "No background path provided.");
                return;
            }

            FileHandle fh = Gdx.files.internal(p);
            if (!fh.exists()) {
                Gdx.app.error("SoundManager", "Audio file not found: " + p);
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
            String p = normalize(internalPath);
            if (p == null || p.isEmpty()) return null;
            Sound s = sfxCache.get(p);
            if (s != null) return s;
            FileHandle fh = Gdx.files.internal(p);
            if (!fh.exists()) {
                Gdx.app.error("SoundManager", "SFX file not found: " + p);
                return null;
            }
            s = Gdx.audio.newSound(fh);
            sfxCache.put(p, s);
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
            String p = normalize(internalPath);
            if (p == null || p.isEmpty()) return;
            if (loopingSfxIds.containsKey(p)) return; // already looping
            Sound s = getOrLoadSound(p);
            if (s == null) return;
            long id = s.loop(sfxVolume);
            loopingSfxIds.put(p, id);
        } catch (Exception ignored) { }
    }

    /** Stop a previously started loop for this path. */
    public static void stopLoop(String internalPath) {
        try {
            String p = normalize(internalPath);
            Long id = loopingSfxIds.remove(p);
            Sound s = sfxCache.get(p);
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
