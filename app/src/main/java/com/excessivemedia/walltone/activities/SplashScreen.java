package com.excessivemedia.walltone.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.CategoryUtils;
import com.excessivemedia.walltone.helpers.HighlightUtils;
import com.excessivemedia.walltone.helpers.Utils;
import com.excessivemedia.walltone.widgets.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SplashScreen extends AppCompatActivity implements GoogleSignIn.OnGoogleSignInListener {

    private static final int GOOGLE_SIGN_IN = 9001;
    private static final String TAG = SplashScreen.class.getSimpleName();

    private FirebaseAuth mAuth;
    private Context mContext;

    private Snackbar internet_connection_weak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        mContext = this;
        startUI(mAuth.getCurrentUser());

        Utils.gradientTextView(findViewById(R.id.welcome),
                Color.parseColor("#be0dff"),
                Color.parseColor("#ff2a42"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(()->{
            HighlightUtils.getInstance().fetchHighlights();
            CategoryUtils.getInstance().fetchCategory();
        },10);
    }

    @Override
    public void googleSignInClicked() {

        GoogleSignInOptions mGSO = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(mContext, mGSO);
        Intent signInIntent = client.getSignInIntent();
        startActivityForResult(signInIntent,GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account == null ){
                    throw new NullPointerException("Account null Exception");
                }
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException | NullPointerException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                updateUser(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rootLayout), "Logging in...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        snackbar.dismiss();
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUser(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUser(null);
                    }

                });
    }

    private void updateUser(FirebaseUser user) {
        if(user == null){
            Snackbar.make(findViewById(R.id.rootLayout),"Log in failed...",Snackbar.LENGTH_SHORT)
                    .setAction("Retry",v->googleSignInClicked())
                    .show();
        }else {
                startUI(user);
        }
    }

    private void startUI(FirebaseUser user) {
        new Handler().postDelayed(()->{
            if(user!=null){
                internetConnectivityCheck();
                startUI();
            }else{
                MotionLayout motionLayout = findViewById(R.id.rootLayout);
                GoogleSignIn googleSignInBtn = findViewById(R.id.googleSignInButton);
                (findViewById(R.id.skip)).setOnClickListener(this::skipSignIn);
                googleSignInBtn.setListener(this);
                motionLayout.transitionToEnd();
                motionLayout.addTransitionListener(new MotionLayout.TransitionListener() {
                    @Override
                    public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                    }

                    @Override
                    public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {
                    }

                    @Override
                    public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                        googleSignInBtn.startAnimation();

                    }

                    @Override
                    public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
                    }

                });

            }
        },1200);
    }

    private void skipSignIn(android.view.View v) {
        Snackbar snackbar = Snackbar.make(v, "Getting Ready...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        final boolean[] visible = {true};
        mAuth.signInAnonymously().addOnSuccessListener(authResult -> {
            snackbar.dismiss();
            visible[0] = false;
            internetConnectivityCheck();
            startUI();
        }).addOnFailureListener(e -> {
            snackbar.dismiss();
            visible[0] = false;
            Snackbar.make(v,"Error Occurred",Snackbar.LENGTH_SHORT)
                    .show();
        });
        new Handler().postDelayed(()->{
            if(visible[0]){

                snackbar.setText("Network Issue...");
            }
        },5000);
    }
    private void internetConnectivityCheck(){
        internet_connection_weak = Snackbar.make(findViewById(R.id.logo), "Internet connection weak", Snackbar.LENGTH_INDEFINITE);
        new Handler().postDelayed(()->{
            if(internet_connection_weak!=null) internet_connection_weak.show();
        },5000);
    }
    private void startUI() {
        if(!(HighlightUtils.getInstance().isUpdate() && CategoryUtils.getInstance().isUpdate())){
            new Handler().postDelayed(this::startUI,200);
            return;
        }
        if(internet_connection_weak !=null && internet_connection_weak.isShown()){
            internet_connection_weak.dismiss();
        }
        internet_connection_weak = null;
        startActivity(new Intent(mContext,Home.class));
        finish();
    }

}