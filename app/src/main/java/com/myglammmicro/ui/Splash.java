package com.myglammmicro.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.myglammmicro.interfaces.Apis;
import com.myglammmicro.utils.CommonMethods;
import com.myglammmicro.utils.RetrofitClient;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Splash extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static  final String TAG=Splash.class.getSimpleName();
    GoogleApiClient googleApiClient;
    private static final int REQUEST_CHECK_SETTINGS = 101;
    private static final int SEARCH_INTENT = 110;
    private String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private FusedLocationProviderClient fusedLocationClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        try{
            if(!CommonMethods.isNetworkAvailable(Splash.this)){
                showCustomDialog(getString(R.string.no_internet),true);
                return;
            }
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
    private void getLastKnownLocation(){
        try{
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {

                               //openSearchHomeScreen(lastKnownLocation);
                                getLocationFromLatLng(location);
                            }
                        }
                    });
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
            openSearchHomeScreen(null,"");
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
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(Splash.this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        return mGoogleApiClient;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if (requestCode == REQUEST_CHECK_SETTINGS) {
                if(resultCode==-1){
                    getLastKnownLocation();
                }else  if(resultCode==0){
                    openSearchHomeScreen(null,"");
                }

            }

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
                            status.startResolutionForResult(Splash.this, REQUEST_CHECK_SETTINGS);
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
    private void openSearchHomeScreen(Location location,String locationDetail){
        try{
            Intent intent = new Intent(this, SearchHomeActivity.class);

            if(location!=null){
                intent.putExtra("lat",location.getLatitude());
                intent.putExtra("lon",location.getLongitude());
            }else {
                intent.putExtra("lat",0);
                intent.putExtra("lon",0);
            }
            intent.putExtra("locationDetail",locationDetail);
            overridePendingTransition(R.anim.open_next, R.anim.close_main);
            startActivityForResult(intent, SEARCH_INTENT);
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
                            openSearchHomeScreen(location,locationDetail);
                        }
                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    try{
                        openSearchHomeScreen(location,"");

                    }catch (Exception ex){
                        Log.e(TAG,ex.getMessage());
                    }
                }
            });

        }catch (Exception ex){
            Log.e(TAG,ex.getMessage());
        }
    }
    private AlertDialog customDialog;

    private void showCustomDialog(String message, final boolean closeApp) {
        final TextView title, confirmation;

        try {
            if (customDialog != null && customDialog.isShowing()) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.general_message_dialog, null);
            builder.setView(dialogView);
            title = (TextView) dialogView.findViewById(R.id.message);
            title.setText(message);
            CommonMethods.setFontMedium(title);
            confirmation = (TextView) dialogView.findViewById(R.id.confirm);
            confirmation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (customDialog != null) {
                        customDialog.dismiss();
                    }
                    if(closeApp){
                        finishAffinity();
                    }
                }
            });
            CommonMethods.setFontRegular(confirmation);
            customDialog = builder.create();
            customDialog.setCanceledOnTouchOutside(false);
            customDialog.show();
            customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    customDialog = null;
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }


}

