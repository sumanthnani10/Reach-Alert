package com.ismartapps.reachalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    public String name;
    String TAG = "Login Activity";

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate");
        Log.d(TAG, "onStart");
        createSignInIntent();
        SharedPreferences settings = getSharedPreferences("settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("dark",false);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed(){
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void createSignInIntent() {
        Log.d(TAG, "createSignInIntent");
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setLogo(R.mipmap.ic_launcher_icon)
                .setTheme(R.style.AppTheme)
                .build();
        signInLauncher.launch(signInIntent);

        /*startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .setLogo(R.mipmap.ic_launcher_icon)
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);*/
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        Log.d(TAG, "onSignInResult");
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Log.d(TAG, "onActivityResult: Sign in Success");
            startMain();

        } else {
            Log.d(TAG, "onActivityResult: Sign in failed");
        }
    }

    public void startMain(){
        Log.d(TAG, "startMain");
        Intent intent = getIntent();
        Intent mainIntent = new Intent(this, SplashActivity.class);
        
        if(intent.getStringExtra("from").equals("SAN"))
        {
            String text = intent.getStringExtra("name");
            Log.d(TAG, "onCreate: "+text+" , "+intent.getExtras());
            double[] latLng = intent.getExtras().getDoubleArray("latlng");
            mainIntent.putExtra("name",text);
            mainIntent.putExtra("placeId",intent.getStringExtra("placeId"));
            mainIntent.putExtra("latlng",latLng);
        }
        
        else if(intent.getStringExtra("shared location")!=null)
        {
            Log.d(TAG, "startMain: ---------------");
            String s = intent.getStringExtra("shared location");
            mainIntent.putExtra("shared location",s);
        }
        startActivity(mainIntent);
        finish();
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Logged Out");
                    }
                });
    }

    public void privacyAndTerms() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTosAndPrivacyPolicyUrls(
                                "https://sites.google.com/view/reach-alert/privacy-policy",
                                "https://sites.google.com/view/reach-alert/privacy-policy")
                        .build(),
                RC_SIGN_IN);
    }

}
