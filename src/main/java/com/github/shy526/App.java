package com.github.shy526;


import com.alibaba.fastjson2.JSONObject;
import com.github.shy526.caimogu.CaiMoGUHelp2;
import com.github.shy526.caimogu.CaiMoGuHelp;
import com.github.shy526.caimogu.GamerskyHelp;
import com.github.shy526.config.Config;
import com.github.shy526.github.GithubHelp;
import com.github.shy526.vo.GithubInfo;
import com.github.shy526.vo.UserInfo;
import com.github.shy526.vo.UserInfo2;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Hello world!
 */
@Slf4j
public class App {
    public static void main(String[] args) {
        log.error("启动踩蘑菇获取影响力任务");
        String githubApiToken = System.getenv("MY_GITHUB_API_TOKEN");
        String ownerRepo = System.getenv("OWNER_REPO");
        String userName = System.getenv("CMG_NAME");
        String password = System.getenv("CMG_PASSWORD");


        GithubInfo githubInfo = new GithubInfo();
        githubInfo.setGithubApiToken(githubApiToken);
        githubInfo.setOwnerRepo(ownerRepo);

        Config.INSTANCE.GithubInfo = githubInfo;
        if (ownerRepo == null || ownerRepo.trim().isEmpty()) {
            log.error("OWNER_REPO 未设置");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            log.error("CMG_PASSWORD 未设置");
            return;
        }
        if (userName == null || userName.trim().isEmpty()) {
            log.error("CMG_NAME 未设置");
            return;
        }

        if (githubApiToken == null || githubApiToken.trim().isEmpty()) {
            log.error("MY_GITHUB_API_TOKEN 未设置");
            return;
        }
        log.error("配置设置未缺失");
        UserInfo2 userInfo = CaiMoGUHelp2.login(userName, password);
        if (userInfo == null) {
            log.error("踩蘑菇 用户名/密码错误,或者踩蘑菇接口失效");
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
        LocalDate now = LocalDate.now();
        log.error("{}:{}", defaultZone, fmt.format(LocalDateTime.now()));
        List<String> score = CaiMoGUHelp2.checkGamePoint(LocalDate.now());
        if (score.size() >= 3) {
            log.error("无法获取更多影响力");
            return;
        }

        //;
        List<String> acGames = new ArrayList<>();
        List<String> caiMoGuGameIds = new ArrayList<>();
        String acGameFileName = String.format("%s_acGameId.txt", userInfo.getUid());
        List<String> files = GithubHelp.getListFileName(ownerRepo, githubApiToken, "src/main/resources");
        if (!files.contains(acGameFileName)) {
            acGames = CaiMoGUHelp2.checkGamePoint(null);
            GithubHelp.createOrUpdateFile(String.join("\n", acGames), acGameFileName, ownerRepo, githubApiToken);
            log.error("创建-->{}", acGameFileName);
        }

        String caiMoGuGameIdFileName = String.format("caiMoGuGameId.txt", userInfo.getUid());
        if (!files.contains(caiMoGuGameIdFileName)) {
            caiMoGuGameIds = CaiMoGUHelp2.scanGameIds();
            GithubHelp.createOrUpdateFile(String.join("\n", caiMoGuGameIds), caiMoGuGameIdFileName, ownerRepo, githubApiToken);
            log.error("创建-->{}", caiMoGuGameIdFileName);
        }
        caiMoGuGameIds = CaiMoGuHelp.readResources(caiMoGuGameIdFileName);
        acGames = CaiMoGuHelp.readResources(acGameFileName);
        caiMoGuGameIds.removeIf(new HashSet<>(acGames)::contains);


        Set<String> delCaiMoGu = new HashSet<>();
        int succee = 0;
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
            List<String> comments = GamerskyHelp.getCommentsByGameName(en);
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
                delCaiMoGu.add(gameId);
            }
            if (succee >= 3) {
                break;
            }
        }

        caiMoGuGameIds.removeIf(delCaiMoGu::contains);
        GithubHelp.createOrUpdateFile(String.join("\n", caiMoGuGameIds), caiMoGuGameIdFileName, ownerRepo, githubApiToken);
        log.error("同步-->{}", caiMoGuGameIdFileName);

        acGames.addAll(ac);
        GithubHelp.createOrUpdateFile(String.join("\n", acGames), acGameFileName, ownerRepo, githubApiToken);
        log.error("同步-->{}", acGameFileName);


        Integer userPoint = CaiMoGUHelp2.getUserPoint();
        log.error("本次任务获取:{}", userPoint - userInfo.getPoint());
    }

}
