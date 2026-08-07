package com.github.shy526.okhttp;

import com.github.shy526.config.Config;
import com.github.shy526.vo.UserInfo;
import com.github.shy526.vo.UserInfo2;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class UserAgentInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36 Edg/151.0.0.0");
        HttpUrl url = original.url();
        UserInfo2 userInfo = Config.INSTANCE.userInfo;
        if (userInfo != null) {
            if (url.host().equals("www.caimogu.cc")) {
                requestBuilder.addHeader("x-requested-with","XMLHttpRequest");
                if (!url.encodedPath().equals("/game/find.html")){
                    Headers headers = original.headers();
                    List<String> cookies = headers.values("cookie");
                    String newCookie=String.join(";", cookies);
                    if (cookies.isEmpty()){
                        newCookie =  userInfo.getToken();
                    }else {
                        newCookie += ";" + userInfo.getToken();
                    }
                    requestBuilder.addHeader("cookie", newCookie);
                }
            }

        }

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
