package com.github.shy526.vo;

import lombok.Data;

@Data
public class GithubInfo {
    private String ownerRepo;
    private String githubApiToken;
    private String repoPath="src/main/resources";
}
