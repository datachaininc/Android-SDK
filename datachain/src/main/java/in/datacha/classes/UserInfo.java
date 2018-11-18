package in.datacha.classes;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

class UserInfo {
    private String publisher_key;
    private String advertising_id;
    private String md5Email;
    private String sha1Email;
    private String sha256Email;
    private String md5Phone_number;
    private String sha1Phone_number;
    private String sha256Phone_number;
    private long time;
    private int timezone;
    private String sdk_version;
    private String app_package_name;

    public UserInfo() {
        this.sdk_version = DatachainConstants.DATACHAIN_SDK_VERSION;
        this.time = new Date().getTime();
        this.timezone = TimeZone.getDefault().getRawOffset();

    }

    UserInfo(String publisher_key, String advertising_id) {
        this.publisher_key = publisher_key;
        this.advertising_id = advertising_id;
        this.time = new Date().getTime();
        this.timezone = TimeZone.getDefault().getRawOffset();
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

     String getMd5Email() {
        return md5Email;
    }

     void setMd5Email(String md5Email) {
        this.md5Email = md5Email;
    }

     String getSha1Email() {
        return sha1Email;
    }

    void setSha1Email(String sh1Email) {
        this.sha1Email = sh1Email;
    }

    String getSha256Email() {
        return sha256Email;
    }

    void setSha256Email(String sha256Email) {
        this.sha256Email = sha256Email;
    }

    String getMd5Phone_number() {
        return md5Phone_number;
    }

    void setMd5Phone_number(String md5Phone_number) {
        this.md5Phone_number = md5Phone_number;
    }

    String getSha1Phone_number() {
        return sha1Phone_number;
    }

    void setSha1Phone_number(String sh1Phone_number) {
        this.sha1Phone_number = sh1Phone_number;
    }

    String getSha256Phone_number() {
        return sha256Phone_number;
    }

    void setSha256Phone_number(String sha256Phone_number) {
        this.sha256Phone_number = sha256Phone_number;
    }


}
