package com.ej.hgj.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ej.hgj.entity.login.MiniProSession;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class WechatMiniProUtils {

    public WechatMiniProUtils() {
    }
    public static MiniProSession jscode2session(String appId, String appSecret, String jscode) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        String requestUrl = "https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JSCODE&grant_type=authorization_code".replace("APPID", appId).replace("SECRET", appSecret).replace("JSCODE", jscode);
        JSONObject jsonObject = httpRequest(requestUrl, "GET", (String)null);
        return convert2MiniProSession(jsonObject);
    }

    private static MiniProSession convert2MiniProSession(JSONObject jsonObject) {
        MiniProSession miniProSession = new MiniProSession();
        miniProSession.setOpenid(jsonObject.getString("openid"));
        miniProSession.setSessionKey(jsonObject.getString("session_key"));
        miniProSession.setUnionid(jsonObject.getString("unionid"));
        return miniProSession;
    }

    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        TrustManager[] tm = new TrustManager[]{new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init((KeyManager[])null, tm, new SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection)url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setRequestMethod(requestMethod);
            if ("GET".equalsIgnoreCase(requestMethod)) {
                httpUrlConn.connect();
            }

            OutputStream outStream;
            if (null != outputStr) {
                outStream = httpUrlConn.getOutputStream();
                outStream.write(outputStr.getBytes("UTF-8"));
                System.out.println(outputStr);
                outStream.close();
            }

            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;

            while((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        return jsonObject;
    }

}
