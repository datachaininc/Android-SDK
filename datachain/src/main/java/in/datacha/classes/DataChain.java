package in.datacha.classes;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import in.datacha.BuildConfig;

public class DataChain {

    static boolean initialized = false;
    private static DataChain datachainInstance = null;
    private Context context;
    private Boolean enableLocation = true;
    private String publisher_key;
    private int locationUpdateInterval = DatachainConstants.DATACHAIN_LOCATION_DEFAULT_INTERVAL;
    private static TrackGooglePurchase trackGooglePurchase;
    static boolean foreground;
    private String publisherUrl;
    private Boolean askLocationPermission=true;


    public static DataChain getInstance(){
        if(datachainInstance==null){
            datachainInstance= new DataChain();

        }
        return datachainInstance;
    }

    private DataChain(){

    }


    /**
     * Initialize Datachain
     *
     * @param context context
     */
    public void init(Context context){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN_MR2){
            return;
        }
        if(initialized) {
            Utils.Log("SDK already initialized");
            return;
        }
        initialized = true;
        if (datachainInstance.getPublisher_key() == null) {
            throw new IllegalArgumentException("Publisher key not defined");
        }
        if(enableLocation){
            if(locationUpdateInterval<DatachainConstants.DATACHAIN_LOCATION_MIN_INTERVAL || locationUpdateInterval>DatachainConstants.DATACHAIN_LOCATION_MAX_INTERVAL){
                throw new IllegalArgumentException("Location update interval should be between "+DatachainConstants.DATACHAIN_LOCATION_MIN_INTERVAL+" and "+DatachainConstants.DATACHAIN_LOCATION_MAX_INTERVAL+" minutes");
            }
        }
        if(context==null){
            throw new NullPointerException("Context is null");
        }
        if(publisherUrl==null || publisherUrl.length()<=0){
            throw new IllegalArgumentException(" Need server url");
        }

        ActivityLifecycleHandler.sessionStartTimestamp=new Date().getTime();
        datachainInstance.context = context;
        Main.registerAPP();

        foreground = (context instanceof Activity);
        if (foreground) {
            ActivityLifecycleHandler.curActivity = (Activity) context;
        }
        else
            ActivityLifecycleHandler.nextResumeIsFirstActivity = true;

        if (TrackGooglePurchase.CanTrack(context)) {
            trackGooglePurchase = new TrackGooglePurchase(context);
            Utils.Log("In app purchase tracking possible");
        }
        else{
            Utils.Log("In app purchase tracking is not possible");
        }


    }

    /**
     * Called when app is in foreground
     * called from activity life cycle handler
     */
    static void onAppFocus() {
        foreground = true;
        if (trackGooglePurchase != null) {
            trackGooglePurchase.trackIAP();
        }

    }

    /**
     * Called when app goes background
     * called from activity life cycle handler
     */
    static void onAppLostFocus() {
        foreground = false;
        if(getInstance().context==null){
            return;
        }

        List<SessionDuration.SessionDetails> sessionDetails = new ArrayList<>();
        SessionDuration sessionDuration = new SessionDuration();
        String sessions = SharedPrefOperations.getString(getInstance().context, DatachainConstants.DATACHAIN_SESSION_DETAILS, "");
        Gson gson = new Gson();
        if (sessions.length() > 0) {
            sessionDuration = gson.fromJson(sessions, SessionDuration.class);
            sessionDetails.addAll(sessionDuration.getSessions());
        }
        sessionDetails.add(new SessionDuration.SessionDetails(ActivityLifecycleHandler.sessionStartTimestamp,new Date().getTime()));

        sessionDuration.setSessions(sessionDetails);
        String adId = SharedPrefOperations.getString(getInstance().context, DatachainConstants.DATACHAIN_USER_ADVERTISING_ID, "");
        sessionDuration.setAdvertising_id(adId);
        sessionDuration.setApp_package_name(getInstance().getContext().getPackageName());
        sessionDuration.setPublisher_key(SharedPrefOperations.getString(getInstance().context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, ""));

        SharedPrefOperations.putString(getInstance().context, DatachainConstants.DATACHAIN_SESSION_DETAILS, gson.toJson(sessionDuration));
        ActivityLifecycleHandler.sessionStartTimestamp=0;

        Utils.Log("App went background");

        LocationManager.changeLocationAccuracy(getInstance().context,LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);



    }
    String getPublisher_key() {
        return this.publisher_key;
    }
    String getPublisherUrl() {
        return this.publisherUrl;
    }

    public Context getContext() {
        return this.context;
    }


    Boolean getEnableLocation() {
        return this.enableLocation;
    }

    Boolean getAskLocationPermission() {
        return this.askLocationPermission;
    }


    int getLocationUpdateInterval(){
        return this.locationUpdateInterval;
    }

    public DataChain enableLocation(boolean enableLocation) {
        this.enableLocation = enableLocation;
        return this;
    }


    public DataChain publisherKey(String publisher_key) {
        this.publisher_key = publisher_key;
        return this;
    }
    public DataChain serverUrl(String url) {
        this.publisherUrl = url;
        return this;
    }
    public DataChain askLocationPermission(boolean value) {
        this.askLocationPermission = value;
        return this;
    }

    public DataChain locationUpdateInterval(int interval){
        this.locationUpdateInterval = interval;
        return this;
    }


    /**
     * Get hashed email of the user
     *
     * @param md5Email      md5 hashed email
     * @param sha1Email     sha1 hashed email
     * @param sha256Email sha256 hashed email
     */
    public static void setUserEmail(String md5Email, String sha1Email, String sha256Email){
        if(initialized){
            Main.setUserHashedEmail(md5Email, sha1Email, sha256Email);
        }
        else{
            throw new IllegalStateException("Datachain is not initialized");
        }
    }


    /**
     * Get hashed user phone number
     *
     * @param md5Phone  md5 hashed phone number
     * @param sha1Phone     sha1 hashed phone number
     * @param sha256Phone   sha256 hashed phone number
     */
    public static void setUserPhoneNumber(String md5Phone, String sha1Phone, String sha256Phone){
        if(initialized){
            Main.setUserHashedPhoneNumber(md5Phone, sha1Phone, sha256Phone);
        }
        else{
            throw new IllegalStateException("Datachain is not initialized");
        }
    }

    /**
     * Get user interests
     * @param action  user action (eg: like, share etc..)
     * @param tag tag
     */
    public static void setUserInterest(String action, String tag){
        if(action.length()<=0 && tag.length()<=0)
            return;
        if(initialized){
            Main.setUserInterest(action, tag);
        }
    }


    /**
     * Called when application is stopped
     *
     */
    public static void applicationStop(){
        onAppLostFocus();
    }

    /**
     * Get user purchase details
     *
     * @param iso   Currency type
     * @param amount    Purchase amount
     */
    public static void setUserPurchase(String iso, String amount){
        if(iso!=null && amount!=null && iso.length()>0 && amount.length()>0){
            Main.setUserPurchase(iso,amount);
        }
        else{
            Utils.Log("Invalid iso and amount");
        }
    }

}
