package com.generated.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SongAdapter extends BaseAdapter {
    private Context context;
    private List<Song> songList;
    private OnSongActionListener actionListener;

    public interface OnSongActionListener {
        void onSongClick(int position);
        void onLikeClick(Song song);
    }

    public SongAdapter(Context context, List<Song> songList, OnSongActionListener actionListener) {
        this.context = context;
        this.songList = songList;
        this.actionListener = actionListener;
    }

    public void updateList(List<Song> newList) {
        this.songList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return songList != null ? songList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return songList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        }

        Song song = songList.get(position);

        TextView txtTitle = convertView.findViewById(R.id.txtSongTitle);
        TextView txtArtist = convertView.findViewById(R.id.txtSongArtist);
        ImageView btnLike = convertView.findViewById(R.id.btnItemLike);

        txtTitle.setText(song.getTitle());
        txtArtist.setText(song.getArtist());

        if (song.isLiked()) {
            btnLike.setImageResource(R.drawable.ic_heart_filled);
        } else {
            btnLike.setImageResource(R.drawable.ic_heart);
        }

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                song.setLiked(!song.isLiked());
                if (song.isLiked()) {
                    btnLike.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    btnLike.setImageResource(R.drawable.ic_heart);
                }
                if (actionListener != null) {
                    actionListener.onLikeClick(song);
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionListener != null) {
                    actionListener.onSongClick(position);
                }
            }
        });

        return convertView;
    }
}
