package com.oneuphero.pogotracker;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String BASE_URL = "https://pogo-tracker.herokuapp.com/";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5001;

    private GoogleMap mMap;
    private ApiEndpointInterface mApiService;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private List<PokemonSpawn> mSpawns = new ArrayList<>();
    private List<PicassoMarker> mPicassoMarkers = new ArrayList<>(); // to ensure markers don't get GC'd before PicassoMarker updates
    private Marker mCurrentLocationMarker;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.mapsCoordinatorLayout);

        Toolbar actionToolbar = (Toolbar) findViewById(R.id.action_toolbar);
        setSupportActionBar(actionToolbar);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mApiService = retrofit.create(ApiEndpointInterface.class);

        FirebaseMessaging.getInstance().subscribeToTopic("pokemon_spawns");
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        moveCameraToCurrentLocation();

        if (!mSpawns.isEmpty()) {
            addSpawnsToMap();
        }
    }

    private void moveCameraToCurrentLocation() {
        if (mMap != null && mLastLocation != null) {
            LatLng userLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            if (mCurrentLocationMarker != null) mCurrentLocationMarker.remove();
            mCurrentLocationMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
//                return true;
                return false;

            case R.id.action_refresh:
                updateLastLocation();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateLastLocation();
    }

    private void updateLastLocation() {
        if (Helper.haveLocationPermissions(this)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        //noinspection MissingPermission
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            PreferencesStore.setLastLocation(this, mLastLocation);
            moveCameraToCurrentLocation();
            String ll = String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude());
            final Snackbar loadingSnackbar = Snackbar.make(mCoordinatorLayout, R.string.tracking_pokemon, Snackbar.LENGTH_INDEFINITE);
            mApiService.getSpawns(ll).enqueue(new Callback<List<PokemonSpawn>>() {
                @Override
                public void onResponse(Call<List<PokemonSpawn>> call, Response<List<PokemonSpawn>> response) {
                    if (loadingSnackbar.isShown()) loadingSnackbar.dismiss();
                    if (response.isSuccessful()) {
                        List<PokemonSpawn> spawns = response.body();
                        if (spawns == null) {
                            showErrorSnackbar(R.string.no_pokemon_found);
                            Log.e(MapsActivity.class.getSimpleName(), "No spawns received!");
                            Log.e(MapsActivity.class.getSimpleName(), response.toString());
                        } else {
                            mSpawns.clear();
                            mSpawns.addAll(spawns);
                            addSpawnsToMap();
                        }
                    } else {
                        showErrorSnackbar(R.string.request_failure);
                        Log.e(MapsActivity.class.getSimpleName(), "getSpawns failed, status " + String.valueOf(response.code()));
                    }
                }

                @Override
                public void onFailure(Call<List<PokemonSpawn>> call, Throwable t) {
                    if (loadingSnackbar.isShown()) loadingSnackbar.dismiss();
                    showErrorSnackbar();
                }
            });
            loadingSnackbar.show();
        }
    }

    private void showErrorSnackbar() {
        showErrorSnackbar(R.string.tracking_pokemon_error);
    }
    private void showErrorSnackbar(int errorMsgResId) {
        Snackbar.make(mCoordinatorLayout, errorMsgResId, Snackbar.LENGTH_LONG)
                .show();
    }

    private void addSpawnsToMap() {
        if (mMap != null) {

            // remove old markers
            // Note that we do not use myMap.clear() because that incur in the exception
            // "java.lang.IllegalArgumentException: Released unknown bitmap reference"
            try {
                for (PicassoMarker marker : mPicassoMarkers) {
                    marker.getMarker().remove();
                }
            } catch (IllegalArgumentException e) {
                FirebaseCrash.report(e);
            }
            mPicassoMarkers.clear();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (PokemonSpawn spawn : mSpawns) {
                LatLng latLng = new LatLng(spawn.getLatitude(), spawn.getLongitude());
                Marker newMarker = mMap.addMarker(
                        new MarkerOptions().
                                position(latLng).
                                title(spawn.getPokemonName()).
                                snippet(spawn.getTimeLeft())
                );

                PicassoMarker marker = new PicassoMarker(newMarker);
                mPicassoMarkers.add(marker);
                Picasso.with(MapsActivity.this).load(spawn.getPokemonIconUrl())
                        .resize(70, 70).centerInside().into(marker);

                builder.include(latLng);
            }

            if (mSpawns.size() > 0) {
                if (mCurrentLocationMarker != null) {
                    builder.include(mCurrentLocationMarker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int padding = 50; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    updateLastLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
