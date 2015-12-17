package pasha.elagin.xster.network;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import pasha.elagin.xster.Constants;

/**
 * Created by pavel on 17.12.15.
 */
public class OAuthAuthorization {

    private static final Random RAND = new Random();
    private static final String HMAC_SHA1 = "HmacSHA1";

    static String generateAuthorizationHeader(String method, String url, Map<String, String> params, String token) {
        long timestamp = System.currentTimeMillis() / 1000;
        long nonce = timestamp + RAND.nextInt();
        return generateAuthorizationHeader(method, url, params, String.valueOf(nonce), String.valueOf(timestamp), token);
    }

    static String generateAuthorizationHeader(String method, String url, Map<String, String> params, String nonce, String timestamp, String otoken) {
        List<HttpParameter> oauthHeaderParams = new ArrayList<HttpParameter>(5);
        oauthHeaderParams.add(new HttpParameter("oauth_consumer_key", Constants.OAUTH_CONSUMER_KEY));
        oauthHeaderParams.add(new HttpParameter("oauth_signature_method", "HMAC-SHA1"));
        oauthHeaderParams.add(new HttpParameter("oauth_timestamp", timestamp));
        oauthHeaderParams.add(new HttpParameter("oauth_nonce", nonce));
        oauthHeaderParams.add(new HttpParameter("oauth_version", "1.0"));
        oauthHeaderParams.add(new HttpParameter("oauth_token", otoken));

        int signatureBaseParamsSize = oauthHeaderParams.size();
        if(params!= null)
            signatureBaseParamsSize += params.size();
        List<HttpParameter> signatureBaseParams = new ArrayList<>(signatureBaseParamsSize);
        signatureBaseParams.addAll(oauthHeaderParams);
        for(Map.Entry<String, String> entry : params.entrySet()) {
            signatureBaseParams.add(new HttpParameter(entry.getKey(), entry.getValue()));
        }

        parseGetParameters(url, signatureBaseParams);
        StringBuilder base = new StringBuilder(method).append("&")
                .append(HttpParameter.encode(constructRequestURL(url))).append("&");
        base.append(HttpParameter.encode(normalizeRequestParameters(signatureBaseParams)));
        String oauthBaseString = base.toString();
        //logger.debug("OAuth base string: ", oauthBaseString);
        String signature = generateSignature(oauthBaseString, otoken);
        //logger.debug("OAuth signature: ", signature);

        oauthHeaderParams.add(new HttpParameter("oauth_signature", signature));

        // http://oauth.net/core/1.0/#rfc.section.9.1.1
//        if (realm != null) {
//            oauthHeaderParams.add(new HttpParameter("realm", realm));
//        }
        return "OAuth " + encodeParameters(oauthHeaderParams, ",", true);
    }

    static void parseGetParameters(String url, List<HttpParameter> signatureBaseParams) {
        int queryStart = url.indexOf("?");
        if (-1 != queryStart) {
            url.split("&");
            String[] queryStrs = url.substring(queryStart + 1).split("&");
            try {
                for (String query : queryStrs) {
                    String[] split = query.split("=");
                    if (split.length == 2) {
                        signatureBaseParams.add(
                                new HttpParameter(URLDecoder.decode(split[0],
                                        "UTF-8"), URLDecoder.decode(split[1],
                                        "UTF-8"))
                        );
                    } else {
                        signatureBaseParams.add(
                                new HttpParameter(URLDecoder.decode(split[0],
                                        "UTF-8"), "")
                        );
                    }
                }
            } catch (UnsupportedEncodingException ignore) {
            }
        }
    }

    static String constructRequestURL(String url) {
        int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        int slashIndex = url.indexOf("/", 8);
        String baseURL = url.substring(0, slashIndex).toLowerCase();
        int colonIndex = baseURL.indexOf(":", 8);
        if (-1 != colonIndex) {
            // url contains port number
            if (baseURL.startsWith("http://") && baseURL.endsWith(":80")) {
                // http default port 80 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            } else if (baseURL.startsWith("https://") && baseURL.endsWith(":443")) {
                // http default port 443 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            }
        }
        url = baseURL + url.substring(slashIndex);

        return url;
    }

    private static String normalizeRequestParameters(List<HttpParameter> params) {
        Collections.sort(params);
        return encodeParameters(params);
    }

    /**
     * @param httpParams parameters to be encoded and concatenated
     * @return encoded string
     * @see <a href="http://wiki.oauth.net/TestCases">OAuth / TestCases</a>
     * @see <a href="http://groups.google.com/group/oauth/browse_thread/thread/a8398d0521f4ae3d/9d79b698ab217df2?hl=en&lnk=gst&q=space+encoding#9d79b698ab217df2">Space encoding - OAuth | Google Groups</a>
     */
    public static String encodeParameters(List<HttpParameter> httpParams) {
        return encodeParameters(httpParams, "&", false);
    }

    public static String encodeParameters(List<HttpParameter> httpParams, String splitter, boolean quot) {
        StringBuilder buf = new StringBuilder();
        for (HttpParameter param : httpParams) {
            if (buf.length() != 0) {
                if (quot) {
                    buf.append("\"");
                }
                buf.append(splitter);
            }
            buf.append(HttpParameter.encode(param.getName())).append("=");
            if (quot) {
                buf.append("\"");
            }
            buf.append(HttpParameter.encode(param.getValue()));
        }
        if (buf.length() != 0) {
            if (quot) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     * @param data  the data to be signed
     * @param token the token
     * @return signature
     * @see <a href="http://oauth.net/core/1.0a/#rfc.section.9.2.1">OAuth Core - 9.2.1.  Generating Signature</a>
     */
    static String generateSignature(String data, String token) {
        byte[] byteHMAC = null;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            SecretKeySpec spec;
            //if (null == token) {
                String oauthSignature = HttpParameter.encode(Constants.OAUTH_CONSUMER_SECRET) + "&";
                spec = new SecretKeySpec(oauthSignature.getBytes(), HMAC_SHA1);
//            } else {
//                spec = token.getSecretKeySpec();
//                if (null == spec) {
//                    String oauthSignature = HttpParameter.encode(consumerSecret) + "&" + HttpParameter.encode(token.getTokenSecret());
//                    spec = new SecretKeySpec(oauthSignature.getBytes(), HMAC_SHA1);
//                    token.setSecretKeySpec(spec);
//                }
//            }
            mac.init(spec);
            byteHMAC = mac.doFinal(data.getBytes());
        } catch (InvalidKeyException ike) {
            //logger.error("Failed initialize \"Message Authentication Code\" (MAC)", ike);
            throw new AssertionError(ike);
        } catch (NoSuchAlgorithmException nsae) {
            //logger.error("Failed to get HmacSHA1 \"Message Authentication Code\" (MAC)", nsae);
            throw new AssertionError(nsae);
        }
        return BASE64Encoder.encode(byteHMAC);
    }

    private static List<HttpParameter> toParamList(HttpParameter[] params) {
        List<HttpParameter> paramList = new ArrayList<HttpParameter>(params.length);
        paramList.addAll(Arrays.asList(params));
        return paramList;
    }
}
