package com.xlab13.playhacker.test;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.xlab13.playhacker.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.IconOverlay;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.xlab13.playhacker.activities.InitializationActivity.mTools;

public class MapActivity extends Activity {
    Context context;


    @BindView(R.id.mvMap)
    MapView mvMap;


    Location myLocation;

    IMapController mapController;

    LocationManager locationManager;
    LocationRequest locationRequest;
    LocationSettingsRequest locationSettingsRequest;

    FusedLocationProviderClient fusedLocationProviderClient;

    IconOverlay mIconOverlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();


        IConfigurationProvider configurationProvider = Configuration.getInstance();
        configurationProvider.setDebugMode(true);
        configurationProvider.getOsmdroidBasePath(this);
        configurationProvider.load(this, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        context = this;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        mvMap.setTileSource(TileSourceFactory.MAPNIK);
        mvMap.setMultiTouchControls(true);
        mapController = mvMap.getController();
        mapController.setZoom(16.5);


        mIconOverlay = new IconOverlay();

        mvMap.getOverlays().add(mIconOverlay);

   }

    @Override
    public void onResume() {
        super.onResume();
        mvMap.onResume();
        startTracking();
    }

    @Override
    public void onPause() {
        super.onPause();
        mvMap.onPause();
    }

    private void startTracking(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(7000);
        locationRequest.setFastestInterval(4000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();

        LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            if (isNewLocation(location)){
                                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                                mapController.setCenter(startPoint);




                                //mIconOverlay.set(startPoint, getDrawable(R.drawable.map_player));
                            }
                        }
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                    if (!locationAvailability.isLocationAvailable()){
                    }
                }
            };

        //fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private boolean isNewLocation(Location newLocation){
        if (myLocation == null){
            myLocation = newLocation;
            return true;
        }

        MathContext mathContext = new MathContext(15, RoundingMode.HALF_UP);

        BigDecimal bigDecimal = new BigDecimal(myLocation.getLatitude(), mathContext);
        bigDecimal = bigDecimal.setScale(4, BigDecimal.ROUND_DOWN);
        double lastLatitude = bigDecimal.doubleValue();

        bigDecimal = new BigDecimal(myLocation.getLongitude(), mathContext);
        bigDecimal = bigDecimal.setScale(4, BigDecimal.ROUND_DOWN);
        double lastLongitude = bigDecimal.doubleValue();



        bigDecimal = new BigDecimal(newLocation.getLatitude(), mathContext);
        bigDecimal = bigDecimal.setScale(4, BigDecimal.ROUND_DOWN);
        double newLatitude = bigDecimal.doubleValue();

        bigDecimal = new BigDecimal(newLocation.getLongitude(), mathContext);
        bigDecimal = bigDecimal.setScale(4, BigDecimal.ROUND_DOWN);
        double newLongitude = bigDecimal.doubleValue();

        if (lastLatitude != newLatitude || lastLongitude != newLongitude){
            myLocation = newLocation;
            return true;
        }
        else return false;
    }

    @OnClick(R.id.btnMapClose)
    public void onCloseCLick(View v){
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    1);
        }
    }

}