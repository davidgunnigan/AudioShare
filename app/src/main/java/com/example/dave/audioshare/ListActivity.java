package com.example.dave.audioshare;

import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*classname ListActivity.java
date 03/07/2017
author David Gunnigan 15043754
https://firebase.google.com/docs/android/setup*/

public class ListActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    private ListView mListView;
    private List<Audio> mListAudios = new ArrayList<>();
    private AudioListAdapter mAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private final static String AUDIO_FILES_TABLE = "audios";
    private ProgressDialog mProgress;
    private MediaPlayer mPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mListView = (ListView) findViewById(R.id.listView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading audios...");
        mProgress.show();

        DatabaseReference ref = mFirebaseDatabase.getReference();
        ref.child(AUDIO_FILES_TABLE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot audioDataSnapshot : dataSnapshot.getChildren()) {
                    Audio audio = audioDataSnapshot.getValue(Audio.class);
                    mListAudios.add(audio);
                }

                mAdapter = new com.example.dave.audioshare.AudioListAdapter(getApplicationContext(), mListAudios);
                mListView.setAdapter(mAdapter);
                mProgress.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    createMediaPlayerIfNeeded();
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.setDataSource(mListAudios.get(position).getUrl());
                    mPlayer.prepareAsync(); // Prepares, but does not block the UI thread.

                } catch (IllegalStateException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createMediaPlayerIfNeeded(){
        if(mPlayer == null){
            mPlayer = new MediaPlayer();
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mPlayer.setOnPreparedListener(this);
        }else {
            mPlayer.reset();
        }
    }

    private void releaseMediaPlayer(){
        if(mPlayer != null){
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }
}
