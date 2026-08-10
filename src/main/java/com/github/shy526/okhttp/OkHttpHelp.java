package com.github.shy526.okhttp;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.shy526.factory.OkHttpClientFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.function.BiFunction;

@Slf4j
public class OkHttpHelp {


    public static <T, K> T getParse(String url, BiFunction<K, Headers, T> success, int resultType) {
        Request request = new Request.Builder().url(url).build();

        return executeRequestParse(request, success, resultType);
    }

    public static <T, K> T postJsonBodyParse(String url, String jsonStr, BiFunction<K, Headers, T> success, int resultType) {

        RequestBody requestBody = RequestBody.create(jsonStr, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();


        return executeRequestParse(request, success, resultType);
    }

    /**
     * 统一执行请求处理器
     *
     * @param request
     * @param success
     * @param <T>
     * @return
     */
    public static <T, R> T executeRequestParse(Request request, BiFunction<R, Headers, T> success, int resultType) {
        OkHttpClient client = OkHttpClientFactory.getInstance().getClient();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("{}:{}", request.toCurl(), response.code());
            }
            ResponseBody body = response.body();
            R result = null;
            if (resultType == 0) {
                result = (R) jsonProcessor(body);
            } else if (resultType == 1) {
                result = (R) documentProcessor(body);
            }
            Headers headers = response.headers();
            try {
                return (T) success.apply(result, headers);
            } catch (Exception e) {
                log.error("success处理异常:{}", e.getMessage());
                log.error(e.getMessage(), e);
            }


        } catch (Exception ex) {
            log.error("{}:{}", request.toCurl(), ex.getMessage());
        }
        return null;
    }

    /**
     * 将字符串处理为json
     *
     * @param responseBody
     * @return
     */
    private static JSONObject jsonProcessor(ResponseBody responseBody) throws IOException {
        JSONObject result = null;

        if (responseBody != null) {
            String bodyStr = responseBody.string();
            if (bodyStr != null && !bodyStr.isEmpty()) {
                result = JSON.parseObject(bodyStr);
            }
        }

        return result;
    }

    /**
     * 将字符串处理为doc
     *
     * @param responseBody
     * @return
     */
    private static Document documentProcessor(ResponseBody responseBody) throws IOException {
        Document result = null;

        if (responseBody != null) {
            String bodyStr = responseBody.string();
            if (bodyStr != null && !bodyStr.isEmpty()) {
                result = Jsoup.parse(bodyStr);
            }
        }

        return result;
    }

}
