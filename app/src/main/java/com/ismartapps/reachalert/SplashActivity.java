package com.ismartapps.reachalert;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends Activity {
    private static final String TAG = "SA";
    private Intent intent,mainIntent;
    private boolean fromShare;
    private LatLng latLng;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.setContentView(R.layout.splash_activity);
        ImageView imageView = findViewById(R.id.splash_img);
        ImageView shadow = findViewById(R.id.splash_shadow);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.splash_animation);
        imageView.startAnimation(animation);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            shadow.setVisibility(View.VISIBLE);
            shadow.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this,R.anim.shadow_anim));
        },1500);

        fromShare = false;
        mainIntent = getIntent();
        action = mainIntent.getAction();
        Uri data;

        Log.d(TAG, "onCreate: -");

        if(mainIntent.getBooleanExtra("fromNotification",false))
        {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(mainIntent.getExtras().getInt("id"));
        }
        else if(action!=null)
        {
            if(action.equals(Intent.ACTION_VIEW))
            {
                fromShare = true;
                data = mainIntent.getData();
                action = data.toString();
                //latLng = new LatLng(Double.parseDouble(action.substring(action.indexOf(":")+1,action.indexOf(","))),Double.parseDouble(action.substring(action.indexOf(",")+1,action.indexOf("?"))));
                Log.d(TAG, "onCreate:------+ ");
            }
        }

        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(2500);
                }catch (InterruptedException e)
                {
                    Log.d(TAG, "run: "+e);
                }
                finally {
                    SharedPreferences targetDetails = getSharedPreferences("targetDetails",MODE_PRIVATE);
                    if(!isServiceRunning(getPackageName()+"."+"LocationUpdatesService"))
                    {
                        SharedPreferences.Editor editor = targetDetails.edit();
                        editor.clear();
                        editor.apply();
                        if(mainIntent.getBooleanExtra("fromNotification",false) && !fromShare)
                        {
                            SharedPreferences sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child("Last Used").child(sharedPreferences.getString("dbname", "User Name")).child("Notification Clicked").setValue(new SimpleDateFormat("dd-MMM-yyyy,E hh:mm:ss a zzzz",new Locale("EN")).format(new Date()));
                            String text = mainIntent.getStringExtra("name");
                            Log.d(TAG, "onCreate: "+text+" , "+mainIntent.getExtras());
                            double[] latLng = mainIntent.getExtras().getDoubleArray("latlng");
                            intent = new Intent(SplashActivity.this, LoginActivity.class);
                            intent.putExtra("from","SAN");
                            intent.putExtra("name",text);
                            intent.putExtra("placeId",mainIntent.getStringExtra("placeId"));
                            intent.putExtra("latlng",latLng);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            intent = new Intent(SplashActivity.this, LoginActivity.class);
                            intent.putExtra("from","SA");
                            if (fromShare)
                            {
                                intent.putExtra("shared location",action);
                                intent.putExtra("from","shared");
                            }
                        }
                    }
                    else
                    {
                        intent = new Intent(SplashActivity.this,MapsActivityFinal.class);
                        intent.putExtra("from",1);
                    }
                    Log.d(TAG, "run: "+intent);
                    startActivity(intent);
                    finish();

                }
            }
        };
        thread.start();

    }

    private boolean isServiceRunning(String serviceName){
        boolean serviceRunning = false;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(50);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : l) {
            if (runningServiceInfo.service.getClassName().equals(serviceName)) {
                serviceRunning = runningServiceInfo.foreground;
            }
        }
        return serviceRunning;
    }
}
