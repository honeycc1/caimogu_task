package com.github.shy526.caimogu;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.shy526.okhttp.OkHttpHelp;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GamerskyHelp {


    /**
     * 获取游民星空众评的调用参数
     *
     * @param gameName
     * @return
     */
    public static String getContentUrlByGameName(String gameName) {
        String url = String.format("https://so.gamersky.com/all/ku?s=%s", gameName);

        //获取目标页面url
        String targetUrl = OkHttpHelp.getParse(url, (Document doc, Headers headers) -> {
            String result = "";
            Element block = doc.select("div.Mid2_L>.ImgY.contentpaging").first();
            if (block == null) {
                return result;
            }

            Elements lis = block.select("li");
            String gameNameTemp = gameName.replaceAll("\\s+", "").toUpperCase();

            for (Element li : lis) {
                Element img = li.select("img[title]").first();
                if (img == null) {
                    continue;
                }
                String title = img.attr("title");
                String titleTemp = title.replaceAll("\\s+", "").toUpperCase();
                gameNameTemp = gameNameTemp.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", "");
                Element aTag = li.select("a[href]").first();
                if (aTag == null) {
                    continue;
                }
                String href = aTag.attr("href");
                String[] split = href.split("/");
                String enName = split[split.length - 1].replaceAll("\\s+", "").toUpperCase().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", "");
                boolean enFlag = gameNameTemp.matches("[A-Za-z0-9]+");
                boolean temp = enFlag ? enName.equals(gameNameTemp) : titleTemp.equals(gameNameTemp);

                if (temp) {

                    URL hrefUrl;
                    try {
                        hrefUrl = new URL(href);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("链接地址解析失败", e);
                    }

                    String host = hrefUrl.getHost();
                    if ("ku.gamersky.com".equals(host)) {
                        result = href;
                        return result;
                    }
                }
            }
            return result;
        }, 1);
        if (targetUrl == null || targetUrl.isEmpty()) {
            return "";
        }
        String gameId = OkHttpHelp.getParse(targetUrl, (Document doc, Headers headers) ->
                {
                    return doc.select("div.Mid>div.Mid_GN div[gameid]").attr("gameid");
                },
                1);
        if (gameId == null || gameId.isEmpty()) {
            return "";
        }
        return String.format("%s?gsGameId=%s", targetUrl, gameId);
    }

    /**
     * 获取一百条评论
     *
     * @param contentUrl
     * @return
     */
    public static List<String> getComment(String contentUrl) {
        String jsonFormat = "{\n" +
                "    \"commentFiltrateParams\": {\n" +
                "        \"userLabelTypes\": [\n" +
                "        ]\n" +
                "    },\n" +
                "    \"contentUrl\": \"%s\",\n" +
                "    \"exposeTimes\": 0,\n" +
                "    \"isFilterConditionShow\": true,\n" +
                "    \"listName\": \"评论列表\",\n" +
                "    \"order\": \"tuiJian\",\n" +
                "    \"pageIndex\": %s,\n" +
                "    \"pageSize\": 20,\n" +
                "    \"projectId\": 0,\n" +
                "    \"turn\": 0,\n" +
                "    \"version\": \"2.0\"\n" +
                "}";


        String url = "https://router5.gamersky.com/@/gameScoreDetailPage/index/6.0.0/0";
        List<String> comments = new ArrayList<>();
        for (int pageIndex = 0; comments.size() < 100; pageIndex++) {
            String jsonStr = String.format(jsonFormat, contentUrl, pageIndex);

            boolean flag = OkHttpHelp.postJsonBodyParse(url, jsonStr, (JSONObject json, Headers headers) -> {
                JSONArray jsonArray = json.getJSONArray("listElements");
                if (jsonArray.isEmpty()) {
                    return true;
                }
                for (Object itemObj : jsonArray) {
                    JSONObject jsonItem = (JSONObject) itemObj;
                    String comment = jsonItem.getString("description");
                    if (comment != null && comment.length() > 10) {
                        comment = comment.replaceAll("<br/>", "\n");
                        comment = comment.replaceAll("<[^>]+>", "");
                        comment = comment.replaceAll("\\s+", "");
                        comment = comment.replaceAll("&nbsp;", "");
                        comments.add(comment);
                    }
                }
                return false;
            }, 0);
            if (flag) {
                break;
            }
        }
        return comments;
    }

    public static List<String> getCommentsByGameName(String gameName) {
        String target = GamerskyHelp.getContentUrlByGameName(gameName);
        if (target == null || target.isEmpty()) {
            return new ArrayList<>();
        }
        return GamerskyHelp.getComment(target);

    }
}
