package pasha.elagin.xster.network;

import android.content.Context;

import java.util.HashMap;

/**
 * Created by pavel on 14.12.15.
 */
public class CreateTwitRequest extends HTTPClient {
    public CreateTwitRequest(Context context, String message) {
        this.context = context;
        path = "statuses/update.json";
        method = "POST";

        post = new HashMap<>();
        post.put("status", message);
    }
}
