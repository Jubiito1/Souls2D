package com.TfPooAs.Souls2D.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.*;
import java.io.*;

/**
 * RankingSystem - guarda y carga las mejores almas conseguidas.
 * Guarda los datos en un archivo local "ranking.txt".
 */
public class RankingSystem {
    private static final String FILE_PATH = "ranking.txt";
    private static final int MAX_ENTRIES = 10;

    public static class Entry {
        public String name;
        public int souls;

        public Entry(String name, int souls) {
            this.name = name;
            this.souls = souls;
        }

        @Override
        public String toString() {
            return name + ":" + souls;
        }

        public static Entry fromString(String line) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                try {
                    return new Entry(parts[0], Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
    }

    /** Guarda el puntaje del jugador en ranking.txt */
    public static void saveScore(String name, int souls) {
        List<Entry> ranking = loadRanking();
        ranking.add(new Entry(name, souls));

        // Ordenar de mayor a menor
        ranking.sort((a, b) -> Integer.compare(b.souls, a.souls));

        // Limitar al top 10
        if (ranking.size() > MAX_ENTRIES) {
            ranking = ranking.subList(0, MAX_ENTRIES);
        }

        // Escribir al archivo
        FileHandle file = Gdx.files.local(FILE_PATH);
        StringBuilder data = new StringBuilder();
        for (Entry e : ranking) {
            data.append(e.toString()).append("\n");
        }
        file.writeString(data.toString(), false);
    }

    /** Carga el ranking desde el archivo */
    public static List<Entry> loadRanking() {
        List<Entry> ranking = new ArrayList<>();
        FileHandle file = Gdx.files.local(FILE_PATH);

        if (!file.exists()) {
            return ranking;
        }

        try {
            String[] lines = file.readString().split("\n");
            for (String line : lines) {
                Entry e = Entry.fromString(line.trim());
                if (e != null) ranking.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ranking;
    }
}
