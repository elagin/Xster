package pasha.elagin.xster;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by elagin on 15.12.15.
 */
public class Twit {
    protected String text;
    protected Date created;
    protected String username;

    public Twit() {
    }

    public Twit(JSONObject item) throws JSONException, ParseException {
        text = item.getString("text");
        created = getTwitterDate(item.getString("created_at"));
        JSONObject user = item.getJSONObject("user");
        username = user.getString("name");
    }

    protected Date getTwitterDate(String date) throws ParseException {
        final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }
}
