package org.whispersystems.textsecuregcm.sms;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SmsUtils {

    private static final Logger logger         = LoggerFactory.getLogger(SmsUtils.class);
    //编码格式。发送编码格式统一用UTF-8
    private static String ENCODE_FORMAT = "UTF-8";
    //智能匹配模版发送接口的http地址
    private static String SEND_SMS_ALL_URL = "https://sms.yunpian.com/v2/sms/single_send.json";
    //APIkey
    private static String API_KEY ="4ecd2c9e8894049219643f4435180a12";
    /**
     * 调取发送短信验证码
     * @param mobile 手机号
     * @param clientType 短信模板类型 1国内短信模板，2国际短信模板
     * @param verificationCode 验证码
     * @return
     */
    public static String sendCode(String mobile, Optional<String> clientType, String verificationCode) {
        Map<String, String> params = new HashMap<String, String>();
        String message=null;

        if ("ios".equals(clientType.orElse(null))) {
            message= String.format(SmsSender.SMS_IOS_VERIFICATION_TEXT, verificationCode, verificationCode);
        } else if ("android-ng".equals(clientType.orElse(null))) {
            message=String.format(SmsSender.SMS_ANDROID_NG_VERIFICATION_TEXT, verificationCode);
        } else {
            message=String.format(SmsSender.SMS_VERIFICATION_TEXT, verificationCode);
        }


//        message="【茜雅】您的验证码是"+code;
        message="[ya] Your Signal verification code: %s";
        message=String.format(message, verificationCode);
        logger.info("----sendCode--mobile={} message={}", mobile, message);
        params.put("apikey", API_KEY);
        params.put("text", message);
        params.put("mobile", mobile);
        return postYunpian(SEND_SMS_ALL_URL, params);
    }

    /**
     * 通过请求云片发送短信
     * @param url post请求URL
     * @param paramsMap 请求参数
     * @return
     */
    public static String postYunpian(String url, Map<String, String> paramsMap) {
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        try {
            HttpPost httpPost = new HttpPost(url);
            if (paramsMap != null) {
                List<NameValuePair> paramList = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> param : paramsMap.entrySet()) {
                    NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
                    paramList.add(pair);
                }
                httpPost.setEntity(new UrlEncodedFormEntity(paramList, ENCODE_FORMAT));
            }

            response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseText = EntityUtils.toString(entity);
                logger.info("Send message successed, responseText message : {}", responseText);
                return responseText;
            }
        } catch (Exception e) {
            logger.info("Send message failed, error message : {}", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (Exception e) {
                logger.info("Close response error, error message : {}", e.getMessage());
                e.printStackTrace();
            }
        }

        return null;
    }
}
