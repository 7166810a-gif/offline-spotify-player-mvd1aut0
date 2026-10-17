package com.generated.app;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private String path;
    private long duration;
    private boolean isLiked;
    private int playCount;
    private long lastPlayedTimestamp;

    public Song(long id, String title, String artist, String album, String path, long duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
        this.isLiked = false;
        this.playCount = 0;
        this.lastPlayedTimestamp = 0;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getPath() { return path; }
    public long getDuration() { return duration; }
    
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public int getPlayCount() { return playCount; }
    public void incrementPlayCount() { 
        this.playCount++;
        this.lastPlayedTimestamp = System.currentTimeMillis();
    }

    public long getLastPlayedTimestamp() { return lastPlayedTimestamp; }
}
