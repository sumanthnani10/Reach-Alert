package com.ismartapps.reachalert;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

public class AdsUnity extends AppCompatActivity {

    private static final String TAG = "Unity Ads Script";
    private final String placementID = "Rewarded_Android";
    private final Boolean testMode = true;
    private Context context;
    private boolean showed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        boolean dark = settings.getBoolean("dark", false);

        if(dark){
            TextView text = findViewById(R.id.textView);
            text.setTextColor(Color.WHITE);
            CardView card = findViewById(R.id.card);
            card.setCardBackgroundColor(Color.BLACK);
        }
        context = this;
        init();
    }

    private void init() {
        final UnityAdsListener myAdsListener = new UnityAdsListener();
        final UnityAdsInitListener myAdsInitListener = new UnityAdsInitListener();
        UnityAds.addListener(myAdsListener);
        String unityGameID = "4257683";
        /*if(UnityAds.isInitialized()){
            showed = true;
            displayRewardedAd();
        } else {*/
            UnityAds.initialize(context, unityGameID, testMode, myAdsInitListener);
//        }
    }

    private void displayRewardedAd () {
        Log.d(TAG, "displayRewardedAd");
        if (UnityAds.isInitialized()) {
            Log.d(TAG, "displayRewardedAd: isInited");
            final UnityAdsShowListener myAdsShowListener = new UnityAdsShowListener();
            UnityAds.show(this, placementID, myAdsShowListener);
        }
    }

    private void goBack(boolean status) {
        Intent backIntent = new Intent();
        backIntent.putExtra("ad_status", status);
        setResult(132, backIntent);
        finish();
    }

    private class UnityAdsInitListener implements IUnityAdsInitializationListener {
        @Override
        public void onInitializationComplete() {
            Log.d(TAG, "onInitializationComplete");
            if(UnityAds.getPlacementState(placementID) == UnityAds.PlacementState.READY && !showed){
                showed = true;
                displayRewardedAd();
            }
        }

        @Override
        public void onInitializationFailed(UnityAds.UnityAdsInitializationError unityAdsInitializationError, String s) {
            Log.d(TAG, "onInitializationFailed: "+unityAdsInitializationError+" "+s);
            goBack(false);
        }
    }

    private class UnityAdsListener implements IUnityAdsListener {

        @Override
        public void onUnityAdsReady(String s) {
            Log.d(TAG, "onUnityAdsReady "+s);
            if(s.equals("Rewarded_Android") && !showed){
                showed = true;
                displayRewardedAd();
            }
        }

        @Override
        public void onUnityAdsStart(String s) {
            Log.d(TAG, "onUnityAdsStart "+s);
        }

        @Override
        public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
            Log.d(TAG, "onUnityAdsFinish: "+s+" "+finishState.toString());
        }

        @Override
        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
            Log.d(TAG, "onUnityAdsFinish: "+s+" "+unityAdsError.toString());
        }
    }

    private class UnityAdsShowListener implements IUnityAdsShowListener {
        @Override
        public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
            Log.d(TAG, "onUnityAdsShowFailure: "+s+" ; "+s1+" ; "+unityAdsShowError.toString());
            goBack(false);
        }

        @Override
        public void onUnityAdsShowStart(String s) {
            Log.d(TAG, "onUnityAdsShowStart: "+s);
        }

        @Override
        public void onUnityAdsShowClick(String s) {
            Log.d(TAG, "onUnityAdsShowClick: "+s);
        }

        @Override
        public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState finishState) {
            Log.d(TAG, "onUnityAdsShowComplete: "+s+" "+finishState);
            goBack(finishState.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED));
        }
    }

}
