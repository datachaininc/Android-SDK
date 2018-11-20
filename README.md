# Datachain Android SDK



This document contains detail information regarding the Datachain SDK including required permissions, list of data points collected, steps to integrate the SDK

#### Datachain SDK Workflow

**Publisher Registration**

App developer have to register with Datachain using their Publisher public key. This is a one time operation to get a new develop onboard with the Datachain system
![alt text](https://raw.githubusercontent.com/datachaininc/Android-SDK/master/Images/image4.jpg)
 

**Application Registration**

The Datachain SDK has to register itself for the first time. Below is the workflow for such a registration
![alt text](https://raw.githubusercontent.com/datachaininc/Android-SDK/master/Images/image6.jpg)
 
**Data Publication**

The SDK uses the below workflow to publish data to the Datachain server

 ![alt text](https://raw.githubusercontent.com/datachaininc/Android-SDK/master/Images/image1.jpg)


### Permissions required

List of permissions required by the SDK

1. android.permission.ACCESS\_NETWORK\_STATE
2. android.permission.RECEIVE\_BOOT\_COMPLETED
3. android.permission.ACCESS\_FINE\_LOCATION
4. android.permission.ACCESS\_COARSE\_LOCATION

### Datapoints collected

The list of data points collected by the Datachain SDK

|   | Frequency |
| --- | --- |
| Advertising ID | Every 24 hours |
| Device Model | Every 24 hours |
| Device Manufacturer | Every 24 hours |
| Android Version | Every 24 hours |
| Android Version Release | Every 24 hours |
| Screen Density | Every 24 hours |
| Screen Size | Every 24 hours |
| Career Name | Every 24 hours |
| App Package Name | Every 24 hours |
| Country | Every 24 hours |
| Language | Every 24 hours |
| Network Type | Every 24 hours |
| Session Count | Based on usage |
| Timezone | Based on usage |
| Apps Installed | Every 24 hours |
| Device Time | Based on usage |
| Hashed Email | One time |
| Hashed Phone Number | One time |
| User Interests | Based on usage |
| Session Duration | Based on usage |
| In-app purchases | Based on usage |
| Location | Based on usage (once every 10 mins) |































#### Compliance with Google privacy policy

Datachain SDK takes all precautions to be compliant with Google privacy policy. Shared below is specific paragraph from Google privacy policy (from Google developer console) that concerns data collection and data sharing from Apps.

**Google privacy policy**

Your app must provide an in-app disclosure of your data collection and use. The in-app disclosure:

- Must be within the app itself, not only in the Play listing or a website;
- Must be displayed in the normal usage of the app and not require the user to navigate into a menu or settings;
- Must describe the data being collected;
- Must explain how the data will be used;
- Cannot only be placed in a privacy policy or terms of service; and
- Cannot be included with other disclosures unrelated to personal or sensitive data collection.

Your app&#39;s in-app disclosure must include a request for user consent. The app&#39;s request for consent:

- Must present the consent dialog in a clear and unambiguous way;
- Must require affirmative user action (e.g. tap to accept, tick a check-box, a verbal command, etc.) in order to accept;
- Must not begin personal or sensitive data collection prior to obtaining affirmative consent;
- Must not consider navigation away from the disclosure (including tapping away or pressing the back or home button) as consent; and
- Must not utilize auto-dismissing or expiring messages.

Note:[Google privacy policy](https://play.google.com/about/privacy-security-deception/personal-sensitive/)





To be compliant with Google privacy policy, Datachain recommends that host APP seek consent from users. Below are example screens on how such a consent can be sought. It is recommended that the host app initialize the SDK only after the consent is taken from the user

Samples for asking user consent

 ![alt text](https://raw.githubusercontent.com/datachaininc/Android-SDK/master/Images/image2.jpg)
![alt text](https://raw.githubusercontent.com/datachaininc/Android-SDK/master/Images/image5.jpg)
![alt text](https://raw.githubusercontent.com/datachaininc/Android-SDK/master/Images/image3.jpg)


**How this SDK works?**

1. SDK initialization

        DataChain.getInstance()
            .publisherKey("YOUR_PUBLISHER_KEY")
            .enableLocation(true)                // enable/disable location
            .locationUpdateInterval(10)        // update interval in minutes(>=10 and <50)
            .serverUrl("YOUR_SERVER\URL")
            .askLocationPermission(true)
            .init(this);                        // context               

    During initialization, the SDK

    1. Collects all the basic user details like advertising id, device model, android version etc
    2. Starts location update
    3. Starts Firebase job for every one hour to update location, user interests, and session duration to Datachain server

    Note: Firebase job dispatcher doesn&#39;t run during [doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby#understand_doze)

1. Pass user data to the SDK

    1. User **email/phone**
        
            DataChain.setUserEmail(md5Hash(email), sha1Hash(email), sha256Hash(email));
            DataChain.setUserPhoneNumber(md5Hash(phone), sha1Hash(phone), sha256Hash(phone));

         Datachain does not collect plaintext user identity. We deal with only hashed version of the Email or Phone Number. It is recommended that host app use hashing functions (shared below) and pass the hashed version of user email or phone number as above

    1. User **purchase info**

        When a user completes an inapp purchase, invoke following function

            DataChain.setUserPurchase(CURRENCY_TYPE, AMOUNT);

    1. User **interests**

        When a user starts a new screen, pass the user interest corresponding to the content in the screen

            DataChain.setUserInterest(ACTION, TAG);

        Eg: For a recipe book app, when a user loads a recipe for an Indian food, you can pass interests like,

            DataChain.setUserInterest("Read","Chicken Recipe");



#### Android studio Implementation

Prerequisites

- Target Android API 18 or higher
- Use Android Studio 1.0 or higher

Steps

1. Download DataChain SDK and import it into your project

    Refer [this](https://developer.android.com/studio/projects/add-app-module#ImportAModule) to know how to import a module in Android Studio

1. Integrate Google Play Services and Gson to your project,

    _implementation &#39;com.google.android.gms:play-services-ads:16.0.0&#39;_
_implementation &#39;com.google.android.gms:play-services-location:16.0.0&#39;_
_implementation &#39;com.google.code.gson:gson:2.8.5&#39;_
_implementation &#39;com.firebase:firebase-jobdispatcher:0.8.5&#39;_

1. Create a **firebase project** (if you don&#39;t have it already) and integrate it in your app, then add firebase messaging in your gradle (to get instance id)

    _implementation &#39;com.google.firebase:firebase-messaging:17.3.4&#39;_

1. If you prefer to share user location with DataChain,
    Add following location permission in manifest

        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION">
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION">
    Request location permission to users.
    Note: DataChain will request location permission if it&#39;s not granted

1. **Initialize DataChain** SDK in onCreate() of your main activity.

    Basic
    
        DataChain.getInstance()
            .publisher_key("YOUR_PUBLISHER_KEY")
            .init(this);

    Advanced

        DataChain.getInstance()
            .publisherKey("YOUR_PUBLISHER_KEY")
            .enableLocation(true)                // enable/disable location
            .locationUpdateInterval(10)        // update interval in minutes(>=10 and <50)
            .serverUrl("YOUR_SERVER_URL")
            .askLocationPermission(true)
            .init(this);                        // context        



1. If you have **user email** or **phone number** , update it using,

        DataChain.setUserEmail(md5Hash(email), sha1Hash(email), sha256Hash(email));
        DataChain.setUserPhoneNumber(md5Hash(phone), sha1Hash(phone), sha256Hash(phone));

    Code for hashing string is given at the bottom of this doc.

1. Set **user-interests**

    When a user starts a new screen or page in the app, call

        DataChain.setUserInterest(action, tag);

1. Pass **user-purchase-info** ,

        DataChain.setUserPurchase(CURRENCY_TYPE,AMOUNT);

1. If you are using **proguard** (For code obfuscation),

    Add these into your proguard-rules.txt

        -dontwarn in.datacha.\*\*
        -keep class in.datacha.\*\* { \*; }









#### Unity Integration

1. Create &quot; **Plugins/Android**&quot; folders under **Assets** , and copy Datachain aar file in it.
2. If you already have setup firebase messaging, go to step 3,
else, create a firebase project, and add firebase instance id to your project follow [this link](https://firebase.google.com/docs/cloud-messaging/unity/client) to setup firebase

1. Datachain sdk needs some dependencies, which are listed below,

    _&#39;com.google.android.gms:play-services-ads:16.0.0&#39;_
    _&#39;com.google.android.gms:play-services-location:16.0.0&#39;_
    _&#39;com.google.code.gson:gson:2.8.5&#39;_
    _&#39;com.firebase:firebase-jobdispatcher:0.8.5&#39;_

    You can include these packages using gradle if you have the gradle project,
    OR
    Include these aar/jar packages of these libraries inside &quot; **Assets/Plugins/Android**&quot;

      _collections-28.0.0.jar_
    _firebase-jobdispatcher-0.8.5.aar_
    _gson-2.8.5.jar_
    _play-services-ads-16.0.0.aar_
    _play-services-ads-identifier-16.0.0.aar_
    _play-services-base-16.0.1.aar_
    _play-services-basement-16.0.1.aar_
    _play-services-location-16.0.0.aar_
    _play-services-tasks-16.0.1.aar_
    _support-compat-28.0.0.aar_
    _support-v4-28.0.0.aar_

    You can find these files in "_Android SDK aar files_" folder

    Some of these files will be added during firebase setup, if so please omit those packages

1. If you prefer to share user location with DataChain,
Add following location permission in manifest

        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION">
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION">


1. Now, in your main c# file, add these lines to initialize Datachain SDK, in **Start()** method

        AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
        AndroidJavaObject datachain = new AndroidJavaClass("in.datacha.classes.DataChain");
        AndroidJavaObject instance = datachain.CallStatic<AndroidJavaObject>("getInstance");
        instance = instance.Call<AndroidJavaObject>("publisherKey","YOUR_KEY");
        instance = instance.Call<AndroidJavaObject>("serverUrl","YOUR_SERVER_URL");
        instance = instance.Call<AndroidJavaObject>("enableLocation",true);
        instance = instance.Call<AndroidJavaObject>("askLocationPermission", true);
        instance = instance.Call<AndroidJavaObject>("locationUpdateInterval", new object[]{10}); // time in minutes between 10 and 50
        instance.Call("init",activity );



1. After SDK initialization, you can pass other user info&#39;s to the SDK
    1. Pass md5, sha1, and sha256 hash of **user-email**

            datachain.CallStatic("setUserEmail",md5Hash(email),sha1Hash(email),sha256Hash(email));
    1. Pass md5, sha1, sha256 hash of user **phone number**

            datachain.CallStatic("setUserPhoneNumber",md5Hash(phone),sha1Hash(phone),sha256Hash(phone));

        Code for hashing string is given at the bottom of this doc.

    1. Pass **user-interests** ,

            datachain.CallStatic("setUserInterests",new object[] {new string[] {"INTEREST_1", "INTEREST_2", "INTEREST_3" }});

    1. Pass **user-purchase-info** ,

            datachain.CallStatic("setUserPurchase", CURRENCY_TYPE,AMOUNT);



1. Call the below function, when the user quit the game

        datachain.CallStatic("applicationStop");





#### Sample codes

Hashing in **Java** for **android**

**// md5**

        private static String md5Hash(String value){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(&quot;MD5&quot;);
            byte[] array = messageDigest.digest(value.getBytes(&quot;UTF-8&quot;));
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray &amp; 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

**// sha1**

    public static String sha1Hash(String value)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance(&quot;SHA-1&quot;);
            messageDigest.update(value.getBytes(&quot;UTF-8&quot;));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes)
            {
                buffer.append(Integer.toString((b &amp; 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

**// sha256**

    public static String sha256Hash(String value)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance(&quot;SHA-256&quot;);
            messageDigest.update(value.getBytes(&quot;UTF-8&quot;));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes)
            {
                buffer.append(Integer.toString((b &amp; 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }



**Hashing in **c#** for **unity****

**// md5**

    private string md5Hash(string value){
        HashAlgorithm algorithm = new MD5CryptoServiceProvider();
        byte[] message =  ASCIIEncoding.ASCII.GetBytes(value);
        byte[] hashValue = algorithm.ComputeHash(message);
        return hashValue.Aggregate(string.Empty, (current, x) =\&gt; current + string.Format(&quot;{0:x2}&quot;, x));
    }

**// sha1**

    private string sha1Hash(string value){
         HashAlgorithm algorithm = new SHA1Managed();
        byte[] message =  ASCIIEncoding.ASCII.GetBytes(value);
        byte[] hashValue = algorithm.ComputeHash(message);
        return hashValue.Aggregate(string.Empty, (current, x) =\&gt; current + string.Format(&quot;{0:x2}&quot;, x));
    }

**// sha256**

    private string sha256Hash(string value){
        HashAlgorithm algorithm = new SHA256Managed();
        byte[] message =  ASCIIEncoding.ASCII.GetBytes(value);
        byte[] hashValue = algorithm.ComputeHash(message);
        return hashValue.Aggregate(string.Empty, (current, x) =\&gt; current + string.Format(&quot;{0:x2}&quot;, x));
    }
