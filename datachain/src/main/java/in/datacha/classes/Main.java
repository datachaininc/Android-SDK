package in.datacha.classes;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import in.datacha.BuildConfig;

class Main{

    /**
     * Register the app, by calling publisher server url with app public key
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    static void registerAPP(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataChain dataChain = DataChain.getInstance();
                if(dataChain.getPublisherUrl()!=null){

                    SharedPrefOperations.putString(dataChain.getContext(),DatachainConstants.DATACHAIN_PUBLISHER_SERVER_URL,dataChain.getPublisherUrl());
                    try {
                        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                        keyStore.load(null);

                        KeyPair keyPair = getKeyByAlias(keyStore);
                        if(keyPair==null){
                            Utils.Log("Android Keystore: alias not found. Creating new keypair");
                            keyPair = generateKey();
                            if(keyPair == null) {
                                Utils.Log("Creating new keypair with alias failed");
                                return;
                            }
                            Utils.Log("Android Keystore: new keypair created");
                            getSignedKey(keyPair);
                        }
                        else{
                            if(SharedPrefOperations.getString(dataChain.getContext(), DatachainConstants.DATACHAIN_SIGNED_KEY,"").length()<=0){
                                getSignedKey(keyPair);
                            }
                            else {
                                initialize();
                            }
                        }



                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }
    private static void getSignedKey(final KeyPair keyPair){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(!task.isSuccessful()) {
                    Utils.Log("Instance ID task not complete");
                    return;
                }
                if(task.getResult()!=null) {

                    PublicKey publicKey = keyPair.getPublic();
                    byte[] publicEncoded = publicKey.getEncoded();
                    Utils.Log("Sending app public key signing request");
                    GetSignedPublicKeyTask task1 = new GetSignedPublicKeyTask(DataChain.getInstance().getContext());
                    task1.execute(Base64.encodeToString(publicEncoded, Base64.DEFAULT).trim(), task.getResult().getToken().trim());

                }
                else{
                    Utils.Log("Instance Id not found");

                }
            }
        });

    }
    /**
     * Initializing datachain sdk and start getting data
     *
     */
    private static void initialize(){
        DataChain instance = DataChain.getInstance();
        if(instance!=null) {

            ((Application)instance.getContext().getApplicationContext()).registerActivityLifecycleCallbacks(new ActivityLifecycleListener());

            int count = SharedPrefOperations.getInt(instance.getContext(),DatachainConstants.DATACHAIN_USER_SESSION_COUNT,0);
            SharedPrefOperations.putInt(instance.getContext(), DatachainConstants.DATACHAIN_USER_SESSION_COUNT,count+1);

            SharedPrefOperations.putString(instance.getContext(), BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, instance.getPublisher_key());

            startFirebaseJob(instance.getContext());

            sendUserDetails(instance.getContext(),instance.getPublisher_key());

            if (instance.getEnableLocation()) {
                SharedPrefOperations.putInt(instance.getContext(),DatachainConstants.DATACHAIN_LOCATION_UPDATE_INTERVAL,instance.getLocationUpdateInterval());
                LocationManager.startLocation();
            } else {
                Utils.Log("Location is not enabled");
            }

            checkCachedEmailPhone(instance.getContext());

            Utils.Log("SDK initialization complete");

        }

    }

    /**
     * Get basic user details and send to server
     *
     * @param context Context
     * @param key Publisher key
     */
    static void sendUserDetails(final Context context, final String key){
        new Thread(new Runnable() {
            public void run() {
                try {
                    Date cDate = new Date();
                    String tDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cDate);
                    int currentDate = Integer.parseInt(tDate);
                    int lastDate = SharedPrefOperations.getInt(context, BuildConfig.DATACHAIN_LAST_UPDATE_TIME, 0);
                    if (currentDate > lastDate) {
                        SharedPrefOperations.putString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, key);

                        AdvertisingIdClient.Info adInfo = null;
                        try {
                            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);

                        } catch (IOException e) {
                            e.printStackTrace();

                        } catch (GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        } catch (GooglePlayServicesRepairableException e) {
                            e.printStackTrace();
                        }


                        if (adInfo != null) {
                            if (adInfo.isLimitAdTrackingEnabled()) {
                                Utils.Log("User opted out from targeted advertisement");
                                SharedPrefOperations.putBoolean(context, DatachainConstants.DATACHAIN_TRACKING_ENABLED,false);
                                FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
                                dispatcher.cancelAll();
                                return;
                            }
                            else{
                                SharedPrefOperations.putBoolean(context, DatachainConstants.DATACHAIN_TRACKING_ENABLED,true);
                            }
                            User user = new User();
                            user.setPublisher_key(key);
                            user.setAdvertising_id(adInfo.getId());
                            SharedPrefOperations.putString(context, DatachainConstants.DATACHAIN_USER_ADVERTISING_ID, adInfo.getId());
                            user.setAndroid_version(Build.VERSION.SDK_INT);
                            user.setAndroid_version_release(Build.VERSION.RELEASE);
                            user.setDevice_model(Build.MODEL);
                            user.setDevice_manufacturer(Build.MANUFACTURER);
                            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                            int densityDpi = (int) (metrics.density * 160f);
                            user.setScreen_density(densityDpi + " DPI");
                            user.setScreen_size(metrics.widthPixels + " x " + metrics.heightPixels);
                            TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
                            if (telephonyManager != null) {
                                String simOperatorName = telephonyManager.getSimOperatorName();
                                user.setCarrier_name(simOperatorName);
                                user.setCountry(telephonyManager.getNetworkCountryIso());

                            }
                            user.setApp_package_name(context.getPackageName());

                            user.setLanguage(Locale.getDefault().getDisplayLanguage());
                            final ConnectivityManager connMgr = (ConnectivityManager)
                                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            final android.net.NetworkInfo wifi;
                            if (connMgr != null) {
                                wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                                if (wifi.isConnectedOrConnecting()) {
                                    user.setNetwork_type("Wifi");
                                } else if (mobile.isConnectedOrConnecting()) {
                                    user.setNetwork_type("Mobile Data");
                                }
                            }


                            user.setSession_count(SharedPrefOperations.getInt(context, DatachainConstants.DATACHAIN_USER_SESSION_COUNT, 0));

                            user.setApps(getApps(context));
                            user.setTimezone();
                            user.setTime();

                            Gson gson = new Gson();
                            HttpPostAsyncTask task = new HttpPostAsyncTask(context);
                            task.execute(BuildConfig.DATACHAIN_USER_DETAILS_API_URL, key, gson.toJson(user), DatachainConstants.DATACHAIN_USER_DETAILS_UPDATE);
                            SharedPrefOperations.putInt(context, BuildConfig.DATACHAIN_LAST_UPDATE_TIME, currentDate);

                            checkCachedEmailPhone(context);
                        }


                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }



    /**
     * For getting the list of apps installed
     *  @param context context
     */
    private static List<AppsInfo> getApps(Context context){

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> appsInstalled = pm.getInstalledApplications(0);
        List<AppsInfo> apps = new ArrayList<>();

        int category;
        String category_name;
        for (ApplicationInfo app : appsInstalled) {

            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                category_name="undefined";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    category = app.category;
                    switch (category){
                        case ApplicationInfo.CATEGORY_AUDIO:
                            category_name="audio";
                            break;
                        case ApplicationInfo.CATEGORY_GAME:
                            category_name="game";
                            break;
                        case ApplicationInfo.CATEGORY_IMAGE:
                            category_name="image";
                            break;
                        case ApplicationInfo.CATEGORY_MAPS:
                            category_name="maps";
                            break;
                        case ApplicationInfo.CATEGORY_NEWS:
                            category_name="news";
                            break;
                        case ApplicationInfo.CATEGORY_PRODUCTIVITY:
                            category_name="productivity";
                            break;
                        case ApplicationInfo.CATEGORY_SOCIAL:
                            category_name="social";
                            break;
                        case ApplicationInfo.CATEGORY_UNDEFINED:
                            break;
                        case ApplicationInfo.CATEGORY_VIDEO:
                            category_name="video";
                            break;
                    }
                }

                apps.add(new AppsInfo(app.loadLabel(context.getPackageManager()).toString(),app.packageName,category_name));

            }

        }
        return apps;

    }



    /**
     * Get hashed email of the user from the publisher and send to server
     *
     * @param md5Email   MD5 hashed email
     * @param sha1Email   SHA1 hashed email
     * @param sha256Email   SHA256 hashed email
     */
    static void setUserHashedEmail(String md5Email, String sha1Email, String sha256Email){
        Context context = DataChain.getInstance().getContext();
        if(context==null){
            return;
        }
        if(!SharedPrefOperations.getBoolean(context,DatachainConstants.DATACHAIN_TRACKING_ENABLED,true)){
            return;
        }
        if(isValidMD5(md5Email) && isValidSHA1(sha1Email) && isValidSHA256(sha256Email)){
            Gson gson = new Gson();
            String advertising_id = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_ADVERTISING_ID,"");
            String key = SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF,"");

            if(advertising_id.length()>0 && key.length()>0) {
                UserInfo userInfo = new UserInfo(key, advertising_id);
                userInfo.setMd5Email(md5Email);
                userInfo.setSha1Email(sha1Email);
                userInfo.setSha256Email(sha256Email);
                userInfo.setApp_package_name(context.getPackageName());
                HttpPostAsyncTask task = new HttpPostAsyncTask(context);
                task.execute(BuildConfig.DATACHAIN_USER_EMAIL_API_URL, key, gson.toJson(userInfo), DatachainConstants.DATACHAIN_USER_EMAIL_UPDATE);

                SharedPrefOperations.removeKey(context, DatachainConstants.DATACHAIN_USER_EMAIL_CACHE);
            }
            else{
                UserInfo userInfo = new UserInfo(" "," ");
                userInfo.setMd5Email(md5Email);
                userInfo.setSha1Email(sha1Email);
                userInfo.setSha256Email(sha256Email);
                SharedPrefOperations.putString(context, DatachainConstants.DATACHAIN_USER_EMAIL_CACHE, new Gson().toJson(userInfo));
            }
        }
        else{
            Utils.Log("Invalid email hashing");
        }

    }

    /**
     * Get hashed phone number of the user
     *
     * @param md5Phone   MD5 hashed phone
     * @param sha1Phone  SHA1 hashed phone
     * @param sha256Phone    SHA256 hashed phone
     */
    static void setUserHashedPhoneNumber(String md5Phone, String sha1Phone, String sha256Phone){
        Context context = DataChain.getInstance().getContext();
        if(context==null){
            return;
        }
        if(!SharedPrefOperations.getBoolean(context,DatachainConstants.DATACHAIN_TRACKING_ENABLED,true)){
            return;
        }

        if(isValidMD5(md5Phone) && isValidSHA1(sha1Phone) && isValidSHA256(sha256Phone)){
            Gson gson = new Gson();
            String advertising_id = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_ADVERTISING_ID,"");
            String key = SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF,"");

            if(advertising_id.length()>0 && key.length()>0) {

                UserInfo userInfo = new UserInfo(key, advertising_id);
                userInfo.setMd5Phone_number(md5Phone);
                userInfo.setSha1Phone_number(sha1Phone);
                userInfo.setSha256Phone_number(sha256Phone);
                userInfo.setApp_package_name(context.getPackageName());
                HttpPostAsyncTask task = new HttpPostAsyncTask(context);
                task.execute(BuildConfig.DATACHAIN_USER_EMAIL_API_URL, key, gson.toJson(userInfo), DatachainConstants.DATACHAIN_USER_PHONE_UPDATE);

                SharedPrefOperations.removeKey(context, DatachainConstants.DATACHAIN_USER_PHONE_CACHE);
            }
            else{
                UserInfo userInfo = new UserInfo(" "," ");
                userInfo.setMd5Phone_number(md5Phone);
                userInfo.setSha1Phone_number(sha1Phone);
                userInfo.setSha256Phone_number(sha256Phone);
                SharedPrefOperations.putString(context, DatachainConstants.DATACHAIN_USER_PHONE_CACHE, new Gson().toJson(userInfo));
            }
        }
        else{
            Utils.Log("Invalid phone hashing");
        }
    }



    private static boolean isValidMD5(String s) {
        if(s==null)
            return false;
        return s.matches("[a-fA-F0-9]{32}");
    }
    private static boolean isValidSHA1(String s) {
        if(s==null)
            return false;
        return s.matches("[a-fA-F0-9]{40}");
    }
    private static boolean isValidSHA256(String s) {
        if(s==null)
            return false;
        return s.matches("[a-fA-F0-9]{64}");
    }

    /**
     * Set user interests
     *
     * @param action user action
     * @param tag  tag
     */
    static void setUserInterest(String action, String tag){

        Context context = DataChain.getInstance().getContext();
        if(context==null){
            return;
        }
        if(!SharedPrefOperations.getBoolean(context,DatachainConstants.DATACHAIN_TRACKING_ENABLED,true)){
            return;
        }
        if(action.length()<=0 && tag.length()<=0)
            return;
        try {
            UserInterests userInterests = new UserInterests();
            String intrsts = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_INTERESTS, "");
            List<UserInterests.Interests> interestsList = new ArrayList<>();
            Gson gson = new Gson();
            if (intrsts.length() > 0) {
                userInterests = gson.fromJson(intrsts, UserInterests.class);
                interestsList.addAll(userInterests.getUser_interests());
            }
            interestsList.add(new UserInterests.Interests(action, tag));
            userInterests.setUser_interests(interestsList);
            userInterests.setApp_package_name(context.getPackageName());
            String adId = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_ADVERTISING_ID, "");
            userInterests.setAdvertising_id(adId);
            userInterests.setPublisher_key(SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, ""));

            SharedPrefOperations.putString(context, DatachainConstants.DATACHAIN_USER_INTERESTS, gson.toJson(userInterests));
            Utils.Log("Received User Interests");
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    static void setUserInterests(String action, String[] tags){

        Context context = DataChain.getInstance().getContext();
        if(context==null){
            return;
        }
        if(!SharedPrefOperations.getBoolean(context,DatachainConstants.DATACHAIN_TRACKING_ENABLED,true)){
            return;
        }
        if(action.length()<=0 && tags.length<=0)
            return;

        try {
            UserInterests userInterests = new UserInterests();
            String intrsts = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_INTERESTS, "");
            List<UserInterests.Interests> interestsList = new ArrayList<>();
            Gson gson = new Gson();
            if (intrsts.length() > 0) {
                userInterests = gson.fromJson(intrsts, UserInterests.class);
                interestsList.addAll(userInterests.getUser_interests());
            }
            for (String tag : tags) {
                interestsList.add(new UserInterests.Interests(action, tag));
            }

            userInterests.setUser_interests(interestsList);
            userInterests.setApp_package_name(context.getPackageName());
            String adId = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_ADVERTISING_ID, "");
            userInterests.setAdvertising_id(adId);
            userInterests.setPublisher_key(SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, ""));

            SharedPrefOperations.putString(context, DatachainConstants.DATACHAIN_USER_INTERESTS, gson.toJson(userInterests));
            Utils.Log("Received User Interests");
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    /**
     * Start recurring firebase job for one hour
     *
     */

    private static void startFirebaseJob(Context context){
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        int interval = DatachainConstants.DATACHAIN_SERVER_UPDATE_INTERVAL;
        if(SharedPrefOperations.getBoolean(context,DatachainConstants.DATACHAIN_PREF_DEBUG_MODE,false)){
            if(DataChain.getInstance()!=null)
                interval = DataChain.getInstance().getDebugServerUpdateInterval();
        }
        int windowStart = (int) TimeUnit.MINUTES.toSeconds(interval);
        Job myJob = dispatcher.newJobBuilder()
                .setService(DataUpdateJobService.class)
                .setTag("location-update-tag")
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(windowStart, windowStart+(int)TimeUnit.MINUTES.toSeconds(5)))
                .setReplaceCurrent(false)
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .build();

        dispatcher.schedule(myJob);
        Utils.Log("Firebase job started with "+String.valueOf(interval)+" minutes interval");


    }

    /**
     * Get android keystore keypair using alias
     *
     * @param keyStore keystore object
     * @return  keypair
     */
    @Nullable
    static KeyPair getKeyByAlias(KeyStore keyStore) {
        if(keyStore == null)
            return null;

        try {
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(DatachainConstants.DATACHAIN_KEYSTORE_ALIAS, null);
            Certificate certificate = keyStore.getCertificate(DatachainConstants.DATACHAIN_KEYSTORE_ALIAS);
            PublicKey publicKey = null;
            if(certificate != null)
                publicKey = certificate.getPublicKey();

            if(privateKey != null && publicKey != null)
                return new KeyPair(publicKey, privateKey);
        } catch (KeyStoreException e) {
            Utils.Log(e.getMessage());
        } catch (UnrecoverableKeyException e) {
            Utils.Log(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Utils.Log(e.getMessage());
        }

        return null;
    }

    /**
     * Generate android keystore key pair
     *
     * @return keypair
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Nullable
    static private KeyPair generateKey() {
        try {
            KeyPairGenerator keyPairGenerator
                    = KeyPairGenerator.getInstance("EC", "AndroidKeyStore");

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(DatachainConstants.DATACHAIN_KEYSTORE_ALIAS
                        , KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .build()
                );
            } else {
                Calendar startDate = Calendar.getInstance();
                Calendar endDate = Calendar.getInstance();

                endDate.add(Calendar.YEAR, 20);

                KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(DataChain.getInstance().getContext());
                builder.setAlias(DatachainConstants.DATACHAIN_KEYSTORE_ALIAS);
                builder.setSerialNumber(BigInteger.ONE);
                builder.setStartDate(startDate.getTime());
                builder.setEndDate(endDate.getTime());

                keyPairGenerator.initialize(builder.build());
            }

            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            Utils.Log(e.getMessage());
        } catch (NoSuchProviderException e) {
            Utils.Log(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            Utils.Log(e.getMessage());
        } catch (ProviderException e){
            Utils.Log(e.getMessage());
        } catch (IllegalArgumentException e){
            Utils.Log(e.getMessage());
        }

        return null;
    }

    /**
     * Send in app purchase info to server
     *
     * @param infos Purchase info list
     */
    static void setUserPurchase(List<InAppPurchase.PurchaseInfo> infos){
        Context context = DataChain.getInstance().getContext();
        if(context==null){
            return;
        }
        String adId = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_ADVERTISING_ID, "");
        String pub_key = SharedPrefOperations.getString(context, BuildConfig.DATACHAIN_PUBLISHER_KEY_PREF, "");

        InAppPurchase inAppPurchase = new InAppPurchase(adId,pub_key,infos);
        inAppPurchase.setApp_package_name(context.getPackageName());
        HttpPostAsyncTask task = new HttpPostAsyncTask(context);
        task.execute(BuildConfig.DATACHAIN_INAPP_PURCHASE_API_URL, pub_key, new Gson().toJson(inAppPurchase), DatachainConstants.DATACHAIN_INAPP_PURCHASE_UPDATE);

    }

    static void setUserPurchase(String iso, String amount){
        List<InAppPurchase.PurchaseInfo> infos = new ArrayList<>();
        infos.add(new InAppPurchase.PurchaseInfo(iso, amount));

        setUserPurchase(infos);
    }

    /**
     * Checking if there's any cached email/phone to update
     * @param context context
     */
    private static void checkCachedEmailPhone(Context context){
        String cacheEmail = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_EMAIL_CACHE, "");
        String cachePhone = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_USER_PHONE_CACHE, "");

        if(cacheEmail.length()>0){
            UserInfo info = new Gson().fromJson(cacheEmail, UserInfo.class);
            setUserHashedEmail(info.getMd5Email(), info.getSha1Email(), info.getSha256Email());
        }
        if(cachePhone.length()>0){
            UserInfo info = new Gson().fromJson(cachePhone, UserInfo.class);
            setUserHashedPhoneNumber(info.getMd5Phone_number(), info.getSha1Phone_number(), info.getSha256Phone_number());
        }
    }


    static void continueInitialize() {
        initialize();
    }
}
