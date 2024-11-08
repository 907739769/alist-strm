package cn.jackding.aliststrm.service;

import cn.jackding.aliststrm.alist.AlistService;
import cn.jackding.aliststrm.util.Utils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * 复制alist文件
 *
 * @Author Jack
 * @Date 2024/6/22 17:53
 * @Version 1.0.0
 */
@Service
@Slf4j
public class CopyAlistFileService {

    @Value("${srcDir:}")
    private String srcDir;

    @Value("${dstDir:}")
    private String dstDir;

    @Autowired
    private AlistService alistService;

    @Autowired
    private AsynService asynService;

    @Value("${minFileSize:100}")
    private String minFileSize;

    @Value("${slowMode:0}")
    private String slowMode;

    private List<String> cache = new CopyOnWriteArrayList<>();

    public void syncFiles(String srcDir, String dstDir, String relativePath, String strmDir) {
        if (StringUtils.isAnyBlank(srcDir, dstDir)) {
            return;
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        //查出所有源目录
        JSONObject object = alistService.getAlist(srcDir + relativePath);
        if (object.getJSONObject("data") == null) {
            return;
        }
        JSONArray jsonArray = object.getJSONObject("data").getJSONArray("content");
        if (jsonArray == null) {
            return;
        }
        //判断是不是用多线程流
        Stream<Object> stream;
        if ("1".equals(slowMode)) {
            stream = jsonArray.stream().sequential();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                log.error("", e);
            }
        } else {
            stream = jsonArray.stream().parallel();
        }
        stream.forEach(content -> {
            JSONObject contentJson = (JSONObject) content;
            String name = contentJson.getString("name");
            JSONObject jsonObject = alistService.getFile(dstDir + "/" + relativePath + "/" + name);
            //是目录
            if (contentJson.getBoolean("is_dir")) {
                //判断目标目录是否存在这个文件夹
                //200就是存在 存在就继续往下级目录找
                if (200 == jsonObject.getInteger("code")) {
                    syncFiles(srcDir, dstDir, relativePath + "/" + name);
                } else {
                    alistService.mkdir(dstDir + "/" + relativePath + "/" + name);
                    syncFiles(srcDir, dstDir, relativePath + "/" + name);
                }
            } else {
                if (cache.contains(dstDir + "/" + relativePath + "/" + name)) {
                    return;
                }
                //是视频文件才复制 并且不存在
                if (!(200 == jsonObject.getInteger("code")) && Utils.isVideo(name)) {
                    if (contentJson.getLong("size") > Long.parseLong(minFileSize) * 1024 * 1024) {
                        JSONObject jsonResponse = alistService.copyAlist(srcDir + "/" + relativePath, dstDir + "/" + relativePath, Collections.singletonList(name));
                        if (jsonResponse != null && 200 == jsonResponse.getInteger("code")) {
                            cache.add(dstDir + "/" + relativePath + "/" + name);
                            flag.set(true);
                        }
                    }
                }
            }
        });

        if (flag.get()) {
            asynService.isCopyDone(dstDir + strmDir);
        }


    }

    public void syncOneFile(String srcDir, String dstDir, String relativePath) {
        if (cache.contains(dstDir + "/" + relativePath)) {
            return;
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        JSONObject jsonObject = alistService.getFile(dstDir + "/" + relativePath);
        if (!(200 == jsonObject.getInteger("code")) && Utils.isVideo(relativePath)) {
            JSONObject srcJson = alistService.getFile(srcDir + "/" + relativePath);
            if (srcJson.getLong("size") > Long.parseLong(minFileSize) * 1024 * 1024) {
                alistService.mkdir(dstDir + "/" + relativePath.substring(0, relativePath.lastIndexOf("/")));
                JSONObject jsonResponse = alistService.copyAlist(srcDir + "/" + relativePath.substring(0, relativePath.lastIndexOf("/")), dstDir + "/" + relativePath.substring(0, relativePath.lastIndexOf("/")), Collections.singletonList(relativePath.substring(relativePath.lastIndexOf("/"))));
                if (jsonResponse != null && 200 == jsonResponse.getInteger("code")) {
                    cache.add(dstDir + "/" + relativePath);
                    flag.set(true);
                }
            }
        }

        if (flag.get()) {
            asynService.isCopyDoneOneFile(dstDir + relativePath);
        }

    }

    public void syncOneFile(String relativePath) {
        syncOneFile(srcDir, dstDir, relativePath);
    }

    public void syncFiles(String srcDir, String dstDir, String relativePath) {
        syncFiles(srcDir, dstDir, relativePath, "");
    }

    public void syncFiles(String relativePath) {
        syncFiles(srcDir, dstDir, relativePath, relativePath);
    }


}
