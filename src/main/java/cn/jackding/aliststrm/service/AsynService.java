package cn.jackding.aliststrm.service;

import cn.jackding.aliststrm.alist.AlistService;
import cn.jackding.aliststrm.util.Utils;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 异步线程服务
 *
 * @Author Jack
 * @Date 2024/6/23 12:42
 * @Version 1.0.0
 */
@Service
public class AsynService {

    @Autowired
    private AlistService alistService;

    @Autowired
    private StrmService strmService;

    private boolean isRun;

    /**
     * 判断alist的复制任务是否完成 完成就执行strm任务
     *
     * @return
     * @Async
     */
    @Async
    public void isCopyDone(String dstDir) {
        if (isRun) {
            return;
        }
        isRun = true;
        Utils.sleep(30);
        while (true) {
            JSONObject jsonObject = alistService.copyUndone();
            if (jsonObject == null || !(200 == jsonObject.getInteger("code"))) {
                isRun = false;
                break;
            }
            if (CollectionUtils.isEmpty(jsonObject.getJSONArray("data"))) {
                isRun = false;
                strmService.strmDir(dstDir);
                break;
            } else {
                Utils.sleep(30);
            }
        }
    }

}
