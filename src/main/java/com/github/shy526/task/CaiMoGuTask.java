package com.github.shy526.task;

import com.alibaba.fastjson2.JSONObject;
import com.github.shy526.caimogu.CaiMoGUHelp2;
import com.github.shy526.caimogu.CaiMoGuHelp;
import com.github.shy526.caimogu.GamerskyHelp;
import com.github.shy526.caimogu.XiaoHeiHe;
import com.github.shy526.config.Config;
import com.github.shy526.github.GithubHelp;
import com.github.shy526.vo.GithubInfo;
import com.github.shy526.vo.UserInfo2;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class CaiMoGuTask {
    public void run(String userName, String password, int ex) {
        GithubInfo githubInfo = Config.INSTANCE.GithubInfo;
        String ownerRepo = githubInfo.getOwnerRepo();
        String githubApiToken = githubInfo.getGithubApiToken();
        String repoPath = githubInfo.getRepoPath();

        UserInfo2 userInfo = CaiMoGUHelp2.login(userName, password);
        if (userInfo == null) {
            log.error("{} 踩蘑菇 用户名/密码错误,或者踩蘑菇接口失效",userName);
            return;
        }
        CaiMoGUHelp2.fillUserInfo(userInfo);
        log.error("当前用户:{},影响力:{}", userInfo.getNickname(), userInfo.getPoint());
        if (userInfo.getPoint() < 0) {
            log.error("帐号可能被封禁,停止任务");
            return;
        }
        ZoneId defaultZone = ZoneId.systemDefault();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
      //  LocalDate now = LocalDate.now();
       // log.error("{}:{}", defaultZone, fmt.format(LocalDateTime.now()));
        List<String> score = CaiMoGUHelp2.checkGamePoint(LocalDate.now());


        List<String> files = GithubHelp.getListFileName(ownerRepo, githubApiToken, repoPath);

        String accountInfoFileName = "accountInfo.txt";
        if (!files.contains(accountInfoFileName)) {
            GithubHelp.createOrUpdateFile(userInfo.account + "-" + userInfo.getUid() + ":" + 0, repoPath + "/" + accountInfoFileName, ownerRepo, githubApiToken);
            log.error("创建-->{}", accountInfoFileName);
        }

        Map<String, Integer> accountMap = getAccountMap(repoPath, accountInfoFileName, ownerRepo, githubApiToken);
        String key = userInfo.account + "-" + userInfo.getUid();
        if (!accountMap.containsKey(key)) {
            accountMap.put(key, 0);
            syncAccountFile(accountMap, repoPath, accountInfoFileName, ownerRepo, githubApiToken);
        }

        Integer nowEx = accountMap.get(key);
        if (nowEx >= ex && ex != -1) {
            log.error("{}-{}的积分任务已完成 {}/{}", userInfo.getAccount(), userInfo.getUid(), nowEx, ex);
            return;
        }

        if (score.size() >= 3) {
            log.error("无法获取更多影响力,积分任务完成情况: {}-{} {}/{}", userInfo.getUid(), userInfo.getNickname(), nowEx, ex);
            return;
        }



        List<String> acGames;
        List<String> caiMoGuGameIds;
        String acGameFileName = String.format("%s_acGameId.txt", userInfo.getUid());


        if (!files.contains(acGameFileName)) {
            acGames = CaiMoGUHelp2.checkGamePoint(null);
            GithubHelp.createOrUpdateFile(String.join("\n", acGames), repoPath + "/" + acGameFileName, ownerRepo, githubApiToken);
            log.error("创建-->{}-{}", acGameFileName, acGames.size());
        }

        String caiMoGuGameIdFileName = String.format("caiMoGuGameId.txt", userInfo.getUid());
        if (!files.contains(caiMoGuGameIdFileName)) {
            caiMoGuGameIds = CaiMoGUHelp2.scanGameIds();
            GithubHelp.createOrUpdateFile(String.join("\n", caiMoGuGameIds), repoPath + "/" + caiMoGuGameIdFileName, ownerRepo, githubApiToken);
            log.error("创建-->{}-{}", caiMoGuGameIdFileName, caiMoGuGameIds.size());
        }

        caiMoGuGameIds = getFileContent(repoPath, caiMoGuGameIdFileName, ownerRepo, githubApiToken);
        acGames = getFileContent(repoPath, acGameFileName, ownerRepo, githubApiToken);
        //  caiMoGuGameIds = CaiMoGuHelp.readResources(caiMoGuGameIdFileName);
        //  acGames = CaiMoGuHelp.readResources(acGameFileName);

        String commentExcludeFileName = "commentExclude.txt";
        List<String> gamerSkyExclude = getFileContent(repoPath, commentExcludeFileName, ownerRepo, githubApiToken);

        caiMoGuGameIds.removeIf(new HashSet<>(acGames)::contains);
        caiMoGuGameIds.removeIf(new HashSet<>(gamerSkyExclude)::contains);
        int succee = score.size();
        Set<String> ac = new HashSet<>();
        for (String gameId : caiMoGuGameIds) {
            //从游民星空加载评论
            JSONObject names = CaiMoGUHelp2.getGameNameByGameId(gameId);
            String cn = names.getString("cn");
            String en = names.getString("en");
            //直接忽略副标题
            en = en.split("[:：]{2}\\s")[0];
            en = en.replaceAll("(- Deluxe Edition|Deluxe Edition|- GOTY Edition|GOTY Edition|[®™])", "");
            if (en == null || en.trim().isEmpty()) {
                en = cn;
            }
            Set<String> comments1 = GamerskyHelp.getCommentsByGameName(en);
            log.error("游民星空:{}-{}", en, comments1.size());
            Set<String> comments2 = XiaoHeiHe.getCommentsByGameName(cn);
            log.error("小黑盒:{}-{}", cn, comments2.size());
            comments1.addAll(comments2);
            List<String> comments = Lists.newArrayList(comments1);
            if (comments.size() > 1) {
                int index = (int) (Math.random() * comments.size());
                int i = CaiMoGUHelp2.acSore(gameId, comments.get(index));
                if (i == 0) {
                    succee++;
                    ac.add(gameId);
                    log.error("{}:{}", cn, comments.get(index));
                } else if (i == 2) {
                    //账户被封直接退出
                    break;
                }
            } else {
                gamerSkyExclude.add(gameId);
            }
            if (succee >= 3) {
                break;
            }
        }


        GithubHelp.createOrUpdateFile(String.join("\n", gamerSkyExclude), repoPath + "/" + commentExcludeFileName, ownerRepo, githubApiToken);
        log.error("同步-->{}-{}", commentExcludeFileName, gamerSkyExclude.size());


        acGames.addAll(ac);
        GithubHelp.createOrUpdateFile(String.join("\n", acGames), repoPath + "/" + acGameFileName, ownerRepo, githubApiToken);
        log.error("同步-->{}-{}", acGameFileName, acGames.size());


        Integer userPoint = CaiMoGUHelp2.getUserPoint();
        log.error("本次任务获取: {}-{} - {}", userInfo.getUid(), userInfo.getNickname(), userPoint - userInfo.getPoint());
        nowEx += userPoint - userInfo.getPoint();
        accountMap.put(key, nowEx + userPoint - userInfo.getPoint());
        syncAccountFile(accountMap, repoPath, accountInfoFileName, ownerRepo, githubApiToken);
        log.error("积分任务完成情况: {}-{} {}/{}", userInfo.getUid(), userInfo.getNickname(), nowEx, ex);
        Config.INSTANCE.userInfo = null;
    }

    private static void syncAccountFile(Map<String, Integer> accountMap, String repoPath, String accountInfoFileName, String ownerRepo, String githubApiToken) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> item : accountMap.entrySet()) {
            sb.append(item.getKey()).append(":").append(item.getValue()).append("\n");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        GithubHelp.createOrUpdateFile(sb.toString(), repoPath + "/" + accountInfoFileName, ownerRepo, githubApiToken);
        log.error("同步-->{}-{}", accountInfoFileName, accountMap.entrySet().size());
    }

    private static Map<String, Integer> getAccountMap(String repoPath, String accountInfoFileName, String ownerRepo, String githubApiToken) {
        Map<String, Integer> result = new HashMap<>();
        List<String> tempList = getFileContent(repoPath, accountInfoFileName, ownerRepo, githubApiToken);
        for (String tempItem : tempList) {
            String[] temp = tempItem.split(":");
            if (temp.length == 2) {
                result.put(temp[0], Integer.parseInt(temp[1]));
            }
        }
        return result;
    }

    private static List<String> getFileContent(String repoPath, String caiMoGuGameIdFileName, String ownerRepo, String githubApiToken) {
        JSONObject content = GithubHelp.getContent(repoPath + "/" + caiMoGuGameIdFileName, ownerRepo, githubApiToken);
        List<String> result = Lists.newArrayList();
        if (content.isEmpty()) {
            return result;
        }
        byte[] contents = Base64.decodeBase64(content.getString("content"));
        String content2 = new String(contents, StandardCharsets.UTF_8);
        String[] split = content2.split("\n");

        if (split.length > 0) {
            result = Lists.newArrayList(split);
        }
        return result;
    }
}
