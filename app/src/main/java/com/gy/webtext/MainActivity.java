package com.gy.webtext;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            textView.setText((String)msg.obj);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        GyToolMethod.sendHttpPostRequest("http://www.baidu.com", "UTF-8", null, new GyToolMethod.HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Message message = new Message();
                message.obj = response;
                handler.sendMessage(message);






            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
