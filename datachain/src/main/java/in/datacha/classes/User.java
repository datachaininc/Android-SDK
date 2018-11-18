package in.datacha.classes;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

 class User {
    private String publisher_key;
    private String advertising_id;
    private String android_id;
    private String device_model;
    private String device_manufacturer;
    private int android_version;
    private String android_version_release;
    private String screen_density;
    private String screen_size;
    private String carrier_name;
    private String app_package_name;
    private String country;
    private String language;
    private String network_type;
    private int session_count;
    private int timezone;
    private long time;
    private List<AppsInfo> apps;
    private String sdk_version;

    User() {
        this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;
    }

    void setAndroid_id(String android_id) {
        this.android_id = android_id;
    }

    void setTimezone() {
        this.timezone = TimeZone.getDefault().getRawOffset();
    }

    void setTime() {
        this.time = new Date().getTime();
    }

    void setCountry(String country) {
        this.country = country;
    }

    void setLanguage(String language) {
        this.language = language;
    }


    void setNetwork_type(String network_type) {
        this.network_type = network_type;
    }

    void setSession_count(int session_count) {
        this.session_count = session_count;
    }


    void setApp_package_name(String app_package_name) {
        this.app_package_name = app_package_name;
    }

     String getPublisher_key() {
        return publisher_key;
    }

    void setPublisher_key(String publisher_key) {
        this.publisher_key = publisher_key;
    }

     String getAndroid_version_release() {
        return android_version_release;
    }

    void setAndroid_version_release(String android_version_release) {
        this.android_version_release = android_version_release;
    }

     String getDevice_model() {
        return device_model;
    }

    void setDevice_model(String device_model) {
        this.device_model = device_model;
    }

     String getDevice_manufacturer() {
        return device_manufacturer;
    }

    void setDevice_manufacturer(String device_manufacturer) {
        this.device_manufacturer = device_manufacturer;
    }

     int getAndroid_version() {
        return android_version;
    }

    void setAndroid_version(int android_version) {
        this.android_version = android_version;
    }

     String getScreen_density() {
        return screen_density;
    }

    void setScreen_density(String screen_density) {
        this.screen_density = screen_density;
    }

     String getScreen_size() {
        return screen_size;
    }

    void setScreen_size(String screen_size) {
        this.screen_size = screen_size;
    }


     String getAdvertising_id() {
        return advertising_id;
    }

    void setAdvertising_id(String publisher_id) {
        this.advertising_id = publisher_id;
    }

     List<AppsInfo> getApps() {
        return apps;
    }

    void setApps(List<AppsInfo> apps) {
        this.apps = apps;
    }

     String getCarrier_name() {
        return carrier_name;
    }

    void setCarrier_name(String carrier_name) {
        this.carrier_name = carrier_name;
    }
}
