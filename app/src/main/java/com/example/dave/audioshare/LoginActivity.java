package com.example.dave.audioshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.List;





public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginButton mLoginButton;
    private SignInButton mSignInButton;
    private GoogleApiClient mGoogleApiClient;


    // Firebase Authentication.
    private FirebaseAuth mAuth;

    // It is triggered when a user is signed in or signed out.
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Data that we want to get from our user Facebook account.
    private List<String> mPermissionsFacebook = Arrays.asList("email", "public_profile");

    // Tag for sending user data through Intent to the MainActivity.
    private static final String USER_EXTRA = "user_extra";

    private static final int RC_SIGN_IN = 1;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        mProgressDialog = new ProgressDialog(this);

        mSignInButton= ((SignInButton) findViewById(R.id.googleBtn));
        makeSignInTextCentered();

        //Facebook
        setLoginButton();
        setLoginButtonFacebookCallback();

        //Google Sign In
        setSignInButton();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setAuthListener();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * It is called when there is a change in the authentication state.
     */
    private void setAuthListener(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    goMainActivity(user.getDisplayName());
                    Log.d(LoginActivity.class.getSimpleName(), "onAuthStateChanged:signed_in:" + user.getDisplayName());
                } else {
                    // User is signed out
                    Log.d(LoginActivity.class.getSimpleName(), "onAuthStateChanged:signed_out");
                }

            }
        };
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {  // Google Sign In
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {   // Facebook login
            // Passing these 3 values to the Facebook SDK to make the login process
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Gets the login button references from the XML file and then
     * set permissions to get accessed to the data
     */
    private void setLoginButton(){
        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        mLoginButton.setReadPermissions(mPermissionsFacebook);
    }

    /**
     *  When the user clicks on the login button, an Activity(FacebookActivity) will be started,
     *  and when this activity completes its job, it's going to send 3 values(requestCode, resultCode, data)
     *  which are going to be received in the MainActivity through onActivityResult method.
     */
    private void setLoginButtonFacebookCallback(){
        mLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                signInWithFacebookOnFirebase(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(LoginActivity.class.getSimpleName(),"Sign in has been canceled! -_-");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(LoginActivity.class.getSimpleName(),"Something went wrong! :'(");
            }
        });
    }

    /**
     * Gets signed in Firebase, using user Facebook data.
     */
    private void signInWithFacebookOnFirebase(AccessToken token) {
        mProgressDialog.setMessage("Logging...");
        mProgressDialog.show();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // if Sign in success, go to the MainActivity and displays user information.
                            FirebaseUser user = mAuth.getCurrentUser();
                            goMainActivity(user.getDisplayName());
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Facebook Authentication failed. o.O",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void setSignInButton(){
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    /**
     * Gets signed in Google
     */
    private void signInWithGoogleOnFirebase(GoogleSignInAccount acct) {
        mProgressDialog.setMessage("Logging...");
        mProgressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgressDialog.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            goMainActivity(user.getDisplayName());
                            Toast.makeText(getApplicationContext(), ""+user.getDisplayName(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Google Authentication failed. :(",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    // Getting data from Google Sign in
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            signInWithGoogleOnFirebase(acct);
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(LoginActivity.this, "Something went wrong! :'(",
                    Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * If sign in success, this method will be invoked.
     * @param user User data Authentication.
     */
    private void goMainActivity( String user ){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(USER_EXTRA, user);
        startActivity(intent);
    }

    private void makeSignInTextCentered(){
        for (int i = 0; i < mSignInButton.getChildCount(); i++) {
            View v = mSignInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setPadding(0, 0, 20, 0);
                return;
            }
        }
    }

}



