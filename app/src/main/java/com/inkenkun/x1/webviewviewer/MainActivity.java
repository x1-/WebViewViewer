package com.inkenkun.x1.webviewviewer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private EditText urlEdit;
    private WebView wv;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        wv = (WebView)findViewById(R.id.wv);
        wv.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                urlEdit.setText( url );
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setBuiltInZoomControls(true);

//        int SDK_INT = android.os.Build.VERSION.SDK_INT;
//        if (SDK_INT > 16) {
//            wv.getSettings().setMediaPlaybackRequiresUserGesture(false);
//        }

        urlEdit = (EditText)findViewById(R.id.url);
        urlEdit.setText( "http://" );
        confirm();


        urlEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Log.d( "onEditorAction", "actionId = " + actionId + " event = " + (event == null ? "null" : event) );
            boolean handled = false;
            if ( (actionId == EditorInfo.IME_ACTION_DONE)
                    || ( (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_UP) ) ) {
                confirm();
                handled = true;
            }
            return handled;
            }
        });
        urlEdit.setOnFocusChangeListener(new TextView.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    hideKeyboard();
            }
        });

        findViewById( R.id.buttonGo ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                confirm();
            }
        });
    }

    private void confirm() {
        String url = urlEdit.getText().toString();

        Log.d( "confirm", "URL:" + url );
        Log.d( "User-Agent", wv.getSettings().getUserAgentString() );

        hideKeyboard();
        wv.loadUrl( url );
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(urlEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
