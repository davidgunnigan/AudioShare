package com.example.dave.audioshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

/*classname MainActivity.java
date 03/07/2017
author David Gunnigan 15043754
reference https://developers.facebook.com/docs/facebook-login
https://firebase.google.com/docs/android/setup*/


public class MainActivity extends AppCompatActivity {

    private TextView mUser;
    private Button mLogOutButton;
    private Button recordAudio;
    private Button viewRecordings;

   // Facebook SDK provides this ImageView(ProfilePictureView) which makes it easier to work with.
    private ProfilePictureView mProfilePicture;

    // Google Sign In photo
    private ImageView mSignInGooglePicture;

    private static final String USER_EXTRA = "user_extra";

    //Root name of the JSON structure,coming from Facebook
    private static final String USER_PROFILE = "/me";
    private static final String USER_ID = "id";

    private static final String FACEBOOK = "facebook.com";

    // Firebase Authentication.
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUser = (TextView) findViewById(R.id.nameAndSurname);
        mLogOutButton = (Button) findViewById(R.id.logout);
        mSignInGooglePicture = (ImageView) findViewById(R.id.signInGooglePicture);
        mProfilePicture = (ProfilePictureView) findViewById(R.id.friendProfilePicture);
        recordAudio = (Button) findViewById(R.id.record);
        recordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RecordAudio.class);
                startActivity(i);
            }
        });
        viewRecordings = (Button) findViewById(R.id.view);
        viewRecordings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });

        displayUserData();
        setLogOutButton();
    }

    private void displayUserData(){
        setUserName();

        if(isFacebookLogin()) {  // Facebook
             if (AccessToken.getCurrentAccessToken() == null) { //Basically, if the user is not logged in anymore.(Facebook)
                 goLoginScreen();
             }
            setFacebookProfileImage();
        } else {  // Google Sign In
            mProfilePicture.setVisibility(View.GONE);
            mSignInGooglePicture.setVisibility(View.VISIBLE);
            
            if(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null ){
                String photoUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
                setGoogleSignInProfileImage(photoUrl);
            }

        }
    }

    private boolean isFacebookLogin(){
        for (UserInfo user: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals(FACEBOOK)) {
                return true;
            }
        }
        return false;
    }

    //Getting user name from the LoginActivity
    private void setUserName(){
        mUser.setText(getIntent().getStringExtra(USER_EXTRA));
    }

    // Log out from Facebook and Firebase as well.
    private void setLogOutButton() {
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                goLoginScreen();
            }
        });

    }

    // if the user loses his current token for some reason, then go to LoginActivity to log in again.
    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Gets user profile image and then displays it on the ImageView(ProfilePictureView)
    private void setFacebookProfileImage(){
        //In oder to get the user image profile from his/her account,
        // a request is made using GraphRequest(Form part of the Facebook SDK)
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                USER_PROFILE,
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        // the response object has more information about the user than just the image profile.
                        try {
                            mProfilePicture.setProfileId(response.getJSONObject().getString(USER_ID));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    private void setGoogleSignInProfileImage(String url){
        Picasso.with(getApplicationContext())
                .load(url)
                .placeholder(R.drawable.ic_account)
                .error(R.drawable.ic_account)
                .resize(120, 120)
                .centerCrop()
                .into(mSignInGooglePicture);
    }
}
