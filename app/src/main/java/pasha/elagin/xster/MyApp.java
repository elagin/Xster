package pasha.elagin.xster;

import android.app.Application;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MyApp extends Application {
    {
        singleton = this;
    }

    private static MyApp singleton;

    public static MyApp getInstance() {
        return singleton;
    }

    private String statusMessage = "";

    private List<Twit> twitList;

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    public boolean setTwitList(JSONArray value) {
        if(twitList == null)
            twitList = new ArrayList<>();
        for(int i = 0; i < value.length(); i++) {
            try {
                JSONObject item = value.getJSONObject(i);
                Twit twit = new Twit(item);
                twitList.add(twit);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
