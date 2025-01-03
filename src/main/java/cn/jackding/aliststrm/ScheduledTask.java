package cn.jackding.aliststrm;

import cn.jackding.aliststrm.alist.AlistService;
import cn.jackding.aliststrm.service.CopyAlistFileService;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @Author Jack
 * @Date 2024/5/24 13:32
 * @Version 1.0.0
 */
@Log4j2
@Service
public class ScheduledTask {

    @Autowired
    private CopyAlistFileService copyAlistFileService;

    @Autowired
    private AlistService alistService;

    /**
     * 每天执行两次
     */
    @Scheduled(cron = "${scheduledCron:0 0 6,18 * * ?}")
    public void syncDaily() {
        JSONObject jsonObject = alistService.copyUndone();
        if (jsonObject == null || !(200 == jsonObject.getInteger("code"))) {
            return;
        } else {
            log.warn("定时任务未执行，因为alist的task/copy/undone服务不可用");
        }
        if (CollectionUtils.isEmpty(jsonObject.getJSONArray("data"))) {
            copyAlistFileService.syncFiles("");
        } else {
            log.warn("定时任务未执行，因为还有正在上传的文件");
        }
    }


}
