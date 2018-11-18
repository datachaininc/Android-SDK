package in.datacha.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import in.datacha.BuildConfig;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    static final String ACTION_PROCESS_UPDATES =
            "in.datacha.classes.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null){
            return;
        }
        final String action = intent.getAction();
        if(!ACTION_PROCESS_UPDATES.equals(action)){
            return;
        }
        LocationResult result = LocationResult.extractResult(intent);
        if(result==null){
            return;
        }
        List<Location> locations = result.getLocations();
        if(locations.size()<=0){
            return;
        }
        UserLocation userLocation = new UserLocation();
        List<UserLocation.LocationDetails> locationDetails = new ArrayList<>();
        String locs = SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_USER_LOCATIONS, "");
        Gson gson = new Gson();
        if (locs.length() > 0) {
            userLocation = gson.fromJson(locs, UserLocation.class);
            locationDetails.addAll(userLocation.getLocations());
        }
        Location location = locations.get(0);
        Utils.Log("Location Received " + location.getLatitude() + " " + location.getLongitude() + " " + location.getAccuracy());
        locationDetails.add(new UserLocation.LocationDetails(location.getLatitude(), location.getLongitude(), location.getAccuracy(),location.getSpeed(), location.getTime()));

        userLocation.setLocations(locationDetails);
        userLocation.setApp_package_name(context.getPackageName());
        String adId = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_ADVERTISING_ID, "");
        if(adId.length()<=0)
            return;
        userLocation.setAdvertising_id(adId);
        userLocation.setPublisher_key(SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, ""));

        SharedPrefOperations.putString(context, BuildConfig.DATACHAIN_USER_LOCATIONS, gson.toJson(userLocation));

        if(SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_PREF_ACCURACY,"BALANCED").equals("HIGH")){
            int count = SharedPrefOperations.getInt(context,DatachainConstants.DATACHAIN_PREF_COUNT,0);
            if(location.getAccuracy()<201){
                count+=1;
                if(count>=2) {
                    SharedPrefOperations.putInt(context, DatachainConstants.DATACHAIN_PREF_COUNT, 0);
                    LocationManager.changeLocationAccuracy(context, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                }
                else{
                    SharedPrefOperations.putInt(context,DatachainConstants.DATACHAIN_PREF_COUNT,count);
                }
            }
            else{
                SharedPrefOperations.putInt(context,DatachainConstants.DATACHAIN_PREF_COUNT,0);
            }
        }else{
            int count = SharedPrefOperations.getInt(context,DatachainConstants.DATACHAIN_PREF_COUNT,0);
            if(location.getAccuracy()>200){
                count+=1;
                if(count>=2){
                    SharedPrefOperations.putInt(context, DatachainConstants.DATACHAIN_PREF_COUNT, 0);
                    LocationManager.changeLocationAccuracy(context, LocationRequest.PRIORITY_HIGH_ACCURACY);
                }
                else{
                    SharedPrefOperations.putInt(context,DatachainConstants.DATACHAIN_PREF_COUNT,count);
                }
            }else{
                SharedPrefOperations.putInt(context,DatachainConstants.DATACHAIN_PREF_COUNT,0);
            }
        }


    }


}