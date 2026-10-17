package com.generated.app;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerService {
    private static MusicPlayerService instance;
    private MediaPlayer mediaPlayer;
    private List<Song> currentPlaylist;
    private int currentIndex;
    private boolean isPlaying;
    private Context context;
    private PlayerListener listener;
    private Handler progressHandler;
    private Runnable progressRunnable;

    public interface PlayerListener {
        void onSongChanged(Song song);
        void onPlaybackStateChanged(boolean isPlaying);
        void onProgressUpdate(int currentPosition, int duration);
    }

    private MusicPlayerService(Context context) {
        this.context = context.getApplicationContext();
        this.currentPlaylist = new ArrayList<>();
        this.currentIndex = 0;
        this.isPlaying = false;
        this.progressHandler = new Handler(Looper.getMainLooper());
        
        setupProgressRunnable();
    }

    public static synchronized MusicPlayerService getInstance(Context context) {
        if (instance == null) {
            instance = new MusicPlayerService(context);
        }
        return instance;
    }

    public void setListener(PlayerListener listener) {
        this.listener = listener;
    }

    public void setPlaylist(List<Song> playlist, int startIndex) {
        if (playlist == null || playlist.isEmpty()) return;
        this.currentPlaylist = new ArrayList<>(playlist);
        this.currentIndex = Math.max(0, Math.min(startIndex, currentPlaylist.size() - 1));
        playCurrentSong();
    }

    public void playCurrentSong() {
        if (currentPlaylist.isEmpty()) return;
        Song song = currentPlaylist.get(currentIndex);

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }

        // Check if real file exists
        if (song.getPath() != null && !song.getPath().isEmpty() && new File(song.getPath()).exists()) {
            try {
                mediaPlayer = MediaPlayer.create(context, Uri.parse(song.getPath()));
                if (mediaPlayer != null) {
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            skipNext();
                        }
                    });
                    mediaPlayer.start();
                    isPlaying = true;
                    song.incrementPlayCount();
                }
            } catch (Exception e) {
                e.printStackTrace();
                isPlaying = false;
            }
        } else {
            // Simulated playback for sample items or missing files
            isPlaying = true;
            song.incrementPlayCount();
        }

        if (listener != null) {
            listener.onSongChanged(song);
            listener.onPlaybackStateChanged(isPlaying);
        }

        startProgressTracking();
    }

    public void togglePlayPause() {
        if (currentPlaylist.isEmpty()) return;
        
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPlaying = false;
                    stopProgressTracking();
                } else {
                    mediaPlayer.start();
                    isPlaying = true;
                    startProgressTracking();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            isPlaying = !isPlaying;
            if (isPlaying) startProgressTracking();
            else stopProgressTracking();
        }

        if (listener != null) {
            listener.onPlaybackStateChanged(isPlaying);
        }
    }

    public void skipNext() {
        if (currentPlaylist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % currentPlaylist.size();
        playCurrentSong();
    }

    public void skipPrevious() {
        if (currentPlaylist.isEmpty()) return;
        currentIndex = (currentIndex - 1 + currentPlaylist.size()) % currentPlaylist.size();
        playCurrentSong();
    }

    public void seekTo(int progress) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.seekTo(progress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Song getCurrentSong() {
        if (currentPlaylist.isEmpty() || currentIndex < 0 || currentIndex >= currentPlaylist.size()) {
            return null;
        }
        return currentPlaylist.get(currentIndex);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void setupProgressRunnable() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && listener != null) {
                    int pos = 0;
                    int dur = 180000;
                    if (mediaPlayer != null) {
                        try {
                            pos = mediaPlayer.getCurrentPosition();
                            dur = mediaPlayer.getDuration();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    listener.onProgressUpdate(pos, dur);
                }
                progressHandler.postDelayed(this, 1000);
            }
        };
    }

    private void startProgressTracking() {
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.post(progressRunnable);
    }

    private void stopProgressTracking() {
        progressHandler.removeCallbacks(progressRunnable);
    }
}
