package com.checkhall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.checkhall.util.AlertUtil;
import com.checkhall.util.DeviceUtil;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "LCheckhall:MainActivity";
    final static int SEND_KAKAO = 1;
    final static int SEND_SMS = 2;
    final static int SEND_EMAIL = 3;
    final static int MAKE_ACALL = 4;
    final static int UPLOAD_PROFILE = 5;

    final static int REQUEST_GALLARY_CODE = 1;

    private WebView webview;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Intent it = null;
            Uri uri = null;
            switch (msg.what) {
                case SEND_KAKAO:
                    Log.d(TAG, "SEND_KAKAO obj=" + msg.obj );
                    sendKakaoMessage(((HashMap<String, String>)msg.obj).get("type"),((HashMap<String, String>)msg.obj).get("title"), ((HashMap<String, String>)msg.obj).get("imageUrl"), ((HashMap<String, String>)msg.obj).get("link"));
                    break;

                case SEND_SMS:
                    Log.d(TAG, "SEND_SMS obj=" + msg.obj );
                    uri = Uri.parse("smsto:"+((HashMap<String, String>)msg.obj).get("mobile_no"));
                    it = new Intent(Intent.ACTION_SENDTO, uri);
                    try {
                        it.putExtra("sms_body", URLDecoder.decode(((HashMap<String, String>)msg.obj).get("body"),"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    startActivity(it);
                    break;

                case SEND_EMAIL:
                    Log.d(TAG, "SEND_EMAIL obj=" + msg.obj );
                    it = new Intent(Intent.ACTION_SEND);
                    it.putExtra(Intent.EXTRA_EMAIL, ((HashMap<String, String>)msg.obj).get("email"));
                    it.putExtra(Intent.EXTRA_SUBJECT, ((HashMap<String, String>)msg.obj).get("title"));
                    try {
                        it.putExtra(Intent.EXTRA_TEXT, URLDecoder.decode(((HashMap<String, String>)msg.obj).get("body"),"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    it.setType("text/plain");
                    startActivity(Intent.createChooser(it, "Choose Email Client"));
                    break;

                case MAKE_ACALL:
                    Log.d(TAG, "MAKE_ACALL obj=" + msg.obj );
                    uri = Uri.parse("tel:" + ((HashMap<String, String>)msg.obj).get("phone_number"));
                    it = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(it);
                    break;
                case UPLOAD_PROFILE:
                    Log.d(TAG, "UPLOAD_PROFILE obj=" + msg.obj );
                    mUploadUrl = ((HashMap<String, String>)msg.obj).get("action_url");
                    //((HashMap<String, String>)msg.obj).get("enctype");
                    mCallbackMethod = ((HashMap<String, String>)msg.obj).get("callback");
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLARY_CODE);
                    break;
            }
        }
    };

    private String mUploadUrl = null;
    private String mCallbackMethod = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GALLARY_CODE:
                    Log.d(TAG,"REQUEST_GALLARY_CODE intent="+data);
                    Log.d(TAG,"imagePath="+getImagePath(data));
                    new HttpUploadTask().execute(mUploadUrl, getImagePath(data));

                    break;
                default:
                    break;
            }
        }
    }
    private String getImagePath(Intent data){
        Uri selPhotoUri = data.getData();
        Cursor c = getContentResolver().query(Uri.parse(selPhotoUri.toString()), null,null,null,null);
        c.moveToNext();
        return c.getString(c.getColumnIndex( MediaStore.MediaColumns.DATA));
    }

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
            //kakaoTalkLinkMessageBuilder.addWebLink(link, URLDecoder.decode(link,"UTF-8"));
            // 웹싸이트에 등록된 kakao<app_key>://kakaolink로 이동
//                kakaoTalkLinkMessageBuilder.addAppButton(getString(R.string.kakaolink_appbutton), new AppActionBuilder()
//                        .addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder().setExecuteParam("execparamkey2=2222").setMarketParam("referrer=kakaotalklink").build())
//                        .addActionInfo(AppActionInfoBuilder.createiOSActionInfoBuilder(AppActionBuilder.DEVICE_TYPE.PHONE).setExecuteParam("execparamkey2=2222").build())
//                        .setUrl("http://www.kakao.com").build());
                // 웹싸이트에 등록한 "http://www.kakao.com"으로 이동.
            kakaoTalkLinkMessageBuilder.addWebButton("연결", link);
            kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder, this);
        } catch (KakaoParameterException e) {
            Log.d(TAG,e.getMessage());
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            Log.d("HybridApp",e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview = (WebView) findViewById(R.id.webview);
        initWebView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            CookieSyncManager.getInstance().startSync();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            CookieSyncManager.getInstance().stopSync();
        }
    }

    private String getActionUrl(){
        String url = null;
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            url = extras.get("action_url").toString();
        } else {
            url = "http://www.checkhall.com/member/login.jsp";
        }
        Log.d("LCheckhall","MainActivity/getActionUrl() url="+url);
        return url;
    }

    private void initWebView(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            CookieSyncManager.createInstance(this);
        }
        webview.setWebViewClient(new WishWebViewClient ());
        webview.setWebChromeClient(new WishWebChromeClient());
        WebSettings webSettings = webview.getSettings();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new AndroidBridge(), "HybridApp");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        Log.i(TAG,"initWebView loadUrl=" + getActionUrl());
        webview.loadUrl(getActionUrl());
    }

    private boolean isLastPag(){
        Log.d(TAG, "isLastPag url=" + webview.getUrl());
        if( webview.getUrl().endsWith("/plan/")
                || webview.getUrl().endsWith("/hall/")
                || webview.getUrl().endsWith("/hall/index.jsp")
                || webview.getUrl().endsWith(".com/")
                || webview.getUrl().endsWith("/index.jsp")
                || webview.getUrl().endsWith("/plan/index.jsp")
                || webview.getUrl().endsWith("/member/login.jsp")
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
                                Log.d(TAG,"TokenId register param - " + data);
                                connection.setDoOutput(true);
                                connection.getOutputStream().write(data.getBytes());
                                if (connection.getResponseCode() == 200) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                                    DeviceUtil.setLogined(MainActivity.this);
                                    Log.d(TAG,"TokenId register Success - " + connection.getResponseCode() + "," + reader.readLine());
                                } else {
                                    Log.d(TAG,"TokenId register fail - " + connection.getResponseCode() + ", "+ connection.getResponseMessage());
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                Log.e(TAG,e.getMessage());
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG,e.getMessage());
                            }
                        }
                    });
                    Log.d(TAG, "server.idx = " + jobj.getString("idx"));
                    DeviceUtil.setUserIdx(MainActivity.this, jobj.getString("idx"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onConsoleMessage JSONException " + e.getMessage());
                }
            }
            Log.d(TAG, cm.message() + " -- From line "
                    + cm.lineNumber() + " of "
                    + cm.sourceId() );
            return true;
        }
    }

    private class WishWebViewClient extends WebViewClient {
       @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

            Log.d(TAG, "onReceivedError ["+error.getErrorCode()+"]"+ error.getDescription().toString() );
            if(error.getErrorCode() < 0){
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                alert.setPositiveButton("재시도", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        webview.reload();
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setNegativeButton("앱종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                });
                alert.setMessage("인터넷 연결상태를 확인후 재시도 해주세요");
                alert.show();
            }
            super.onReceivedError(view, request, error);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            Log.d(TAG,"shouldOverrideUrlLoading url="+url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            /* NOTICE 로그인을 한 다음 앱을 종료하고, 다시 앱을 실행했을 때 간헐적으로 로그인이 안 된 상태가 된다.
             이는 웹뷰의 RAM과 영구 저장소 사이에 쿠키가 동기화가 안 되어 있기 때문이다. 따라서 강제로 동기화를 해준다. */
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //noinspection deprecation
                CookieSyncManager.getInstance().sync();
            } else {
                // 롤리팝 이상에서는 CookieManager의 flush를 하도록 변경됨.
                CookieManager.getInstance().flush();
            }
            super.onPageFinished(view, url);
        }
    }

    private class AndroidBridge {
        private void sendHandMessage(int what, Object obj) {
            Message message = mHandler.obtainMessage();
            message.what = what;
            message.obj = obj;
            mHandler.sendMessage(message);
        }

        @JavascriptInterface
        public void sendKakao(final String type, final String title, final String imageUrl, final String link) {
            Log.d(TAG, "sendKakao(" + type + "," + title + ", " + imageUrl + "," + link + ")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("type", type);
            paramMap.put("title", title);
            paramMap.put("imageUrl", imageUrl);
            paramMap.put("link", link);
            sendHandMessage(SEND_KAKAO, paramMap);
        }

        @JavascriptInterface
        public void sendSms(final String mobile_no, final String body) {
            Log.d(TAG, "sendSms(" + mobile_no + "," + body + ")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("mobile_no", mobile_no);
            paramMap.put("body", body);
            sendHandMessage(SEND_SMS, paramMap);
        }

        @JavascriptInterface
        public void sendEmail(final String email, final String title, final String body) {
            Log.d(TAG, "sendEmail(" + email + ", " + title + ", " + body + ")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("email", email);
            paramMap.put("title", title);
            paramMap.put("body", body);
            sendHandMessage(SEND_EMAIL, paramMap);
        }

        @JavascriptInterface
        public void makeACall(final String phone_number) {
            Log.d(TAG, "makeACall(" + phone_number + ")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("phone_number", phone_number);
            sendHandMessage(MAKE_ACALL, paramMap);
        }

        @JavascriptInterface
        public void uploadFile(final String action_url, final String enctype, final String callback) {
            Log.d(TAG, "uploadFile(" + action_url + ", " + enctype + ", " + callback + ")");
            HashMap<String, String> paramMap = new HashMap();
            paramMap.put("action_url", action_url);
            paramMap.put("enctype", enctype);
            paramMap.put("callback", callback);
            sendHandMessage(UPLOAD_PROFILE, paramMap);
        }
    }
    private class HttpUploadTask extends  AsyncTask<String, Void, String> {

        final private String lineEnd = "\r\n";
        final private String twoHyphens = "--";
        final private String boundary = "*****";

        public String HttpFileUpload(String urlString, String fileName) {
            String result = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(fileName);
                URL connectUrl = new URL(urlString);
                Log.d(TAG, "HttpFileUpload / fileInputStream  is " + fileInputStream);

                // open connection
                HttpURLConnection conn = (HttpURLConnection)connectUrl.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                // write data
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName+"\"" + lineEnd);
                dos.writeBytes(lineEnd);

                int bytesAvailable = fileInputStream.available();
                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                byte[] buffer = new byte[bufferSize];
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                Log.d(TAG, "Image bytesAvailable="+bytesAvailable);

                // read image
                int writedSize = 0;
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    writedSize += bufferSize;
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close streams
                Log.d(TAG , "File is written complete size = "+writedSize);
                fileInputStream.close();
                dos.flush(); // finish upload...

                // get response
                int ch;
                InputStream is = conn.getInputStream();
                StringBuffer b =new StringBuffer();
                while( ( ch = is.read() ) != -1 ){
                    b.append( (char)ch );
                }
                result =new String(b.toString().getBytes(), "utf-8");
                Log.d(TAG, "result = " + result);
                dos.close();
            } catch (Exception e) {
                Log.e(TAG, "exception " + e.getMessage());
            }
            return result;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG,"doInBackground strings.length="+strings.length);
            Log.d(TAG,"doInBackground strings[0]"+strings[0]);
            Log.d(TAG,"doInBackground strings[1]"+strings[1]);
            return HttpFileUpload(strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG,"onPostExecute s="+s.trim());
            //결과가 성공여부인지도 따져야 한다.
            try {
                JSONObject jsonObj = new JSONObject(s);
                if( jsonObj.get("result").equals("Y")){
                    Log.d(TAG, "onPostExecute result sucess call javascript:"+mCallbackMethod);
                    webview.loadUrl("javascript:"+mCallbackMethod);
                } else {
                    AlertUtil.showAlert(MainActivity.this, "이미지 등록 실패 ("+jsonObj.get("resultMsg")+")");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                AlertUtil.showAlert(MainActivity.this, "이미지 등록 실패 (json parser exception)");
            }
            // 업로드 완료후 의 액션인...javascript를 호출해주면 된다.
            super.onPostExecute(s);
        }
    }
}



