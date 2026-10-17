package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements MusicPlayerService.PlayerListener {

    private static final int PERMISSION_REQ_CODE = 101;

    private TextView navHome, navSmart, navLibrary;
    private FrameLayout contentContainer;

    // Mini Player
    private LinearLayout miniPlayerLayout;
    private TextView miniTitle, miniArtist;
    private ImageView miniBtnPlay, miniBtnLike;

    // Full Player
    private RelativeLayout fullPlayerLayout;
    private ImageView fullBtnClose, fullBtnPlay, fullBtnPrev, fullBtnNext, fullBtnLike;
    private TextView fullTitle, fullArtist, txtCurrentTime, txtTotalTime;
    private SeekBar fullSeekBar;

    private MusicPlayerService playerService;
    private MusicLibraryManager libraryManager;

    private int currentTab = 0; // 0: Home, 1: Smart, 2: Library

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        libraryManager = MusicLibraryManager.getInstance(this);
        playerService = MusicPlayerService.getInstance(this);
        playerService.setListener(this);

        initViews();
        setupNavListeners();

        checkPermissionsAndLoad();
        showTab(0);
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navSmart = findViewById(R.id.navSmart);
        navLibrary = findViewById(R.id.navLibrary);
        contentContainer = findViewById(R.id.contentContainer);

        // Mini player
        miniPlayerLayout = findViewById(R.id.miniPlayerLayout);
        miniTitle = findViewById(R.id.miniTitle);
        miniArtist = findViewById(R.id.miniArtist);
        miniBtnPlay = findViewById(R.id.miniBtnPlay);
        miniBtnLike = findViewById(R.id.miniBtnLike);

        miniPlayerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFullPlayer();
            }
        });

        miniBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.togglePlayPause();
            }
        });

        miniBtnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Song song = playerService.getCurrentSong();
                if (song != null) {
                    song.setLiked(!song.isLiked());
                    updateLikeUI(song.isLiked());
                }
            }
        });

        // Full player
        fullPlayerLayout = findViewById(R.id.fullPlayerLayout);
        fullBtnClose = findViewById(R.id.fullBtnClose);
        fullBtnPlay = findViewById(R.id.fullBtnPlay);
        fullBtnPrev = findViewById(R.id.fullBtnPrev);
        fullBtnNext = findViewById(R.id.fullBtnNext);
        fullBtnLike = findViewById(R.id.fullBtnLike);
        fullTitle = findViewById(R.id.fullTitle);
        fullArtist = findViewById(R.id.fullArtist);
        txtCurrentTime = findViewById(R.id.txtCurrentTime);
        txtTotalTime = findViewById(R.id.txtTotalTime);
        fullSeekBar = findViewById(R.id.fullSeekBar);

        fullBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFullPlayer();
            }
        });

        fullBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.togglePlayPause();
            }
        });

        fullBtnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.skipPrevious();
            }
        });

        fullBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.skipNext();
            }
        });

        fullBtnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Song song = playerService.getCurrentSong();
                if (song != null) {
                    song.setLiked(!song.isLiked());
                    updateLikeUI(song.isLiked());
                }
            }
        });

        fullSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerService.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupNavListeners() {
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTab(0); }
        });
        navSmart.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTab(1); }
        });
        navLibrary.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showTab(2); }
        });
    }

    private void showTab(int tabIndex) {
        currentTab = tabIndex;
        updateNavColors();
        contentContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        View view;

        if (tabIndex == 0) {
            view = inflater.inflate(R.layout.fragment_home, contentContainer, false);
            setupHomeFragment(view);
        } else if (tabIndex == 1) {
            view = inflater.inflate(R.layout.fragment_smart, contentContainer, false);
            setupSmartFragment(view);
        } else {
            view = inflater.inflate(R.layout.fragment_library, contentContainer, false);
            setupLibraryFragment(view);
        }

        contentContainer.addView(view);
    }

    private void updateNavColors() {
        int activeColor = 0xFF1DB954;
        int inactiveColor = 0xFFB3B3B3;

        navHome.setTextColor(currentTab == 0 ? activeColor : inactiveColor);
        navSmart.setTextColor(currentTab == 1 ? activeColor : inactiveColor);
        navLibrary.setTextColor(currentTab == 2 ? activeColor : inactiveColor);
    }

    private void checkPermissionsAndLoad() {
        String perm = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
                Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) {
            libraryManager.loadDeviceSongs();
        } else {
            requestPermissions(new String[]{perm}, PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                libraryManager.loadDeviceSongs();
                if (currentTab == 2) {
                    showTab(2); // refresh library
                }
            }
        }
    }

    // --- Home Fragment Setup ---
    private void setupHomeFragment(View view) {
        View cardSmart = view.findViewById(R.id.cardSmartMix);
        cardSmart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Song> smartMix = libraryManager.getSmartMixQueue();
                playerService.setPlaylist(smartMix, 0);
            }
        });

        LinearLayout likedContainer = view.findViewById(R.id.containerLikedSongs);
        List<Song> likedSongs = libraryManager.getLikedSongs();

        if (likedSongs.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("עדיין אין שירים שאהבת. לחץ על לב בשירים כדי להוסיף למועדפים.");
            tv.setTextColor(0xFFB3B3B3);
            tv.setTextSize(14sp);
            tv.setPadding(0, 12, 0, 12);
            likedContainer.addView(tv);
        } else {
            for (int i = 0; i < Math.min(likedSongs.size(), 5); i++) {
                final int index = i;
                final Song song = likedSongs.get(i);
                View row = LayoutInflater.from(this).inflate(R.layout.item_song, likedContainer, false);
                
                TextView title = row.findViewById(R.id.txtSongTitle);
                TextView artist = row.findViewById(R.id.txtSongArtist);
                ImageView btnLike = row.findViewById(R.id.btnItemLike);

                title.setText(song.getTitle());
                artist.setText(song.getArtist());
                btnLike.setImageResource(R.drawable.ic_heart_filled);

                btnLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        song.setLiked(false);
                        showTab(0); // refresh home
                    }
                });

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playerService.setPlaylist(likedSongs, index);
                    }
                });

                likedContainer.addView(row);
            }
        }
    }

    // --- Smart Fragment Setup ---
    private void setupSmartFragment(View view) {
        Button btnPlaySmart = view.findViewById(R.id.btnPlaySmartMix);
        TextView txtStats = view.findViewById(R.id.txtSmartStats);

        int likedCount = libraryManager.getLikedSongs().size();
        int totalCount = libraryManager.getAllSongs().size();
        txtStats.setText(String.format(Locale.getDefault(), "נלמדו %d שירים מתוך %d באחסון המכשיר", likedCount, totalCount));

        btnPlaySmart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Song> smartMix = libraryManager.getSmartMixQueue();
                playerService.setPlaylist(smartMix, 0);
                Toast.makeText(MainActivity.this, "מתחיל מיקס חכם לפי הטעם שלך...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Library Fragment Setup ---
    private void setupLibraryFragment(View view) {
        ListView listView = view.findViewById(R.id.listViewSongs);
        TextView txtStatus = view.findViewById(R.id.txtPermissionStatus);
        Button btnGrant = view.findViewById(R.id.btnGrantPermission);

        String perm = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
                Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
            txtStatus.setVisibility(View.VISIBLE);
            btnGrant.setVisibility(View.VISIBLE);
            btnGrant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{perm}, PERMISSION_REQ_CODE);
                }
            });
            return;
        }

        List<Song> songs = libraryManager.getAllSongs();
        SongAdapter adapter = new SongAdapter(this, songs, new SongAdapter.OnSongActionListener() {
            @Override
            public void onSongClick(int position) {
                playerService.setPlaylist(songs, position);
            }

            @Override
            public void onLikeClick(Song song) {
                // Liked toggled
            }
        });

        listView.setAdapter(adapter);
    }

    // --- Player Listener Callbacks ---
    @Override
    public void onSongChanged(Song song) {
        if (song == null) return;
        miniPlayerLayout.setVisibility(View.VISIBLE);
        miniTitle.setText(song.getTitle());
        miniArtist.setText(song.getArtist());
        updateLikeUI(song.isLiked());

        fullTitle.setText(song.getTitle());
        fullArtist.setText(song.getArtist());
    }

    @Override
    public void onPlaybackStateChanged(boolean isPlaying) {
        int playIcon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow;
        miniBtnPlay.setImageResource(playIcon);
        fullBtnPlay.setImageResource(playIcon);
    }

    @Override
    public void onProgressUpdate(int currentPosition, int duration) {
        fullSeekBar.setMax(duration);
        fullSeekBar.setProgress(currentPosition);
        txtCurrentTime.setText(formatTime(currentPosition));
        txtTotalTime.setText(formatTime(duration));
    }

    private void updateLikeUI(boolean isLiked) {
        int resId = isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart;
        miniBtnLike.setImageResource(resId);
        fullBtnLike.setImageResource(resId);
    }

    private String formatTime(int ms) {
        int seconds = (ms / 1000) % 60;
        int minutes = (ms / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void openFullPlayer() {
        fullPlayerLayout.setVisibility(View.VISIBLE);
    }

    private void closeFullPlayer() {
        fullPlayerLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (fullPlayerLayout.getVisibility() == View.VISIBLE) {
            closeFullPlayer();
        } else {
            super.onBackPressed();
        }
    }
}
=====IMPORT: android.app.Activity=====
=====IMPORT: android.os.Bundle=====
=====IMPORT: android.widget.TextView=====
=====IMPORT: android.view.View=====
=====IMPORT: android.widget.LinearLayout=====
=====IMPORT: android.widget.FrameLayout=====
=====IMPORT: android.widget.ImageView=====
=====IMPORT: android.widget.RelativeLayout=====
=====IMPORT: android.widget.SeekBar=====
=====IMPORT: android.widget.ListView=====
=====IMPORT: android.widget.Button=====
=====IMPORT: android.widget.Toast=====
=====IMPORT: android.content.pm.PackageManager=====
=====IMPORT: android.Manifest=====
=====IMPORT: android.os.Build=====
=====IMPORT: android.view.LayoutInflater=====
=====IMPORT: java.util.List=====
=====IMPORT: java.util.Locale=====
=====IMPORT: com.generated.app.Song=====
=====IMPORT: com.generated.app.MusicPlayerService=====
=====IMPORT: com.generated.app.MusicLibraryManager=====
=====IMPORT: com.generated.app.SongAdapter=====
=====IMPORT: com.generated.app.R=====
=====IMPORT: android.content.Context=====
=====IMPORT: android.media.MediaPlayer=====
=====IMPORT: android.net.Uri=====
=====IMPORT: android.os.Handler=====
=====IMPORT: android.os.Looper=====
=====IMPORT: java.io.File=====
=====IMPORT: java.util.ArrayList=====
=====IMPORT: android.content.ContentResolver=====
=====IMPORT: android.database.Cursor=====
=====IMPORT: android.provider.MediaStore=====
=====IMPORT: java.util.Collections=====
=====IMPORT: java.util.Comparator=====
=====IMPORT: java.util.Random=====
=====IMPORT: android.view.ViewGroup=====
=====IMPORT: android.widget.BaseAdapter=====
