package in.datacha.classes;

import android.location.Location;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

 class UserLocation {

    private String advertising_id;
    private String publisher_key;
    private List<LocationDetails> locations;
    private String sdk_version;
     private String app_package_name;

    UserLocation() {
        this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;

    }


     public String getApp_package_name() {
         return app_package_name;
     }

     public void setApp_package_name(String app_package_name) {
         this.app_package_name = app_package_name;
     }

     String getAdvertising_id() {
        return advertising_id;
    }

    void setAdvertising_id(String android_id) {
        this.advertising_id = android_id;
    }

     String getPublisher_key() {
        return publisher_key;
    }

    void setPublisher_key(String publisher_key) {
        this.publisher_key = publisher_key;
    }

     List<LocationDetails> getLocations() {
        return locations;
    }

     void setLocations(List<LocationDetails> locations) {
        this.locations = locations;
    }

     static class LocationDetails{
        private Double latitude;
        private Double longitude;
        private float accuracy;
        private long time;
        private float speed;
        private int timezone;

         LocationDetails() {
             this.timezone = TimeZone.getDefault().getRawOffset();
        }

        LocationDetails(Double latitude, Double longitude, float accuracy, float speed, long time) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracy = accuracy;
            this.speed = speed;
            this.time = time;
            this.timezone = TimeZone.getDefault().getRawOffset();
        }

         Double getLatitude() {
            return latitude;
        }

         void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

         Double getLongitude() {
            return longitude;
        }

         void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

         float getAccuracy() {
            return accuracy;
        }

         void setAccuracy(float accuracy) {
            this.accuracy = accuracy;
        }

         long getDate() {
            return time;
        }

         void setDate(long date) {
            this.time = date;
        }

         float getSpeed() {
            return speed;
        }

         void setSpeed(float speed) {
            this.speed = speed;
        }

         void setTimezone(){
            this.timezone = TimeZone.getDefault().getRawOffset();
        }
    }


}
