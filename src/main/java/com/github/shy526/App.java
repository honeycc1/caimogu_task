package com.github.shy526;


import com.alibaba.fastjson2.JSON;
import com.github.shy526.caimogu.XiaoHeiHe;
import com.github.shy526.config.Config;
import com.github.shy526.task.CaiMoGuTask;
import com.github.shy526.vo.GithubInfo;
import com.github.shy526.vo.SubAccount;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

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
        String subAccount = System.getenv("SUB_ACCOUNT");
        String repoPath = System.getenv("REPO_PATH");
        GithubInfo githubInfo = new GithubInfo();
        if (repoPath != null && !repoPath.isEmpty()) {
            if ("/".equals(repoPath)) {
                repoPath = "";
            }
            githubInfo.setRepoPath(repoPath);
        }
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
        CaiMoGuTask caiMoGuTask = new CaiMoGuTask();
        caiMoGuTask.run(userName, password, -1);

        if (!Strings.isNullOrEmpty(subAccount) && JSON.isValid(subAccount.trim())) {
            List<SubAccount> subAccounts = JSON.parseArray(subAccount.trim(), SubAccount.class);
            for (SubAccount account : subAccounts) {
                caiMoGuTask.run(account.getName(), account.getPas(), account.getExpiration());
            }
        }
    }

}
