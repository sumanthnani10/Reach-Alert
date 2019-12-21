package com.ismartapps.reachalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

public class MapsActivitySecondary extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String TAG = "Maps Activity Secondary";
    private TextView targetPlaceName,targetPlaceType,targetPlaceAddress,confirm;
    private CardView searchCard, radiusControlCard;
    private SeekBar radiusController;
    private Circle circle = null;
    private RelativeLayout placeDetailsContainer;
    private Marker marker = null;
    private ImageView mCurrLoc;
    private ImageView mRadiusTick,zoomIn,zoomOut;
    private LatLng targetLatLng;
    private InterstitialAd interstitialAd;
    private TargetDetails targetDetails;
    private int clickCount=0,activityCount;
    private DrawerLayout drawerLayout;
    private boolean dark;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        dark = settings.getBoolean("dark",false);
        if(dark)
            setContentView(R.layout.activity_maps_dark);
        else{
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_maps);
        }
        activityCount=0;
        Log.d(TAG, "initMap: (Secondary) initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_primary);
        mapFragment.getMapAsync(this);
        //ADCOLONY-AdColony.configure(this, "app72332c41df0a460897", "vz1446be3697b24002b8");
        AudienceNetworkAds.initialize(this);
        AdSettings.setIntegrationErrorMode(AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE);
        interstitialAd = new InterstitialAd(this, "478651842722184_478653109388724");
        interstitialAd.loadAd();
        Log.d(TAG, "onCreate: Secondary");
    }

    private void initVars(){
        targetPlaceName = findViewById(R.id.place_name);
        targetPlaceType = findViewById(R.id.place_type);
        targetPlaceAddress = findViewById(R.id.place_address);
        confirm = findViewById(R.id.confirm);
        mCurrLoc = findViewById(R.id.location_btn_img);
        searchCard = findViewById(R.id.searchbar_layout_card);
        mRadiusTick = findViewById(R.id.place_tick_image);
        radiusControlCard = findViewById(R.id.radius_controller_container_card);
        radiusController = findViewById(R.id.radius_controller);
        zoomIn = findViewById(R.id.zoom_in);
        zoomOut = findViewById(R.id.zoom_ot);
        drawerLayout = findViewById(R.id.drawer_layout);
        placeDetailsContainer = findViewById(R.id.Place_details_view_relative_container);
    }

    private void init(){
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Intent intent = getIntent();
        targetDetails = intent.getExtras().getParcelable("targetDetails");
        marker=null;
        circle=null;
        confirm.setText("Confirm Radius");
        if(dark)
            mRadiusTick.setImageResource(R.mipmap.ic_launcher_confirm_radius_dark);
        else
            mRadiusTick.setImageResource(R.mipmap.ic_launcher_confirm_radius);
        mRadiusTick.setVisibility(View.VISIBLE);
        targetPlaceName.setText(targetDetails.getName());
        targetPlaceType.setText(targetDetails.getType());
        targetPlaceAddress.setText(targetDetails.getAddress());
        mCurrLoc.setVisibility(View.INVISIBLE);
        searchCard.setVisibility(View.INVISIBLE);
        radiusControlCard.setVisibility(View.VISIBLE);
        targetLatLng = targetDetails.getTarget();
        LatLng currentLatLng = targetDetails.getCurrent();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng,15f));
        marker = mMap.addMarker(new MarkerOptions().position(targetLatLng).title(targetPlaceName.getText().toString()).draggable(false));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng,15f));

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i)
            {
                placeDetailsContainer.setVisibility(View.INVISIBLE);
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                placeDetailsContainer.setVisibility(View.VISIBLE);}
        });

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.splash_animation);
        animation.setDuration(3000);
        radiusController.setAnimation(animation);

        int minRadius=500;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            radiusController.setMin(minRadius);
        }

        radiusController.setMax(5000);
        circle = mMap.addCircle(new CircleOptions()
                .center(targetLatLng)
                .strokeWidth(3)
                .radius(minRadius)
                .strokeColor(R.color.imageColor3));

        if (dark)
        {
            circle.setStrokeColor(R.color.quantum_black_100);
            circle.setFillColor(R.color.white);
        }

        radiusController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (radiusController.getProgress() < minRadius) {
                    radiusController.setProgress(minRadius);
                }
                circle.setRadius(radiusController.getProgress());
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(toBounds(targetLatLng,radiusController.getProgress()),100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mRadiusTick.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mRadiusTick.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(MapsActivitySecondary.this,R.anim.tick_anim);
                mRadiusTick.startAnimation(animation);
                Toast.makeText(MapsActivitySecondary.this, "Radius set to "+(int) circle.getRadius()+"m", Toast.LENGTH_SHORT).show();
            }
        });

        mRadiusTick.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivitySecondary.this, "Confirm Radius", Toast.LENGTH_SHORT).show();
            return true;
        });

        zoomOut.setOnClickListener(view -> {
            CameraPosition zoomOutPosition = new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom((float) (mMap.getCameraPosition().zoom-(0.75))).build();
            Log.d(TAG, "onClick: Zoom Out");
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(zoomOutPosition));
        });

        zoomIn.setOnClickListener(view -> {
            CameraPosition zoomInPosition = new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom((float) (mMap.getCameraPosition().zoom+(0.75))).build();
            Log.d(TAG, "onClick: Zoom In");
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(zoomInPosition));
        });

        zoomIn.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivitySecondary.this, "Zoom In", Toast.LENGTH_SHORT).show();
            return true;
        });

        zoomOut.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivitySecondary.this, "Zoom Out", Toast.LENGTH_SHORT).show();
            return false;
        });

        mRadiusTick.setOnClickListener(view -> {
            clickCount++;

            if (clickCount==1){
                Toast.makeText(this, "Radius Confirmed", Toast.LENGTH_SHORT).show();
                //Show Ad
                goToFinal();
                finish();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCount++;

                if (clickCount==1){
                    Toast.makeText(MapsActivitySecondary.this, "Radius Confirmed", Toast.LENGTH_SHORT).show();
                    //Show Ad
                    goToFinal();
                    finish();
                }

            }
        });
    }

    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    @Override
    protected void onDestroy() {
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(activityCount==0)
        {
            activityCount++;
        }
        else
        {
            showAd();
        }
    }

    void showAd()
    {
        Log.d(TAG, "showAd");
        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                Log.d(TAG, "onInterstitialDisplayed: ");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                Log.d(TAG, "onInterstitialDismissed: ");
                if(interstitialAd!=null){
                    interstitialAd.destroy();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "onError: ");
                if(interstitialAd!=null){
                    interstitialAd.destroy();
                }

            }

            @Override
            public void onAdLoaded(Ad ad) {
                Log.d(TAG, "onAdLoaded: ");
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                Log.d(TAG, "onAdClicked: ");
                if(interstitialAd!=null){
                    interstitialAd.destroy();
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                Log.d(TAG, "onLoggingImpression: ");
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void goToFinal() {
        Intent intent = new Intent(this,MapsActivityFinal.class);
        targetDetails.setRadius(circle.getRadius());
        intent.putExtra("targetDetails", targetDetails);
        intent.putExtra("from",2);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: (Secondary) map is ready");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setPadding(0,0,0,400);
        initVars();
        init();
        if(dark)
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapstyle_night));
    }


}
