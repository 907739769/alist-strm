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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    @Value("${strmAfterSync:1}")
    private String strmAfterSync;

    private final Set<String> cache = ConcurrentHashMap.newKeySet();

    public void syncFiles(String srcDir, String dstDir, String relativePath, String strmDir, Set<String> taskIdList) {
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
        } else {
            stream = jsonArray.stream().parallel();
        }
        stream.forEach(content -> {
            JSONObject contentJson = (JSONObject) content;
            String name = contentJson.getString("name");

            //不是视频文件就不用继续往下走上传了
            if (!contentJson.getBoolean("is_dir") && !Utils.isVideo(name)) {
                return;
            }

            JSONObject jsonObject = alistService.getFile(dstDir + "/" + relativePath + "/" + name);
            //是目录
            if (contentJson.getBoolean("is_dir")) {
                //判断目标目录是否存在这个文件夹
                //200就是存在 存在就继续往下级目录找
                if (200 == jsonObject.getInteger("code")) {
                    syncFiles(srcDir, dstDir, relativePath + "/" + name, taskIdList);
                } else {
                    alistService.mkdir(dstDir + "/" + relativePath + "/" + name);
                    syncFiles(srcDir, dstDir, relativePath + "/" + name, taskIdList);
                }
            } else {
                if (cache.contains(dstDir + "/" + relativePath + "/" + name)) {
                    log.info("文件已处理过，跳过处理" + dstDir + "/" + relativePath + "/" + name);
                    return;
                }
                //是视频文件才复制 并且不存在
                if (!(200 == jsonObject.getInteger("code")) && Utils.isVideo(name)) {
                    if (contentJson.getLong("size") > Long.parseLong(minFileSize) * 1024 * 1024) {
                        JSONObject jsonResponse = alistService.copyAlist(srcDir + "/" + relativePath, dstDir + "/" + relativePath, Collections.singletonList(name));
                        if (jsonResponse != null && 200 == jsonResponse.getInteger("code")) {
                            cache.add(dstDir + "/" + relativePath + "/" + name);
                            flag.set(true);
                            //获取上传文件的任务id
                            JSONArray tasks = jsonResponse.getJSONObject("data").getJSONArray("tasks");
                            taskIdList.add(tasks.getJSONObject(0).getString("id"));
                        }
                    }
                }
            }
        });

        if (flag.get() && "1".equals(strmAfterSync)) {
            asynService.isCopyDone(dstDir, strmDir, taskIdList);
        }


    }

    public void syncOneFile(String srcDir, String dstDir, String relativePath) {
        if (cache.contains(dstDir + "/" + relativePath)) {
            log.info("文件已处理过，跳过处理" + dstDir + "/" + relativePath);
            return;
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        String taskId = null;
        JSONObject jsonObject = alistService.getFile(dstDir + "/" + relativePath);
        if (!(200 == jsonObject.getInteger("code")) && Utils.isVideo(relativePath)) {
            JSONObject srcJson = alistService.getFile(srcDir + "/" + relativePath);
            if (srcJson.getJSONObject("data").getLong("size") > Long.parseLong(minFileSize) * 1024 * 1024) {
                alistService.mkdir(dstDir + "/" + relativePath.substring(0, relativePath.lastIndexOf("/")));
                JSONObject jsonResponse = alistService.copyAlist(srcDir + "/" + relativePath.substring(0, relativePath.lastIndexOf("/")), dstDir + "/" + relativePath.substring(0, relativePath.lastIndexOf("/")), Collections.singletonList(relativePath.substring(relativePath.lastIndexOf("/"))));
                if (jsonResponse != null && 200 == jsonResponse.getInteger("code")) {
                    cache.add(dstDir + "/" + relativePath);
                    flag.set(true);
                    //获取上传文件的任务id
                    JSONArray tasks = jsonResponse.getJSONObject("data").getJSONArray("tasks");
                    taskId = tasks.getJSONObject(0).getString("id");
                }
            }
        }

        if (flag.get() && "1".equals(strmAfterSync)) {
            asynService.isCopyDoneOneFile(dstDir + relativePath, taskId);
        }

    }

    public void syncOneFile(String relativePath) {
        syncOneFile(srcDir, dstDir, relativePath);
    }

    public void syncFiles(String srcDir, String dstDir, String relativePath, Set<String> taskIdList) {
        syncFiles(srcDir, dstDir, relativePath, "", taskIdList);
    }

    public void syncFiles(String relativePath, Set<String> taskIdList) {
        syncFiles(srcDir, dstDir, relativePath, relativePath, taskIdList);
    }


}
