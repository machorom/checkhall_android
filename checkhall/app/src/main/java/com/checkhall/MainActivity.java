package com.checkhall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.checkhall.util.DeviceUtil;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    final static int SEND_KAKAO = 1;
    final static int SEND_SMS = 2;
    final static int SEND_EMAIL = 3;
    final static int MAKE_ACALL = 4;

    private WebView webview;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Intent it = null;
            Uri uri = null;
            switch (msg.what) {
                case SEND_KAKAO:
                    Log.d("HybridApp", "SEND_KAKAO obj=" + msg.obj );
                    sendKakaoMessage(((HashMap<String, String>)msg.obj).get("type"),((HashMap<String, String>)msg.obj).get("title"), ((HashMap<String, String>)msg.obj).get("imageUrl"), ((HashMap<String, String>)msg.obj).get("link"));
                    break;

                case SEND_SMS:
                    Log.d("HybridApp", "SEND_SMS obj=" + msg.obj );
                    uri = Uri.parse("smsto:"+((HashMap<String, String>)msg.obj).get("mobile_no"));
                    it = new Intent(Intent.ACTION_SENDTO, uri);
                    it.putExtra("sms_body", ((HashMap<String, String>)msg.obj).get("body"));
                    startActivity(it);
                    break;

                case SEND_EMAIL:
                    Log.d("HybridApp", "SEND_EMAIL obj=" + msg.obj );
                    it = new Intent(Intent.ACTION_SEND);
                    it.putExtra(Intent.EXTRA_EMAIL, ((HashMap<String, String>)msg.obj).get("email"));
                    it.putExtra(Intent.EXTRA_SUBJECT, ((HashMap<String, String>)msg.obj).get("title"));
                    it.putExtra(Intent.EXTRA_TEXT, ((HashMap<String, String>)msg.obj).get("body"));
                    it.setType("text/plain");
                    startActivity(Intent.createChooser(it, "Choose Email Client"));
                    break;

                case MAKE_ACALL:
                    Log.d("HybridApp", "MAKE_ACALL obj=" + msg.obj );
                    uri = Uri.parse("tel:" + ((HashMap<String, String>)msg.obj).get("phone_number"));
                    it = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(it);
                    break;
            }
        }
    };

    private void sendKakaoMessage(String type, String title, String imageUrl, String link)
    {
        try {
            KakaoLink kakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
            KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
            kakaoTalkLinkMessageBuilder.addText(title);
            kakaoTalkLinkMessageBuilder.addImage(imageUrl, 300, 200);
            // 앱이 설치되어 있는 경우 kakao<app_key>://kakaolink?execparamkey1=1111 로 이동. 앱이 설치되어 있지 않은 경우 market://details?id=com.kakao.sample.kakaolink&referrer=kakaotalklink 또는 https://itunes.apple.com/app/id12345로 이동
//            kakaoTalkLinkMessageBuilder.addAppLink("link text?",
//                    new AppActionBuilder()
//                            .addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder().setExecuteParam("execparamkey1=1111").setMarketParam("referrer=kakaotalklink").build())
//                            .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder(AppActionBuilder.DEVICE_TYPE.PHONE).setExecuteParam("execparamkey1=1111").build())
//                            .setUrl("http://www.kakao.com")
//                            .build());

            kakaoTalkLinkMessageBuilder.addWebLink(link, link);
            // 웹싸이트에 등록된 kakao<app_key>://kakaolink로 이동
//                kakaoTalkLinkMessageBuilder.addAppButton(getString(R.string.kakaolink_appbutton), new AppActionBuilder()
//                        .addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder().setExecuteParam("execparamkey2=2222").setMarketParam("referrer=kakaotalklink").build())
//                        .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder(AppActionBuilder.DEVICE_TYPE.PHONE).setExecuteParam("execparamkey2=2222").build())
//                        .setUrl("http://www.kakao.com").build());
                // 웹싸이트에 등록한 "http://www.kakao.com"으로 이동.
            kakaoTalkLinkMessageBuilder.addWebButton("연결", null);
            kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder, this);
        } catch (KakaoParameterException e) {
            Log.d("HybridApp",e.getMessage());
        }
    }

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
        webview.addJavascriptInterface(new AndroidBridge(), "HybridApp");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        Log.i("HybridApp","initWebView loadUrl=" + getActionUrl());
        webview.loadUrl(getActionUrl());
    }

    private boolean isLastPag(){
        Log.d("HybridApp", "isLastPag url=" + webview.getUrl());
        if( webview.getUrl().equals("http://www.checkhall.com/plan/")
                || webview.getUrl().equals("http://www.checkhall.com/hall/")
                || webview.getUrl().equals("http://www.checkhall.com/hall/index.jsp")
                || webview.getUrl().equals("http://www.checkhall.com/")
                || webview.getUrl().equals("http://www.checkhall.com/index.jsp")
                || webview.getUrl().equals("http://www.checkhall.com/plan/index.jsp")
                || webview.getUrl().equals("http://www.checkhall.com/member/login.jsp")
                || !webview.canGoBack() ){
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("MainActivity","onKeyDown keyCode="+keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!isLastPag()) {
                webview.goBack();
            } else {
                alertAppFinish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
                                Log.d("HybridApp","TokenId register param - " + data);
                                connection.setDoOutput(true);
                                connection.getOutputStream().write(data.getBytes());
                                if (connection.getResponseCode() == 200) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                                    DeviceUtil.setLogined(MainActivity.this);
                                    Log.d("HybridApp","TokenId register Success - " + connection.getResponseCode() + "," + reader.readLine());
                                } else {
                                    Log.d("HybridApp","TokenId register fail - " + connection.getResponseCode() + ", "+ connection.getResponseMessage());
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                Log.e("HybridApp",e.getMessage());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e("HybridApp",e.getMessage());
                            }
                        }
                    });
                    Log.d("HybridApp", "server.idx = " + jobj.getString("idx"));
                    DeviceUtil.setUserIdx(MainActivity.this, jobj.getString("idx"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d("HybridApp", cm.message() + " -- From line "
                    + cm.lineNumber() + " of "
                    + cm.sourceId() );
            return true;
        }
    }

    private class WishWebViewClient extends WebViewClient {
       @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.d("HybridApp", "onReceivedError " + error.getDescription().toString() );
            super.onReceivedError(view, request, error);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            Log.d("HybridApp","shouldOverrideUrlLoading url="+url);
            view.loadUrl(url);
            return true;
        }
    }

    private class AndroidBridge {
        private void sendHandMessage(int what, Object obj){
            Message message = mHandler.obtainMessage();
            message.what = what;
            message.obj = obj;
            mHandler.sendMessage(message);
        }

        @JavascriptInterface
        public void sendKakao(final String type, final String title, final String imageUrl, final String link) {
            Log.d("HybridApp", "sendKakao("+type+","+title+", "+imageUrl+","+link+")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("type", type);
            paramMap.put("title", title);
            paramMap.put("imageUrl", imageUrl);
            paramMap.put("link", link);
            sendHandMessage(SEND_KAKAO, paramMap);
        }

        @JavascriptInterface
        public void sendSms(final String mobile_no, final String body) {
            Log.d("HybridApp", "sendSms(" + mobile_no + "," + body + ")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("mobile_no", mobile_no);
            paramMap.put("body", body);
            sendHandMessage(SEND_SMS, paramMap);
        }

        @JavascriptInterface
        public void sendEmail(final String email, final String title, final String body) {
            Log.d("HybridApp", "sendEmail(" + email + ", " + title + ", " + body + ")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("email", email);
            paramMap.put("title", title);
            paramMap.put("body", body);
            sendHandMessage(SEND_EMAIL, paramMap);
        }

        @JavascriptInterface
        public void makeACall(final String phone_number) {
            Log.d("HybridApp", "makeACall(" + phone_number + ")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("phone_number", phone_number);
            sendHandMessage(MAKE_ACALL, paramMap);
        }

    }
}
