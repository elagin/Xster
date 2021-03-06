package pasha.elagin.xster.network;

import android.annotation.SuppressLint;
import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

/**
 * Created by pavel on 14.12.15.
 */
public class CustomTrustManager {
    private static TrustManager[] trustManagers;

    @SuppressLint("TrulyRandom")
    public static void allowAllSSL() {

        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        javax.net.ssl.SSLContext sslContext;

        if (trustManagers == null) {
            trustManagers = new javax.net.ssl.TrustManager[]{new _FakeX509TrustManager()};
        }

        try {
            sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.e("allowAllSSL", e.toString());
        }
    }

    public static class _FakeX509TrustManager implements javax.net.ssl.X509TrustManager {
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @SuppressWarnings("UnusedParameters")
        public boolean isClientTrusted(X509Certificate[] chain) {
            return (true);
        }

        @SuppressWarnings("UnusedParameters")
        public boolean isServerTrusted(X509Certificate[] chain) {
            return (true);
        }

        public X509Certificate[] getAcceptedIssuers() {
            return (_AcceptedIssuers);
        }
    }
}
