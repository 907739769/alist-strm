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
import java.util.concurrent.atomic.AtomicBoolean;

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

    public void syncFiles(String srcDir, String dstDir, String relativePath) {
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
        jsonArray.forEach(content -> {
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
                //是视频文件才复制 并且不存在
                if (!(200 == jsonObject.getInteger("code")) && Utils.isVideo(name)) {
                    alistService.copyAlist(srcDir + "/" + relativePath, dstDir + "/" + relativePath, Collections.singletonList(name));
                    flag.set(true);
                }
            }
        });

        if (flag.get()) {
            asynService.isCopyDone(dstDir);
        }


    }

    public void syncFiles(String relativePath) {
        syncFiles(srcDir, dstDir, relativePath);
    }


}
