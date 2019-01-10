package in.datacha.classes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

class LocationManager {
    private static final long TIME_FOREGROUND_SEC = 5 * 60;
    private static final long FOREGROUND_UPDATE_TIME_MS = (TIME_FOREGROUND_SEC - 30) * 1_000;


    /**
     * start location update
     *
     */
    static void startLocation(){
        Context context = DataChain.getInstance().getContext();
        if (!checkPermissions(context)) {
            if(DataChain.getInstance().getAskLocationPermission())
                requestPermissions(context);
        }
        else{
            startLocationUpdates();
        }

    }

    static void startLocationUpdates(){
        //start location updates
        DataChain instance = DataChain.getInstance();
        Utils.Log("Location update started");

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(instance.getContext());

        // remove any updates if running
        mFusedLocationClient.removeLocationUpdates(getPendingIntent(instance.getContext()));
        long interval = SharedPrefOperations.getInt(instance.getContext(), DatachainConstants.DATACHAIN_LOCATION_UPDATE_INTERVAL, DatachainConstants.DATACHAIN_LOCATION_DEFAULT_INTERVAL)*60*1000;

        if(DataChain.foreground){
            interval = FOREGROUND_UPDATE_TIME_MS;
        }
        createLocationRequest(instance.getContext(),interval,LocationRequest.PRIORITY_HIGH_ACCURACY);


    }


    /**
     * Change location update accuracy
     *
     * @param context       context
     * @param accuracy      location accuracy
     */
    static void changeLocationAccuracy(Context context, int accuracy){
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            mFusedLocationClient.removeLocationUpdates(getPendingIntent(context));
            long interval = SharedPrefOperations.getInt(context, DatachainConstants.DATACHAIN_LOCATION_UPDATE_INTERVAL, DatachainConstants.DATACHAIN_LOCATION_DEFAULT_INTERVAL)*60*1000;

            if(DataChain.foreground){
                interval = FOREGROUND_UPDATE_TIME_MS;
            }
            if (checkPermissions(context)) {
                createLocationRequest(context,interval, accuracy);
            }

    }


    @SuppressLint("MissingPermission")
    private static void createLocationRequest(Context context, long update_interval,int accuracy) {

        if(SharedPrefOperations.getBoolean(context,DatachainConstants.DATACHAIN_PREF_DEBUG_MODE,false)){
            update_interval = (int) TimeUnit.MINUTES.toMillis(SharedPrefOperations.getInt(context, DatachainConstants.DATACHAIN_PREF_DEBUG_LOCATION_INTERVAL, DatachainConstants.DATACHAIN_DEBUG_LOCATION_UPDATE_INTERVAL));
        }

        LocationRequest mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(update_interval);

        mLocationRequest.setFastestInterval(update_interval);
        mLocationRequest.setPriority(accuracy);

        mLocationRequest.setMaxWaitTime((long)(update_interval*1.5));

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,getPendingIntent(context));

        if(accuracy==LocationRequest.PRIORITY_HIGH_ACCURACY){
            Utils.Log("Location request accuracy changed to High");
            SharedPrefOperations.putString(context, DatachainConstants.DATACHAIN_PREF_ACCURACY,"HIGH");
        }
        else{
            Utils.Log("Location request accuracy changed to Balanced");
            SharedPrefOperations.putString(context, DatachainConstants.DATACHAIN_PREF_ACCURACY,"BALANCED");
        }

    }



    /**
     * Return the current state of the permissions needed.
     */
    private static boolean checkPermissions(Context context) {
        int permissionState = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    private static void requestPermissions(Context context) {


        int locationFinePermission = ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_FINE_LOCATION");


        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (locationFinePermission != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            startLocationUpdates();
        }
        else {
            if (locationFinePermission != PackageManager.PERMISSION_GRANTED) {
                PermissionsActivity.startPrompt();
            }
            else{
                startLocationUpdates();
            }
        }

    }


    /**
     * Pending intent for location updates
     *
     * @param context context
     * @return pending intent
     */
    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }



}
