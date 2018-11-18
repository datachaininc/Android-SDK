package in.datacha.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import in.datacha.BuildConfig;

public class HttpPostAsyncTask extends AsyncTask<String, Void, Void> {
    @SuppressLint("StaticFieldLeak")
    private Context context;
    HttpPostAsyncTask(Context ctx){
        this.context = ctx;
    }
    public HttpPostAsyncTask(){

    }
    @Override
    protected Void doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        try {

            if(params!=null && params.length>=3) {

                try {
                    URL url = new URL(params[0]);
                    String key = params[1];
                    String postData = params[2];
                    String type = params[3];

                    if(postData==null){
                        return null;
                    }
                    if(!SharedPrefOperations.getBoolean(context, DatachainConstants.DATACHAIN_TRACKING_ENABLED,true))
                        return null;

                    // Create the urlConnection
                    urlConnection = (HttpURLConnection) url.openConnection();


                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Sdk_Version", DatachainConstants.DATACHAIN_SDK_VERSION);

                    urlConnection.setRequestMethod("POST");


                    urlConnection.setRequestProperty("x-api-key", key);

                    // Send the post body
                    JSONObject payload = new JSONObject();
                    payload.put("raw", postData);

                    String[] signedData = signData(postData);
                    String signedKey = SharedPrefOperations.getString(context, DatachainConstants.DATACHAIN_SIGNED_KEY,"");
                    if(signedData==null || signedKey.length()<=0){
                        return null;
                    }

                    payload.put("signed", signedData[0]);
                    payload.put("key", signedData[1].trim());
                    payload.put("signed_key", signedKey);


                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
//                    String postDataPayload = new Gson().toJson(payload);
//                    if(postDataPayload==null || postDataPayload.length()<=0)
//                        return null;
                    writer.write(payload.toString());
                    writer.flush();
                    writer.close();

                    int statusCode = urlConnection.getResponseCode();
                    if(type!=null ) {
                        Date cDate = new Date();
                        String tDate = new SimpleDateFormat("yyyyMMdd",Locale.getDefault()).format(cDate);
                        if (type.equals(DatachainConstants.DATACHAIN_LOCATION_UPDATE) && statusCode == 200) {

                            if (context != null) {
                                SharedPrefOperations.removeKey(context, BuildConfig.DATACHAIN_USER_LOCATIONS);
                            }

                        }else if(type.equals(DatachainConstants.DATACHAIN_USER_DETAILS_UPDATE) && statusCode==200){

                            int currentDate = Integer.parseInt(tDate);
                            SharedPrefOperations.putInt(context,BuildConfig.DATACHAIN_LAST_UPDATE_TIME,currentDate);
                        }else if(type.equals(DatachainConstants.DATACHAIN_SESSION_DETAIL_UPDATE) && statusCode==200){
                            if (context != null) {
                                SharedPrefOperations.removeKey(context, DatachainConstants.DATACHAIN_SESSION_DETAILS);
                            }
                        }else if(type.equals(DatachainConstants.DATACHAIN_USER_INTEREST_UPDATE) && statusCode==200){
                            if (context != null) {
                                SharedPrefOperations.removeKey(context, DatachainConstants.DATACHAIN_USER_INTERESTS);
                            }
                        }
                        Utils.Log("Update status "+type+":"+String.valueOf(statusCode));
                    }
                    else {
                        Utils.Log("Update status :"+String.valueOf(statusCode));
                    }


                }catch (IOException e){
                    e.printStackTrace();
                }
                finally {
                    if(urlConnection!=null){
                        try {
                            urlConnection.getInputStream().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        urlConnection.disconnect();
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sign the data with keystore
     *
     * @param postData  Data to be signed
     * @return new String[]{signedData, publickey}
     */
    private String[] signData(String postData) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyPair keyPair = Main.getKeyByAlias(keyStore);
            if(keyPair==null){
                return null;
            }
            else{
                String publicKey = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
                return new String[]{signData(keyPair.getPrivate(),postData),publicKey};
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
        return null;
    }

    @Nullable
    private String signData(PrivateKey privateKey, String data) {
        if(privateKey==null)
            return null;
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes("UTF-8"));
            byte[] signedData = signature.sign();

            StringBuilder sb = new StringBuilder(signedData.length * 2);
            for(byte b: signedData)
                sb.append(String.format("%02x", b));
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
