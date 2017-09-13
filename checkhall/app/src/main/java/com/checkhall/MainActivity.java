package com.checkhall;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.checkhall.util.DeviceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new WishWebViewClient ());
        webview.setWebChromeClient(new WishWebChromeClient());
        WebSettings webSettings = webview.getSettings();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//      if (Build.VERSION.SDK_INT >= 19) {
//            webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//        }

        webview.loadUrl("http://www.checkhall.com/member/login.jsp");
    }

    private class WishWebChromeClient extends WebChromeClient{

        public boolean onConsoleMessage(ConsoleMessage cm) {
            if(cm.message().contains("{\"idx\":")){
                try {
                    JSONObject jobj = new JSONObject(cm.message());
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            URL url = null;
                            try {
                                url = new URL("http://m.checkhall.com/member/setPushToken.jsp");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                String data = "idx="+DeviceUtil.getUserIdx(MainActivity.this)+"&device_id="+ DeviceUtil.getDeviceUUID(MainActivity.this)+"&push_type=fcm&push_token="+DeviceUtil.getPushTokenId(MainActivity.this);
                                Log.d("TokenId","TokenId register param - " + data);
                                connection.setDoOutput(true);
                                connection.getOutputStream().write(data.getBytes());
                                if (connection.getResponseCode() == 200) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                                    Log.d("TokenId","TokenId register Success - " + connection.getResponseCode() + "," + reader.readLine());
                                } else {
                                    Log.d("TokenId","TokenId register fail - " + connection.getResponseCode() + ", "+ connection.getResponseMessage());
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Log.d("WishWebChromeClient", "server.idx = " + jobj.getString("idx"));
                    DeviceUtil.setUserIdx(MainActivity.this, jobj.getString("idx"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d("WishWebChromeClient", cm.message() + " -- From line "
                    + cm.lineNumber() + " of "
                    + cm.sourceId() );
            return true;
        }
    }

    private class WishWebViewClient extends WebViewClient {

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.d("WishWebViewClient", "onReceivedError " + error.getDescription().toString() );
            super.onReceivedError(view, request, error);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            Log.d("WishWebViewClient","shouldOverrideUrlLoading url="+url);
            view.loadUrl(url);
            return true;
        }
    }
}
