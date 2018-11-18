package in.datacha.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import in.datacha.classes.DataChain;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataChain.getInstance()
                .publisherKey("DatachainNewBaw7wtEhP7rBrhGvueqk")
                .enableLocation(true)
                .locationUpdateInterval(12)
                .serverUrl("https://lmxlynjk9i.execute-api.ap-south-1.amazonaws.com/Prod/auth")
                .askLocationPermission(true)
                .init(this);

        String email = "jerinamathew@gmail.com";
        String phone = "+919539741274";

//        DataChain.setUserEmail(md5Hash(email), sha1Hash(email), sha256Hash(email));
//        DataChain.setUserPhoneNumber(md5Hash(phone), sha1Hash(phone), sha256Hash(phone));
//
        DataChain.setUserInterest("Edit","Image");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Returns the hashed string
     *
     * @param value The string to be hashed
     * @return hashed string
     */
    private static String md5Hash(String value){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] array = messageDigest.digest(value.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sha1Hash(String value)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(value.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes)
            {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String sha256Hash(String value)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(value.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes)
            {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
