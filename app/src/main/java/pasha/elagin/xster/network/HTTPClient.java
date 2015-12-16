package pasha.elagin.xster.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import pasha.elagin.xster.Constants;

/**
 * Created by pavel on 14.12.15.
 */
public class HTTPClient {

    private final String CLASS_NAME = getClass().toString();

    private final static String CHARSET = "UTF-8";
    private final static String SERVER = "https://api.twitter.com/1.1/";

    protected String path;
    protected String method;

    protected Context context;
    protected Map<String, String> post;

    private String getAuthHdr() {
        String res2 = String.format("%s oauth_consumer_key=\"%s\", oauth_nonce=\"%s\", ", Constants.OAUTH, Constants.OAUTH_CONSUMER_KEY, Constants.OAUTH_NONCE) +
                String.format(" oauth_signature=\"%s\", oauth_signature_method=\"%s\", ", Constants.OAUTH_SIGNATURE, Constants.OAUTH_SIGNATURE_METHOD) +
                String.format(" oauth_timestamp=\"%s\", oauth_token=\"%s\", oauth_version=\"%s\"", Constants.OAUTH_TIMESTAMP, Constants.OAUTH_TOKEN, Constants.OAUTH_VERSION);
        return res2;
    }

    public RequestAnswer request() {
        RequestAnswer answer = new RequestAnswer();
        if (!isOnline(context)) {
            answer.setError("Интернет не доступен");
            return answer;
        }

        URL url;
        try {
            url = new URL(SERVER + path);
        } catch (MalformedURLException e) {
            answer.setError(e.getLocalizedMessage());
            return answer;
        }

        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();
        CustomTrustManager.allowAllSSL();
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept-Charset", CHARSET);
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
            connection.setRequestProperty("Content-Language", "ru-RU");
            connection.setRequestProperty("Authorization", getAuthHdr());
            //connection.setRequestProperty("Authorization", "OAuth oauth_consumer_key=\"IJ0r4TNnCDmX3Gf32KENCwmfu\", oauth_nonce=\"b68174be0bdfdd283b46101f133aa36d\", oauth_signature=\"iKF64GLO31J4%2BQBLVaYxWavabmw%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1450104989\", oauth_token=\"4452517714-NUYwuyOZFU5nENwhGq5MktFEVOOeAfN5cYvloBs\"");
            connection.setUseCaches(false);
            if (post != null && !post.isEmpty()) {
                String POST = makePOST(post);
                Log.d("POST", url.toString() + "?" + POST);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length", Integer.toString((POST).getBytes().length));
                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(POST);
                os.flush();
                os.close();
            }
            connection.connect();

            InputStream is;
            try {
                is = connection.getInputStream();
                if (connection.getContentEncoding() != null) {
                    is = new GZIPInputStream(is);
                }
                int responseCode = connection.getResponseCode();
                Log.d("JSON ERROR", String.valueOf(responseCode));
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                } else {
                    Log.e(CLASS_NAME, String.valueOf(responseCode));
                    answer.setError(String.format("Server return %d HTTP status code ", responseCode));
                }
            } catch (FileNotFoundException e) {
                Log.e(CLASS_NAME, e.getLocalizedMessage());
                answer.setError(String.format("Server return %s", e.getLocalizedMessage()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            answer.setError(e.getLocalizedMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (answer.isHaveError())
            return answer;

        answer.setAnswer(response.toString().replaceAll("(?i)[<|>|<|>]", ""));
        return answer;
    }

    private String makePOST(Map<String, String> post) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (final String key : post.keySet())
            try {
                if (first)
                    first = false;
                else
                    result.append("&");
                if (post.get(key) == null) {
                    //TODO Caused by: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                    return "ERROR";
                }
                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(post.get(key), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        return result.toString();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

