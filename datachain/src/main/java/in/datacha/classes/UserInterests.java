package in.datacha.classes;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

 class UserInterests {
    private String publisher_key;
    private String advertising_id;
    private List<Interests> user_interests;
    private String sdk_version;
     private String app_package_name;

    UserInterests() {
        this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;

    }

     UserInterests(String publisher_key, String advertising_id, List<Interests> user_interests) {
        this.publisher_key = publisher_key;
        this.advertising_id = advertising_id;
        this.user_interests = user_interests;
         this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;

     }

     public String getApp_package_name() {
         return app_package_name;
     }

     public void setApp_package_name(String app_package_name) {
         this.app_package_name = app_package_name;
     }

     String getPublisher_key() {
        return publisher_key;
    }

    void setPublisher_key(String publisher_key) {
        this.publisher_key = publisher_key;
    }

     String getAdvertising_id() {
        return advertising_id;
    }

    void setAdvertising_id(String advertising_id) {
        this.advertising_id = advertising_id;
    }

    List<Interests> getUser_interests() {
        return user_interests;
    }

    void setUser_interests(List<Interests> user_interests) {
        this.user_interests = user_interests;
    }

     static class Interests{
        private long time;
        private int timezone;
        private String action;
        private String tag;

         Interests() {
             this.time = new Date().getTime();
             this.timezone = TimeZone.getDefault().getRawOffset();
        }

        Interests(String action, String tag) {
            this.time = new Date().getTime();
            this.timezone = TimeZone.getDefault().getRawOffset();
            this.action = action;
            this.tag=tag;
        }

         long getTimestamp() {
            return time;
        }

         void setTimestamp(long timestamp) {
            this.time = timestamp;
        }

         int getTimezone() {
            return timezone;
        }

         void setTimezone(int timezone) {
            this.timezone = timezone;
        }

         public String getAction() {
             return action;
         }

         public void setAction(String action) {
             this.action = action;
         }

         public String getTag() {
             return tag;
         }

         public void setTag(String tag) {
             this.tag = tag;
         }
     }
}
