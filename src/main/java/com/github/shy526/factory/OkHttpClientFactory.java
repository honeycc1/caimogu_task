package com.github.shy526.factory;

import com.github.shy526.okhttp.TrustAllCerts;
import com.github.shy526.okhttp.TrustAllHostnameVerifier;
import com.github.shy526.okhttp.UserAgentInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;

@Slf4j
public class OkHttpClientFactory {
    private static volatile OkHttpClientFactory instance;
    private final OkHttpClient client;

    private OkHttpClientFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
        }
        if (ssfFactory == null) {
            log.error("OkHttp init error ssfFactory is null ");
            client = new OkHttpClient.Builder().addInterceptor(new UserAgentInterceptor()).build();
            return;
        }
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor())
                .sslSocketFactory(ssfFactory, new TrustAllCerts())
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .build();

    }

    public static OkHttpClientFactory getInstance() {
        if (instance == null) {
            synchronized (OkHttpClientFactory.class) {
                if (instance == null) {
                    instance = new OkHttpClientFactory();
                }
            }
        }
        return instance;
    }

    public OkHttpClient getClient() {
        return client;
    }
}
