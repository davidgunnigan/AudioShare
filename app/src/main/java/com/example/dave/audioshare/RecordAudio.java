package com.example.dave.audioshare;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/*classname Audio.java
date 03/07/2017
author David Gunnigan 15043754
https://firebase.google.com/docs/android/setup*/

public class RecordAudio extends AppCompatActivity {

    private Button mRecord;
    private Button mUpload;
    private Button mPlay;
    private Button mStop;

    private String mFileName = null;
    private MediaRecorder mRecorder;

    private StorageReference mStorage;
    private ProgressDialog mProgress;

    private FirebaseDatabase mFirebaseDatabase;
    private final static String AUDIO_FILES_TABLE = "audios";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);

        mRecord = (Button) findViewById(R.id.recordButton);
        mStop = (Button) findViewById(R.id.stopButton);
        mPlay = (Button) findViewById(R.id.play);
        mUpload = (Button) findViewById(R.id.upload);

        mProgress = new ProgressDialog(this);

        mStorage = FirebaseStorage.getInstance().getReference();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mStop.setEnabled(false);
        mPlay.setEnabled(false);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recorded_audio.3gp";

        // If you're running this app on a device with Version 6.0 and up.
        // This is the new way to ask for permissions so the user can have control which permissions to grant.
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                setActionsButtons();

            } else {
                ActivityCompat.requestPermissions(RecordAudio.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            setActionsButtons();
        }



    }

    // Basically, checks if a particular table already exists
    boolean exists;
    public boolean checkIfitExists(final String elementToCheck){
        DatabaseReference mRef =  mFirebaseDatabase.getReference();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(elementToCheck) || snapshot.child(elementToCheck).exists()) {
                    exists = true;
                } else {
                    exists = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return exists;
    }

    public void setActionsButtons(){
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    mRecorder.setOutputFile(mFileName);
                    mRecorder.prepare();
                    mRecorder.start();
                } catch (IllegalStateException ise) {

                } catch (IOException ioe) {

                }

                mRecord.setEnabled(false);
                mStop.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();

            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.stop();
                mRecorder = null;
                mRecord.setEnabled(true);
                mStop.setEnabled(false);
                mPlay.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
            }
        });


        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(mFileName);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();

                } catch (Exception e) {

                }
            }
        });


        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgress.setMessage("Uploading Audio...");
                mProgress.show();


                Uri uri = Uri.fromFile(new File(mFileName));
                // UUID generates unique ID
                StorageReference filepath = mStorage.child("Audios/"+UUID.randomUUID() + ".3gp");

                // retrieves recording from storage and sends to database
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgress.dismiss();


                        if(!checkIfitExists(AUDIO_FILES_TABLE)) {

                            final DatabaseReference audios = mFirebaseDatabase.getReference(AUDIO_FILES_TABLE);

                            // Generates unique ids for each audio
                            String uniqueAudioID = audios.push().getKey();

                            Audio audio = new Audio(
                                    uniqueAudioID,  // ID
                                    taskSnapshot.getMetadata().getName(), // Name
                                    taskSnapshot.getMetadata().getDownloadUrl().toString(), //  url
                                    taskSnapshot.getMetadata().getCreationTimeMillis() // created Date
                            );

                            audios.child(uniqueAudioID).setValue(audio);

                        }

                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == 1){
            setActionsButtons();
        }
    }
}
