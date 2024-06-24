# alist-strm
 alist生成可播放strm视频文件

## docker部署 


```
部署前参数需要修改
必要参数
alistServerUrl  alist地址 如http://192.168.1.2:5244
alistServerToken 可在alist后台获取
alistScanPath 需要生成strm文件的目录如http://192.168.1.2:5244/阿里云分享/电影 那就填入/阿里云分享/电影
可选参数
slowMode  单线程模式，防止请求网盘太快，默认0，启用填1
encode 是否编码strm文件里面的链接  默认1启用  不启用填0
tgToken  tg机器人token，通过t.me/BotFather机器人创建bot获取token
tgUserId tg用户id，通过t.me/userinfobot机器人获取userId
isDownSub 是否下载目录里面的字幕文件 默认0不下载  下载填1
runAfterStartup  启动是否立即执行同步任务 默认启用1，启用填0

复制alist不同目录的视频 源目录删除不会删除目标目录文件 只会新增
srcDir 源目录
dstDir 目标目录

```

# 开发计划

- [x] tg机器人命令生成strm文件
- [ ] ...

# 更新记录

```
20240610 重构代码,增加tg机器人命令strm、strmdir
20240617 增加下载目录中字幕文件的功能
20240617 增加alist目录复制的功能 使用tg机器人/sync命令执行任务
20240622 执行sync任务之后自动执行strm任务 增加定时任务每天6、18点执行sync任务
20240623 增加调用接口api/v1/notify直接执行复制sync任务  配合qb使用  监听端口是6894
20240624 增加/syncdir命令执行指定目录的同步复制，如：/sync /阿里云盘/电影#/115网盘/电影，就会将/阿里云盘/电影下的视频同步复制到/115网盘/电影
```

# docker CLI安装

```
docker run -d \
--name=alist-strm \
-e TZ=Asia/Shanghai \
-e alistServerUrl=http://192.168.1.2:5244 \
-e alistServerToken=xxx \
-e alistScanPath='/阿里云分享/电影' \
-e slowMode=0 \
-v /volume1/docker/alist-strm/data:/data \
jacksaoding/alist-strm:latest
```

# docker compose安装

```
version: "3"
services:
  app:
    container_name: alist-strm
    image: 'jacksaoding/alist-strm:latest'
    network_mode: "host"
    environment:
      TZ: Asia/Shanghai
      alistServerUrl: http://192.168.1.2:5244
      alistServerToken: xxx
      alistScanPath: /阿里云分享/电影
      slowMode: 0
    volumes:
      - /volume1/docker/alist-strm/data:/data
```
