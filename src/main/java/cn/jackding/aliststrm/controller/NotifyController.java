package cn.jackding.aliststrm.controller;

import cn.jackding.aliststrm.service.CopyAlistFileService;
import cn.jackding.aliststrm.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author Jack
 * @Date 2024/6/23 20:34
 * @Version 1.0.0
 */
@RestController
@RequestMapping("api/v1")
public class NotifyController {

    @Value("${replaceDir:}")
    private String replaceDir;

    @Autowired
    private CopyAlistFileService copyAlistFileService;

    @PostMapping("/notify")
    public void notifySync() {
        copyAlistFileService.syncFiles("");
    }

    @PostMapping("/notifyByDir")
    public void notifyByDir(@RequestBody Map<String, Object> map) {
        String relativePath = "";
        if (StringUtils.hasText(replaceDir) && StringUtils.hasText((CharSequence) map.get("dir"))) {
            relativePath = map.get("dir").toString().replace(replaceDir, "");
            if (Utils.isVideo(relativePath)) {
                {
                    relativePath = relativePath.substring(0, relativePath.lastIndexOf("/"));
                }
            }
        }
        copyAlistFileService.syncFiles(relativePath);
    }

}
