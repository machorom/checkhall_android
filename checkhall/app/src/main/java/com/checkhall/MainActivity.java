package com.checkhall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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

    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView) findViewById(R.id.webview);

        initWebView();
    }
    private String getActionUrl(){
        String url = null;
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            url = extras.get("action_url").toString();
        } else {
            url = "http://www.checkhall.com/member/login.jsp";
        }
        return url;
    }

    private void initWebView(){
        webview.setWebViewClient(new WishWebViewClient ());
        webview.setWebChromeClient(new WishWebChromeClient());
        WebSettings webSettings = webview.getSettings();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        Log.i("Main","initWebView loadUrl=" + getActionUrl());
        webview.loadUrl(getActionUrl());
    }

    private boolean isLastPag(){
        Log.d("Main", "isLastPag url=" + webview.getUrl());
        if( !webview.canGoBack() || webview.getUrl().equals("http://www.checkhall.com/plan/")
          || webview.getUrl().equals("http://www.checkhall.com/member/login.jsp")){
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && !isLastPag()) {
            webview.goBack();
            return true;
        }

        alertAppFinish();
        return true;
        //return super.onKeyDown(keyCode, event);
    }

    private void alertAppFinish(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // AlertDialog 셋팅
        alertDialogBuilder
                .setMessage("앱을 종료하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("종료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 프로그램을 종료한다
                                MainActivity.this.finish();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                // 다이얼로그를 취소한다
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
                                    DeviceUtil.setLogined(MainActivity.this);
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
