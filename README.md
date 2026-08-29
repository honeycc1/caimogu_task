# 踩蘑菇每天3点影响力

试跑半个月挺稳定的

现在只能嫖3点

已经恢复

~~已经失效,使用会导致影响力变负~~

## 配置方式

1. fork仓库

2. 配置四个仓库参数
    - `OWNER_REPO` 用户名/仓库名
    - ~~`CAI_MO_GU_TOKEN`~~ ~~踩蘑菇的Token cmg_token~~
    - `MY_GITHUB_API_TOKEN` githubapiToken 需要读写权限
    - `CMG_NAME` 踩蘑菇帐号名
    - `CMG_PASSWORD` 踩蘑菇密码
    - `REPO_PATH`
        - 仓库存储txt的路径
        - 默认 `src/main/resources`
        - `/` 代表根目录
    - SUB_ACCOUNT
        - `expiration`
            - 代表要获取的总积分
        - 格式要求
    ```json
    [
      { "name": "2", "pas": "密码.", "expiration": "90" },
      { "name": "3", "pas": "密码.", "expiration": "90" }
    ]
    ```

~~3. 删除文件~~

- ~~run.txt~~     ~~(记录上一次运行时间)~~
- ~~acIds.txt~~   ~~(记录已经评价的游戏)~~
- ~~postIds.txt~~  ~~(记录已经评论的帖子)~~
- ~~gameComment.txt~~  ~~(记录评论游戏库评论)~~

## 更新

- 2026.08.08
    - 小黑盒评论获取-
        - 增加评论多样性
    - 配置多账号的变量
        - SUB_ACCOUNT
    - 可以设置仓库路径的变量
        - REPO_PATH
    - 增加了总积分机制
        - 满足总积分后不在获得积分

- 2026.08.08
    - 只保留游戏库评论功能
    - 添加从游民星空获取评论转移到踩蘑菇的功能
    - 后面应该会添加别的网站转移的功能
        - www.biligame.com
        - b站的专栏获取
        - 有时间在做吧
    - 还是想吐槽一下踩蘑菇和游民星空的搜索可能还在十年前,是真难受

- 2026-01-14
    - h5游戏库无法获取影响力,暂时屏蔽,只保留回帖功能

- 2026-01-14
    - 修复一些小问题/幂等处理
    - 游戏库无法使用正常获取影响力(不知道是不是我号的问题),碰到了可以反馈一下

- 2026-01-13
    - 在同一个帖子的相同恢复无法获取影响力 随机六个表情防重
    - 修复一些小问题

- 2026-01-12
    - 签名错误问题首字母+自动被转换为空格问题
    - 自动清理非本用户的记录文件

- 2026-01-11
    - 基于h5接口的自动登录
    - 添加调用h5接口的代码
    - 复刻js签名算法
    - 流程调整
    - 增加对游戏库评论评论
    - 严格检查可获取影响力频道
    - 重试机制

- 2025.12.12
    - cmgToken问题修复(10-12号之间fork的需要重新修改)
    - 个人主页问题修复
    - 触发器恢复

- 2025.12.10
    - 帖子回复

## 踩蘑菇接口(以下是该项目用到的所有接口和html)

- https://www.caimogu.cc/game/find.html?act=fetch&date=2024-07&sort=1&sort_desc=1&page=1
    - 游戏库列表

- https://www.caimogu.cc/game/act/score
    - 游戏评分

- https://www.caimogu.cc/user/my.html
    - 个人主页

- https://www.caimogu.cc/user/act/my_list?act=reply&page=3
    - 个人主页回复列表

- https://www.caimogu.cc/circle/act/post_list?id=449&kwType=post&kw=&type=all&topic=&tags=&page=1
    - 莫个圈子中帖子列表

- https://www.caimogu.cc/post/comments?id=2264113&pid=0&order=default&page=1
    - 帖子中评论列表

- https://www.caimogu.cc/post/act/comment
    - 评论帖子

- https://www.caimogu.cc/login.html
    - 登录页

- https://www.caimogu.cc/logi
    - 登录

- https://www.caimogu.cc/user/my/wallet/list?act=point&page=%s
    - 影响力删减列表

- https://www.caimogu.cc/game/find.html?act=fetch&score=%s&kw=&platforms=&tags=&status=0&sort=2&sort_desc=1&page=%s
    - 按评分的游戏库列表

### 踩蘑菇h5接口

- https://api.caimogu.cc/v2/game/commentList
    - 游戏评论列表

- https://api.caimogu.cc/v1/game/commentReply
    - 评论游戏库评论

- https://api.caimogu.cc/v2/game/score
    - 评论游戏

- https://api.caimogu.cc/v3/post/comment/list
    - 圈子评论列表

- https://api.caimogu.cc/v2/game/list
    - 游戏库列表

- https://api.caimogu.cc/v3/circle/detail/list
    - 圈子中的帖子列表

- https://api.caimogu.cc/v3/post/reply
    - 评价帖子

- https://api.caimogu.cc/v3/my/reply/list
    - 个人主页回复列表

- https://api.caimogu.cc/v3/my/info
    - 用户信息

- https://api.caimogu.cc/v3/login/account
    - 登录

### 游明星空

- https://so.gamersky.com/all/ku?s=%s
    - 游戏查询

- https://router5.gamersky.com/@/gameScoreDetailPage/index/6.0.0/0
    - 获取评论

## 小黑盒

- https://api.xiaoheihe.cn/bbs/app/api/general/search/v1
    - 搜索

- https://api.xiaoheihe.cn/bbs/app/link/game/comments
    - 获取评论