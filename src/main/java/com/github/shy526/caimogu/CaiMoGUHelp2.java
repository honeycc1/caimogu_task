package com.github.shy526.caimogu;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.shy526.config.Config;
import com.github.shy526.factory.OkHttpClientFactory;
import com.github.shy526.okhttp.OkHttpHelp;
import com.github.shy526.vo.UserInfo2;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class CaiMoGUHelp2 {


    public static UserInfo2 login(String username, String password) {
        AtomicReference<String> tempTokenWrap = new AtomicReference<>("");
        String token = OkHttpHelp.getParse("https://www.caimogu.cc/login.html", (Document doc, Headers headers) -> {
            String cookie = headers.get("set-cookie");
            tempTokenWrap.set(cookie.split(";")[0]);
            return doc.select("input[name=__token__]").attr("value");
        }, 1);

        FormBody formBody = new FormBody.Builder()
                .add("account", username)
                .add("password", Base64.encodeBase64String(password.getBytes()))
                .add("remember", "1")
                .add("site", "")
                .add("__token__", token)
                .build();

        Request request = new Request.Builder()
                .url("https://www.caimogu.cc/login")
                .post(formBody)
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("Host", "www.caimogu.cc")
                .addHeader("cookie", tempTokenWrap.get())
                .build();


        UserInfo2 userInfo = OkHttpHelp.executeRequestParse(request, (JSONObject json, Headers headers) -> {
            Integer code = json.getInteger("status");
            if (code.equals(1)) {
                UserInfo2 tempUserInfo = new UserInfo2();
                String nickname = json.getJSONObject("data").getString("nickname");
                tempUserInfo.setNickname(nickname);
                List<String> cookieStrList = headers.values("Set-Cookie");
                cookieStrList.stream()
                        .filter(cookie -> cookie.startsWith("cmg_token="))
                        .findFirst().ifPresent(cookie -> {
                            String cmgToken = cookie.split(";")[0];
                            tempUserInfo.setToken(cmgToken);
                        });
                return tempUserInfo;
            }
            return null;
        }, 0);
        if (userInfo == null) {
            return null;
        }
        Config.INSTANCE.userInfo = userInfo;
        return userInfo;
    }

    public static void fillUserInfo(UserInfo2 userInfo) {
        String url = "https://www.caimogu.cc/";
        String uid = OkHttpHelp.getParse(url, (Document doc, Headers h) -> {
            return doc.select("div.main-container > div.left > input[name=user_id]").attr("value");
        }, 1);
        userInfo.setUid(uid);
        Integer userPoint = getUserPoint();
        userInfo.setPoint(userPoint);
    }

    public static Integer getUserPoint() {
        String url = String.format("https://www.caimogu.cc/user/my/wallet/info?type=center");
        return OkHttpHelp.getParse(url, (JSONObject json, Headers h) -> {
            if (json.getInteger("status").equals(1)) {
                JSONObject data = json.getJSONObject("data");
                return data.getInteger("point");
            }
            return 0;
        }, 0);
    }

    public static List<String> checkGamePoint() {
        String urlFormat = "https://www.caimogu.cc/user/my/wallet/list?act=point&page=%s";
        List<String> acIds = new ArrayList<>();
        boolean flag = true;
        //拆分被刪除的遊戲評論
        for (int page = 1; flag; page++) {
            String url = String.format(urlFormat, page);
            flag = OkHttpHelp.getParse(url, (JSONObject json, Headers h) -> {
                if (json.getInteger("status").equals(1)) {
                    JSONObject data = json.getJSONObject("data");
                    JSONArray jsonArray = data.getJSONArray("list");
                    if (jsonArray.isEmpty()) {
                        return false;
                    }
                    for (Object itemObj : jsonArray) {
                        JSONObject item = (JSONObject) itemObj;
                        String type = item.getString("type");
                        if ("12".equals(type)) {
                            acIds.add(item.getString("name_id"));
                        }
                    }
                    return true;
                }
                return false;
            }, 0);
        }

        return acIds;
    }


    public static List<String> scanGameIds() {
        String urlFormat = "https://www.caimogu.cc/game/find.html?act=fetch&score=%s&kw=&platforms=&tags=&status=0&sort=2&sort_desc=1&page=%s";
        List<String> gameIds = new ArrayList<>();
        for (float score = 100; score >= 0; score--) {
            boolean flag = true;
            AtomicInteger gameNumber = new AtomicInteger();
            String scoreStr = String.format("%.1f", score / 10);
            for (int page = 1; flag; page++) {
                String url = String.format(urlFormat, scoreStr, page);
                flag = OkHttpHelp.getParse(url, (JSONObject json, Headers headers) -> {
                    if (json.getInteger("status").equals(1)) {
                        JSONArray data = json.getJSONArray("data");
                        if (data.isEmpty()) {
                            return false;
                        }
                        for (Object itemObj : data) {
                            JSONObject item = (JSONObject) itemObj;
                            if ("0000-00-00".equalsIgnoreCase(item.getString("selling_time"))) {
                                continue;
                            }
                            String id = item.getString("id");
                            gameIds.add(id);
                        }
                        gameNumber.addAndGet(data.size());
                        return true;
                    }
                    return false;
                }, 0);
            }
            log.error("评分:{},数量:{}", scoreStr, gameNumber.get());
        }

        return gameIds;
    }

    public static JSONObject getGameNameByGameId(String gameId) {
        String urlFormat = "https://www.caimogu.cc/game/%s.html";
        String url = String.format(urlFormat, gameId);
        JSONObject result = new JSONObject();
        String cn = OkHttpHelp.getParse(url, (Document doc, Headers headers) -> Objects.requireNonNull(doc.select("div.title-share > div.title").first()).text(), 1);
        String en = OkHttpHelp.getParse(url, (Document doc, Headers headers) -> Objects.requireNonNull(doc.select("div.base-info > div.base > .title-en").first()).text(), 1);
        result.put("cn", cn);
        result.put("en", en);
        return result;
    }

    public static int acSore(String id, String comment) {
        OkHttpClient client = OkHttpClientFactory.getInstance().getClient();
        FormBody formBody = new FormBody.Builder()
                .add("id", id.toString())
                .add("type", "2")
                .add("score", "10")
                .add("content", "神中神非常好玩")
                .build();
        Request request = new Request.Builder()
                .url("https://www.caimogu.cc/game/act/score") // 测试API，可替换为实际接口
                .post(formBody).build();
        AtomicInteger flag = new AtomicInteger();
        OkHttpHelp.executeRequestParse(request, (JSONObject json, Headers headers) -> {
            if (json.getInteger("status").equals(1)) {
                flag.set(0);
            } else {
                String info = json.getString("info");
                log.error("{}:{}", Config.INSTANCE.userInfo.getNickname(), json.getString("info"));
                flag.set(1);
                if (info.equals("您的账户已被禁言")) {
                    flag.set(2);
                }
            }
            return null;
        }, 0);
        return flag.get();
    }

}
