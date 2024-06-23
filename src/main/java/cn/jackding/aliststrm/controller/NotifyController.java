package cn.jackding.aliststrm.controller;

import cn.jackding.aliststrm.service.CopyAlistFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Jack
 * @Date 2024/6/23 20:34
 * @Version 1.0.0
 */
@RestController
@RequestMapping("api/v1")
public class NotifyController {

    @Autowired
    private CopyAlistFileService copyAlistFileService;

    @PostMapping("/notify")
    public void notifySync() {
        copyAlistFileService.syncFiles("");
    }

}
