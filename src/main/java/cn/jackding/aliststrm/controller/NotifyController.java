package cn.jackding.aliststrm.controller;

import cn.jackding.aliststrm.service.CopyAlistFileService;
import cn.jackding.aliststrm.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author Jack
 * @Date 2024/6/23 20:34
 * @Version 1.0.0
 */
@RestController
@RequestMapping("api/v1")
@Slf4j
public class NotifyController {

    @Value("${replaceDir:}")
    private String replaceDir;

    @Autowired
    private CopyAlistFileService copyAlistFileService;

    @PostMapping("/notify")
    public void notifySync() {
        copyAlistFileService.syncFiles("", new CopyOnWriteArrayList<>());
    }

    @PostMapping("/notifyByDir")
    public void notifyByDir(@RequestBody Map<String, Object> map) {
        log.info("map: " + map);
        String relativePath = "";
        if (StringUtils.hasText(replaceDir) && StringUtils.hasText((CharSequence) map.get("dir"))) {
            relativePath = map.get("dir").toString().replaceFirst(replaceDir, "");
            if (Utils.isVideo(relativePath)) {
                copyAlistFileService.syncOneFile(relativePath);
            } else {
                copyAlistFileService.syncFiles(relativePath, new CopyOnWriteArrayList<>());
            }
        } else {
            copyAlistFileService.syncFiles(relativePath, new CopyOnWriteArrayList<>());
        }
    }

}
