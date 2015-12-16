package pasha.elagin.xster.network;

import android.content.Context;

/**
 * Created by elagin on 15.12.15.
 */
public class GetHomeTimelineRequest extends HTTPClient {
    public GetHomeTimelineRequest(Context context) {
        this.context = context;
        path = "statuses/home_timeline.json";
        method = "GET";
    }
}
