package com.myglammmicro.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;
import com.myglammmicro.R;
import com.myglammmicro.adapters.RestaurantsListAdapter;
import com.myglammmicro.interfaces.Apis;
import com.myglammmicro.models.RestaurantRepository;
import com.myglammmicro.models.Restaurants;
import com.myglammmicro.utils.CommonMethods;
import com.myglammmicro.utils.RetrofitClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchHomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG=SearchHomeActivity.class.getSimpleName();
    private TextView currentLocation,searchEdit;
    private RecyclerView restaurantList;
    private RestaurantsListAdapter restaurantsListAdapter;
    private double lat,lon;
    private String locationDetail="";
    private ImageView locationSearch,close;
    GoogleApiClient googleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 101;
    private String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            getIntentData();
            initializeActivityControl();
            fetchRestaurantByUserLocation(lat,lon);
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void getIntentData() {
        try {
            if(getIntent().hasExtra("lat")&getIntent().hasExtra("lon")){
                lat=getIntent().getDoubleExtra("lat",0);
                lon=getIntent().getDoubleExtra("lon",0);
            }
            if(getIntent().hasExtra("locationDetail")){
                locationDetail=getIntent().getStringExtra("locationDetail");
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    private void initializeActivityControl(){


        try{
            currentLocation=(TextView)findViewById(R.id.current_location);
            if(!locationDetail.equalsIgnoreCase("")){
                currentLocation.setText(locationDetail);
            }else {
                currentLocation.setText(getString(R.string.unknown_location));
            }

            currentLocation.setSelected(true);
            CommonMethods.setFontBold(currentLocation);
            searchEdit=(EditText)findViewById(R.id.search_edit);
            CommonMethods.setFontRegular(searchEdit);
            searchEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {


                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if(s.length()>0){
                            showCloseButton();
                            hideSearchLocation();
                            fetchRestaurantBySearchLocation(s.toString());
                        }else {
                            hideCloseButton();
                            showSearchLocation();
                            if(lat!=0&&lon!=0){
                                fetchRestaurantByUserLocation(lat,lon);
                            }else{

                            }
                        }
                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }

                }
            });
            restaurantList=(RecyclerView)findViewById(R.id.restaurant_list);
            locationSearch=(ImageView)findViewById(R.id.location_search);
            locationSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        fetchCurrentLocation();
                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }
            });
            close=(ImageView)findViewById(R.id.cancel);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        searchEdit.setText("");
                        showSearchLocation();
                        hideCloseButton();

                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }
            });
            showSearchLocation();
            hideCloseButton();



        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void initializeRecyclerView(ArrayList<Restaurants>restaurants){
        try{
            if(restaurantsListAdapter==null){
                restaurantsListAdapter = new RestaurantsListAdapter(SearchHomeActivity.this, restaurants);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                restaurantList.setLayoutManager(mLayoutManager);
                restaurantList.setItemAnimator(new DefaultItemAnimator());
                restaurantList.setAdapter(restaurantsListAdapter);
            }else {
                restaurantsListAdapter.updateList(restaurants);
                restaurantsListAdapter.notifyDataSetChanged();
            }


        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void showProgressbar(){
        CircularProgressBar progressBar;
        try{
            progressBar=(CircularProgressBar)findViewById(R.id.loader);
            if(progressBar.getVisibility()!=View.VISIBLE){
                progressBar.setVisibility(View.VISIBLE);
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void hideProgressbar(){
        CircularProgressBar progressBar;
        try{
            progressBar=(CircularProgressBar)findViewById(R.id.loader);
            if(progressBar.getVisibility()==View.VISIBLE){
                progressBar.setVisibility(View.GONE);
            }
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void showRecyclerView(){
        try{
            if(restaurantList!=null&&restaurantList.getVisibility()!=View.VISIBLE){
                restaurantList.setVisibility(View.VISIBLE);
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void hideRecyclerView(){
        try{
            if(restaurantList!=null&&restaurantList.getVisibility()==View.VISIBLE){
                restaurantList.setVisibility(View.GONE);
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void fetchRestaurantByUserLocation(double lat,double lon){
        RestaurantRepository repository = ViewModelProviders.of(this).get(RestaurantRepository.class);
        try{
            if(lat!=0&&lon!=0){
                showRecyclerView();
                showProgressbar();
                repository.initializeData();
                repository.getRestaurantsByLocation("zone","restaurants",30,lat, lon).observe(this, new Observer<List<Restaurants>>() {
                    @Override
                    public void onChanged(List<Restaurants> restaurants) {
                        try{
                            if(restaurants!=null){
                                hideProgressbar();
                                initializeRecyclerView((ArrayList<Restaurants>)restaurants);
                            }
                        }catch (Exception ex){
                            Log.e(TAG,ex.getMessage());
                        }
                    }
                });
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void fetchRestaurantBySearchLocation(String searchText){
        final RestaurantRepository repository = ViewModelProviders.of(this).get(RestaurantRepository.class);
        try{
            showRecyclerView();
            showProgressbar();
            repository.initializeData();
            repository.getRestaurantsByArea("zone",searchText,30).observe(this, new Observer<List<Restaurants>>() {
                @Override
                public void onChanged(List<Restaurants> restaurants) {
                    try{
                        if(restaurants!=null){
                            hideProgressbar();
                            initializeRecyclerView((ArrayList<Restaurants>)restaurants);
                        }

                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }
            });
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }

    private void fetchCurrentLocation(){
        try{
            googleApiClient = getInstance();
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!CommonMethods.hasPermissions(permissions,this)){
                    requestPermissions(permissions, 102);
                }else {
                    if (CommonMethods.isGPSEnabled(getApplicationContext())) {
                        getLastKnownLocation();
                    }else {
                        if(googleApiClient != null){
                            settingsRequest();
                            googleApiClient.connect();
                        }
                    }
                }

            }else {
                if (CommonMethods.isGPSEnabled(getApplicationContext())) {
                    getLastKnownLocation();
                }else {
                    if(googleApiClient != null){
                        settingsRequest();
                        googleApiClient.connect();
                    }
                }
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean permissionGranted = true;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                permissionGranted = false;
                break;
            }
        }
        if (!permissionGranted) {
            Toast.makeText(getApplicationContext(), getString(R.string.permission_not_granted_message), Toast.LENGTH_LONG).show();
            //openSearchHomeScreen(null,"");//show error
        } else {
            if (CommonMethods.isGPSEnabled(getApplicationContext())) {
                getLastKnownLocation();

            }else {
                if(googleApiClient != null){
                    settingsRequest();
                    googleApiClient.connect();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private   GoogleApiClient getInstance(){
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(SearchHomeActivity.this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        return mGoogleApiClient;
    }
    private void getLastKnownLocation(){
        try{
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                getLocationFromLatLng(location);
                                fetchRestaurantByUserLocation(location.getLatitude(),location.getLongitude());
                            }
                        }
                    });
        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    public void settingsRequest()
    {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        Log.e("Application","success");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        // Log.e("Application","Button Clicked1");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Log.e("Application","block");
                            status.startResolutionForResult(SearchHomeActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            Log.e("Applicationsett",e.toString());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //Log.e("Application","Button Clicked2");
                        //Toast.makeText(context, "Location is Enabled", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if (requestCode == REQUEST_CHECK_SETTINGS) {
                if(resultCode==-1){
                    getLastKnownLocation();
                }else  if(resultCode==0){
                    currentLocation.setText(getString(R.string.unknown_location));
                }

            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void getLocationFromLatLng(final Location location){
        try {
            Apis apiService = RetrofitClient.getClient().create(Apis.class);
            Call<String> call = apiService.getLocationDetail(location.getLatitude(),location.getLongitude());
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try{
                        String locationDetail="";
                        if(response.isSuccessful()){
                            JSONObject locationObject=new JSONObject(response.body());
                            if(locationObject.has("location")){
                                locationDetail=locationObject.getJSONObject("location").getString("title")+","+locationObject.getJSONObject("location").getString("city_name");
                            }
                            if(locationDetail.equalsIgnoreCase("")){
                                currentLocation.setText(getString(R.string.unknown_location));
                            }else {
                                currentLocation.setText(locationDetail);
                            }
                        }
                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    try{
                        currentLocation.setText(getString(R.string.unknown_location));

                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }
            });

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void showCloseButton(){
        try{
            if(close!=null&&close.getVisibility()!=View.VISIBLE){
                close.setVisibility(View.VISIBLE);
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void hideCloseButton(){
        try{
            if(close!=null&&close.getVisibility()==View.VISIBLE){
                close.setVisibility(View.GONE);
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void showSearchLocation(){
        try{
            if(locationSearch!=null&&locationSearch.getVisibility()!=View.VISIBLE){
                locationSearch.setVisibility(View.VISIBLE);
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private void hideSearchLocation(){
        try{
            if(locationSearch!=null&&locationSearch.getVisibility()==View.VISIBLE){
                locationSearch.setVisibility(View.GONE);
            }

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
}
