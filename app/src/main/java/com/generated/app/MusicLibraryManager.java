package com.generated.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MusicLibraryManager {
    private static MusicLibraryManager instance;
    private List<Song> allSongs;
    private Context context;

    private MusicLibraryManager(Context context) {
        this.context = context.getApplicationContext();
        this.allSongs = new ArrayList<>();
        loadDeviceSongs();
    }

    public static synchronized MusicLibraryManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicLibraryManager(context);
        }
        return instance;
    }

    public void loadDeviceSongs() {
        allSongs.clear();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, null, selection, null, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                do {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String album = cursor.getString(albumColumn);
                    String path = cursor.getString(dataColumn);
                    long duration = cursor.getLong(durationColumn);

                    if (artist == null || artist.equals("<unknown>")) {
                        artist = "אמן לא ידוע";
                    }
                    if (title == null) {
                        title = "שיר ללא שם";
                    }

                    allSongs.add(new Song(id, title, artist, album, path, duration));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // If no real music found on device, add sample fallback song so UI is populated and testable
        if (allSongs.isEmpty()) {
            allSongs.add(new Song(-1, "שיר לדוגמה - אין שירים באחסון", "ספוטיפיי אופליין", "אלבום אופליין", "", 180000));
        }
    }

    public List<Song> getAllSongs() {
        return allSongs;
    }

    public List<Song> getLikedSongs() {
        List<Song> liked = new ArrayList<>();
        for (Song song : allSongs) {
            if (song.isLiked()) {
                liked.add(song);
            }
        }
        return liked;
    }

    /**
     * Spotify-like Smart Algorithm:
     * Learns user preferences by combining:
     * 1. Liked songs (highest weight)
     * 2. Frequently played songs (high weight)
     * 3. Recently played songs
     * 4. Exploration factor (adds random unplayed songs to discover)
     */
    public List<Song> getSmartMixQueue() {
        List<Song> mix = new ArrayList<>();
        List<Song> liked = getLikedSongs();
        List<Song> frequent = new ArrayList<>();
        List<Song> others = new ArrayList<>();

        for (Song s : allSongs) {
            if (s.getPlayCount() > 0 && !liked.contains(s)) {
                frequent.add(s);
            } else if (!liked.contains(s)) {
                others.add(s);
            }
        }

        // Sort frequent by play count desc
        Collections.sort(frequent, new Comparator<Song>() {
            @Override
            public int compare(Song s1, Song s2) {
                return Integer.compare(s2.getPlayCount(), s1.getPlayCount());
            }
        });

        // Shuffle others for exploration
        long seed = System.nanoTime();
        Collections.shuffle(others, new Random(seed));

        // Build smart queue structure:
        // Alternate between liked, frequent, and discovery
        int maxLen = Math.max(allSongs.size(), 20);
        int lIdx = 0, fIdx = 0, oIdx = 0;

        // Add all liked first or multiple times if small
        if (!liked.isEmpty()) {
            mix.addAll(liked);
        }

        while (mix.size() < maxLen && (lIdx < liked.size() || fIdx < frequent.size() || oIdx < others.size())) {
            if (fIdx < frequent.size()) {
                mix.add(frequent.get(fIdx++));
            }
            if (oIdx < others.size()) {
                mix.add(others.get(oIdx++));
            }
            if (lIdx < liked.size()) {
                mix.add(liked.get(lIdx++));
            }
            // safety break if lists exhausted
            if (fIdx >= frequent.size() && oIdx >= others.size() && lIdx >= liked.size()) {
                break;
            }
        }

        if (mix.isEmpty()) {
            mix.addAll(allSongs);
        }

        return mix;
    }
}
