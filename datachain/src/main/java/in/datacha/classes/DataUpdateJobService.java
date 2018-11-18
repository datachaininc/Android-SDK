package in.datacha.classes;

import android.content.Context;
import android.os.Build;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.location.LocationRequest;

import in.datacha.BuildConfig;

public class DataUpdateJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Utils.Log("Firebase job invoked");
        if(!SharedPrefOperations.getBoolean(getApplicationContext(),DatachainConstants.DATACHAIN_TRACKING_ENABLED,true)){
            return false;
        }
        Context context = getApplicationContext();
        String key = SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, "");

        boolean restartLocationUpdate = false;
        String locs = SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_USER_LOCATIONS, "");
        if (locs.length() > 0) {

            HttpPostAsyncTask task = new HttpPostAsyncTask(context);
            task.execute(BuildConfig.DATACHAIN_USER_LOCATIONS_API_URL, key, locs, DatachainConstants.DATACHAIN_LOCATION_UPDATE);

        } else
            restartLocationUpdate = true;

        String sessions = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_SESSION_DETAILS, "");
        if(sessions.length()>0){
            HttpPostAsyncTask task = new HttpPostAsyncTask(context);
            task.execute(BuildConfig.DATACHAIN_USER_SESSIONS_API_URL, key, sessions, DatachainConstants.DATACHAIN_SESSION_DETAIL_UPDATE);
        }

        String interests = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_INTERESTS, "");
        if(interests.length()>0){
            HttpPostAsyncTask task = new HttpPostAsyncTask(context);
            task.execute(BuildConfig.DATACHAIN_USER_INTERESTS_API_URL, key, interests, DatachainConstants.DATACHAIN_USER_INTEREST_UPDATE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            restartLocationUpdate = true;
        }
        if (restartLocationUpdate) {
            if (SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_PREF_ACCURACY, "HIGH").equals("HIGH"))
                LocationManager.changeLocationAccuracy(context, LocationRequest.PRIORITY_HIGH_ACCURACY);
            else
                LocationManager.changeLocationAccuracy(context, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }

        Main.sendUserDetails(context,key);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
