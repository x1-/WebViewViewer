package com.inkenkun.x1.webviewviewer;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

public class MainActivity extends Activity {

    private EditText urlEdit;
    private TextView htmlView;
    private WebView wv;
    private Button show;
    private Button posSetter;
    private Boolean synced = false;
    // AdvertisingID
    private String advertisingId;
    // OptOut
    private boolean isAdTrackingEnabled = false;
    private Handler handler;

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        syncAdvertisingId();

        wv = (WebView)findViewById(R.id.wv);
        htmlView = (TextView) findViewById(R.id.html);
        show = (Button) findViewById(R.id.buttonShow);
        posSetter = (Button) findViewById(R.id.buttonSetPos);

        wv.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ( url.startsWith("http") ) {
                    urlEdit.setText( url );
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if ( url.startsWith("http") ) {
                    urlEdit.setText( url );
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.startsWith("x1scheme://")) {
                    setAdvertisingId();
                }
            }

        });
        wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setBuiltInZoomControls(true);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 16) {
            wv.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

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

        htmlView.setOnKeyListener( new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;
            }
        } );

        findViewById( R.id.buttonGo ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                confirm();
            }
        });
        findViewById( R.id.buttonBack ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                wv.goBack();
            }
        });
        findViewById( R.id.buttonShow ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String caption = show.getText().toString();
                if ( caption.equals( getString(R.string.show_name) ) ) {
                    wv.loadUrl( "javascript:window.activity.viewSource(document.documentElement.outerHTML);" );
                    show.setText( getString(R.string.hide_name) );
                    htmlView.setVisibility( View.VISIBLE );
                } else {
                    show.setText( getString(R.string.show_name) );
                    htmlView.setText( "" );
                    htmlView.setVisibility( View.GONE );
                }
            }
        });
        findViewById( R.id.buttonSetPos ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String caption = posSetter.getText().toString();
                if ( caption.equals( getString(R.string.set_pos_name) ) ) {
                    wv.loadUrl( "javascript:setFramePos(735, 735, 0);" );
                    posSetter.setText( getString(R.string.unset_pos_name) );
                } else {
                    wv.loadUrl( "javascript:setFramePos(735, 0, 0);" );
                    posSetter.setText( getString(R.string.set_pos_name) );
                }
            }
        });

        handler = new Handler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wv.clearCache(true);
    }

    @JavascriptInterface
    public void viewSource(final String src) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if ( htmlView != null ) {
                    htmlView.setText(src);
                }
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

    private void syncAdvertisingId() {
        Thread sync = new Thread(new Runnable() {
            @Override
            public void run() {
                AdvertisingIdClient.Info info = null;
                try {
                    info = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                    advertisingId = info.getId();
                    isAdTrackingEnabled = info.isLimitAdTrackingEnabled();
                    Log.d( "DEBUG", "Android AdvertisingId : " + advertisingId );
                    Log.d( "DEBUG", "OptoutFlag : " + String.valueOf(isAdTrackingEnabled) );
                    synced = true;
                } catch (IOException e) {
                    Log.d( "DEBUG", "GooglePlayServicesへの接続失敗" );
                } catch (IllegalStateException e) {
                    Log.d( "DEBUG", "メインスレッドで処理が呼ばれた" );
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.d( "DEBUG", "GooglePlayがデバイスにインストールされていなかった" );
                } catch (GooglePlayServicesRepairableException e) {
                    Log.d( "DEBUG", "GooglePlayServicesを使ううえでエラーが起きた" );
                }
            }
        });
        sync.start();
    }

    private void setAdvertisingId() {
        if ( synced ) {
            wv.loadUrl( "javascript:setIDFAOption('" + advertisingId + "', " + isAdTrackingEnabled + ");" );
            Log.d( "DEBUG", "javascript:setIDFAOption:" + advertisingId );
        }
    }
}
