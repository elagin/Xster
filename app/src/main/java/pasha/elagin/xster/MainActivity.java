package pasha.elagin.xster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final String CLASS_TAG = getClass().getName();

    private EditText editText;
    private Button buttonPost;
    private Button buttongetTimeLine;
    private TextView textViewCharactersLeft;
    private View notifyTop;
    private TextView textNotify;

    private int MAX_CHARS_TWIT_SIZE = 140;

    private MyApp myApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(CLASS_TAG, "MainActivity onCreate");

        myApp = (MyApp) getApplicationContext();

        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(MyIntentService.BROADCAST_ACTION);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Instantiates a new ResponseStateReceiver
        ResponseStateReceiver mDownloadStateReceiver = new ResponseStateReceiver();

        // Registers the ResponseStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, statusIntentFilter);

        setContentView(R.layout.activity_main);

        buttonPost = (Button) findViewById(R.id.buttonPost);
        buttonPost.setOnClickListener(this);

        buttongetTimeLine = (Button) findViewById(R.id.buttongetTimeLine);
        buttongetTimeLine.setOnClickListener(this);

        textNotify = (TextView) findViewById(R.id.text_notify);

        textViewCharactersLeft = (TextView) findViewById(R.id.textViewCharactersLeft);

        editText = (EditText) findViewById(R.id.editText);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_CHARS_TWIT_SIZE)});
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                checkTwitText();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.buttonPost:
                buttonPost.setEnabled(false);
                MyIntentService.startActionCreateTwit(this, String.valueOf(editText.getText()));
                break;
            case R.id.buttongetTimeLine:
                buttongetTimeLine.setEnabled(false);
                MyIntentService.startActionGetHomeTimeline(this);
                break;
        }
    }

    private class ResponseStateReceiver extends BroadcastReceiver {
        private ResponseStateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(CLASS_TAG, "ResponseStateReceiver onReceive");
            showNotify();
            int resultCode = intent.getIntExtra(MyIntentService.RESULT_CODE, 0);
            switch (intent.getStringExtra(MyIntentService.EXTENDED_OPERATION_TYPE)) {
                case MyIntentService.ACTION_CREATE_TWIT:
                    Log.d(CLASS_TAG, "ResponseStateReceiver ACTION_CREATE_TWIT");
                    if (resultCode == MyIntentService.RESULT_SUCCSESS) {
                        editText.setText("");
                    }
                    buttonPost.setEnabled(true);
                    break;

                case MyIntentService.ACTION_GET_HOME_TIMELINE:
                    Log.d(CLASS_TAG, "ResponseStateReceiver ACTION_GET_HOME_TIMELINE");
                    buttongetTimeLine.setEnabled(true);
                    break;

                case MyIntentService.ACTION_SHOW_NOTIFY:
                    Log.d(CLASS_TAG, "ResponseStateReceiver ACTION_SHOW_NOTIFY");
                    break;
            }
        }
    }

    void showNotify() {
        textNotify.setText(myApp.getStatusMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(CLASS_TAG, "MainActivity onReceive");
        showNotify();
        checkTwitText();
    }

    void checkTwitText() {
        int characters = editText.getText().toString().length();
        textViewCharactersLeft.setText(String.format(this.getString(R.string.chars_left), MAX_CHARS_TWIT_SIZE - characters));
        buttonPost.setEnabled(characters > 0);
    }
}
