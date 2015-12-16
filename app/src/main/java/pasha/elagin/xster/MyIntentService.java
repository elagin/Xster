package pasha.elagin.xster;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import pasha.elagin.xster.network.CreateTwitRequest;
import pasha.elagin.xster.network.GetHomeTimelineRequest;
import pasha.elagin.xster.network.RequestAnswer;

/**
 * Created by pavel on 14.12.15.
 */
public class MyIntentService extends IntentService {

    final String CLASS_TAG = getClass().getName();

    private final BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);

    public static final String APP_PATH = "pasha.elagin.xster.";
    // Defines a custom Intent action
    public static final String BROADCAST_ACTION = APP_PATH + "BROADCAST";

    public static final String ACTION_CREATE_TWIT = APP_PATH + "action.CreateTwit";
    public static final String ACTION_GET_HOME_TIMELINE = APP_PATH + "action.GetHomeTimeline";
    public static final String ACTION_SHOW_NOTIFY = APP_PATH + "action.showNotify";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = APP_PATH + "STATUS";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_OPERATION_TYPE = APP_PATH + "OPERATION_TYPE";

    // Defines the key for the log "extra" in an Intent
    public static final String EXTENDED_STATUS_LOG = APP_PATH + "LOG";

    public static final String RESULT_CODE = "result_code";
    public final static int RESULT_SUCCSESS = 0;
    public final static int RESULT_ERROR = 1;

    public static final String RESULT = "RESULT";

    private static final String PARAM_MSG = "msg";

    private MyApp myApp = null;

    public MyIntentService() {
        super("MyIntentService");
        Log.d(CLASS_TAG, "MyIntentService Constructor");
    }

    public void onCreate() {
        super.onCreate();
        Log.d(CLASS_TAG, "MyIntentService onCreate");
        myApp = (MyApp) getApplicationContext();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(CLASS_TAG, "MyIntentService onDestroy");
    }

    private static Intent newIntent(Context context, String action) {
        Intent res = new Intent(context, MyIntentService.class);
        res.setAction(action);
        return res;
    }

    public static void startActionCreateTwit(Context context, String message) {
        Log.d("MyIntentService", "startActionCreateTwit");
        Intent intent = newIntent(context, ACTION_CREATE_TWIT);
        intent.putExtra(PARAM_MSG, message);
        context.startService(intent);
    }

    public static void startActionGetHomeTimeline(Context context) {
        Log.d("MyIntentService", "startActionGetHomeTimeline");
        Intent intent = newIntent(context, ACTION_GET_HOME_TIMELINE);
        context.startService(intent);
    }

    private RequestAnswer handleActionCreatePoint(Intent intent) {
        Log.d(CLASS_TAG, "handleActionCreatePoint");
        final String message = intent.getStringExtra(PARAM_MSG);
        return new CreateTwitRequest(this, message).request();
    }

    private RequestAnswer handleActionGetHomeTimeline(Intent intent) {
        Log.d(CLASS_TAG, "handleActionGetHomeTimeline");
        return new GetHomeTimelineRequest(this).request();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        myApp.setStatusMessage(getString(R.string.send_request));
        mBroadcaster.broadcastIntentWithState(ACTION_SHOW_NOTIFY, RESULT_SUCCSESS, "");

        switch (action) {
            case ACTION_CREATE_TWIT: {
                RequestAnswer result = handleActionCreatePoint(intent);
                if (!result.isHaveError()) {
                    Log.d(CLASS_TAG, "onHandleIntent ACTION_CREATE_TWIT SUCCSESS");
                    try {
                        JSONObject json = new JSONObject(result.getAnswer());
                        try {
                            Twit twit = new Twit(json);
                            myApp.setStatusMessage(getString(R.string.sended_to_twiter));
                            mBroadcaster.broadcastIntentWithState(action, RESULT_SUCCSESS, "");
                        } catch (ParseException e) {
                            e.printStackTrace();
                            result.setError(e.getLocalizedMessage());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result.setError(e.getLocalizedMessage());
                    }
                }
                Log.d(CLASS_TAG, "onHandleIntent ACTION_CREATE_TWIT ERROR");
                myApp.setStatusMessage(result.getError());
                mBroadcaster.broadcastIntentWithState(action, RESULT_ERROR, "");
            }
            break;

            case ACTION_GET_HOME_TIMELINE: {
                RequestAnswer result = handleActionGetHomeTimeline(intent);
                if (!result.isHaveError()) {
                    String answ = result.getAnswer();
                    try {
                        JSONArray array = new JSONArray(answ);
                        if(myApp.setTwitList(array)) {
                            Log.d(CLASS_TAG, "onHandleIntent ACTION_GET_HOME_TIMELINE SUCCSESS");
                            myApp.setStatusMessage(getString(R.string.recived_from_twiter));
                            mBroadcaster.broadcastIntentWithState(action, RESULT_SUCCSESS, "");
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result.setError(e.getLocalizedMessage());
                    }
                }
                Log.d(CLASS_TAG, "onHandleIntent ACTION_GET_HOME_TIMELINE ERROR");
                myApp.setStatusMessage(result.getError());
                mBroadcaster.broadcastIntentWithState(action, RESULT_ERROR, result.getError());
            }
            break;
        }
    }
}
