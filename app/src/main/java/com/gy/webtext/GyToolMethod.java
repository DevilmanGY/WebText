package com.gy.webtext;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by 39340 on 2017/1/31.
 */

public class GyToolMethod {
    /**
     * 回调接口以供sendHttpGetRequest，sendHttpPostRequest使用
     */
    public interface HttpCallbackListener {
        //接口还是在子线程中执行，若要更新UI需要在主线程异步处理
        void onFinish(String response);

        void onError(Exception e);
    }

    /**
     * 请求访问Http协议，onFinish中返回访问字符串并可用异步处理方式更新UI
     * ··形参有:
     * ··1.URL地址(String) 2.回调对象(HttpCallbackListener)
     * @param address
     * @param listener
     */
    public static void sendHttpGetRequest(
            final String address,
            final HttpCallbackListener listener
    ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    //利用URL获取HttpURLConnection对象
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();

                    //配置connection
                    connection.setRequestMethod("GET");
                        /*
                        get机制也可传递数据，用的是在URL地址里面通过？号间隔，
                        然后以name=value的形式给客户端传递参数。
                        例如（http://www.baidu.com/s?w=%C4&inputT=2710）
                        */
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    /*
                     * 添加Header，告诉服务端一些信息，比如读取某个文件的多少字节到多少字节，是不是可以压缩传输，
                     * 客户端支持的编码格式，客户端类型，配置，需求等。
                     */
                    //connection.setRequestProperty("Connection","Keep-Alive"); // 维持长连接
                    //connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

                    //利用connection发出请求并获取输入流
                    InputStream in = connection.getInputStream();

                    // 服务器响应code，200表示请求成功并返回
                    int code = connection.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        return;
                    }

                    //解析输入流
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(in)
                    );
                    String line;
                    StringBuilder parseString = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        parseString.append(line);
                    }
                    bufferedReader.close();

                    //调用回调方法“输出”解析字符
                    listener.onFinish(parseString.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onError(e);
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
            }
        }).start();
    }

    /**
     * 向Http地址中传入数据，并在onFinish中返回传入Map格式化后的字符串，
     * ··形参有:
     * ··1.URL地址(String) 2编码方式(String)
     * ..3.参数集合(Map<String,String>) 4.回调对象(HttpCallbackListener)
     * @param address
     * @param codeSystem
     * @param params
     * @param listener
     */
    public static void sendHttpPostRequest(
            final String address,
            final String codeSystem,
            final Map<String, String> params,
            final HttpCallbackListener listener
    ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    //利用URL获取HttpURLConnection对象
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();

                    //配置connection
                    connection.setRequestMethod("POST");
                        /*
                        post传输方式不在URL里传递，也正好解决了get传输量小、容易篡改及不安全等一系列不足。
                        主要是通过对HttpURLConnection的设置，让其支持post传输方式，然后在通过相关属性传递参数
                        （若需要传递中文字符，则可以通过URLEncoder编码，而在获取端采用URLDecoder解码即可）
                        */
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    //处理参数为指定格式的String
                    StringBuilder paramStr = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (!TextUtils.isEmpty(paramStr)) {
                            paramStr.append("&");
                        }

                        //若需要传递中文字符，则可以通过URLEncoder编码，而在获取端采用URLDecoder解码即可
                        // 并以codeSystem方式编码
                        paramStr.append(URLEncoder.encode(entry.getKey(), codeSystem)
                                + "="
                                + URLEncoder.encode(entry.getValue(), codeSystem)
                        );
                    }

                    //写入处理参数
                    OutputStream out = connection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            new OutputStreamWriter(out, codeSystem)
                    );
                    bufferedWriter.write(paramStr.toString());
                    bufferedWriter.flush();
                    bufferedWriter.close();

                    //调用回调方法后期处理，并传入写入服务器的字符串参数
                    listener.onFinish(paramStr.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onError(e);
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
            }
        }).start();
    }
}