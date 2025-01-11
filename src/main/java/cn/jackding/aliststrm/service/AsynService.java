package cn.jackding.aliststrm.service;

import cn.jackding.aliststrm.alist.AlistService;
import cn.jackding.aliststrm.util.Utils;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final AtomicBoolean isRun=new AtomicBoolean(false);

    /**
     * 判断alist的复制任务是否完成 完成就执行strm任务
     *
     * @return
     * @Async
     */
    @Async
    public void isCopyDone(String dstDir, String strmDir, List<String> taskIdList) {
        if (isRun.get() && StringUtils.isBlank(strmDir)) {
            return;
        }
        isRun.set(true);
        Utils.sleep(30);
        while (true) {
            boolean allTasksCompleted = true;
            for (String taskId : taskIdList) {
                JSONObject jsonResponse = alistService.copyInfo(taskId);
                if (jsonResponse == null) {
                    continue;
                }

                // 检查任务状态
                Integer code = jsonResponse.getInteger("code");
                Integer state = jsonResponse.getJSONObject("data").getInteger("state");

                //不是上传成功状态
                if (200 == code && state != 2) {
                    //也不是上传中状态 就是其他失败状态了  就重试
                    if (state != 1) {
                        alistService.copyRetry(taskId);
                    }
                    allTasksCompleted = false;
                }
            }
            if (allTasksCompleted) {
                isRun.set(false);
                strmService.strmDir(dstDir + strmDir);// 生成 STRM 文件
                break;// 任务完成，退出循环
            } else {
                Utils.sleep(30);//继续检查
            }
        }
    }

    @Async
    public void isCopyDoneOneFile(String path, String taskId) {
        Utils.sleep(30);
        while (true) {
            JSONObject jsonResponse = alistService.copyInfo(taskId);
            if (jsonResponse == null) {
                break;
            }
            // 检查任务状态
            Integer code = jsonResponse.getInteger("code");
            Integer state = jsonResponse.getJSONObject("data").getInteger("state");
            //判定任务是否完成了 完成了就生成strm文件
            if (404 == code || state == 2) {
                strmService.strmOneFile(path);// 生成 STRM 文件
                break;// 任务完成，退出循环
            } else if (state != 1) {
                //也不是上传中状态 就是其他失败状态了  就重试
                alistService.copyRetry(taskId);
            }
            Utils.sleep(30);//继续检查
        }
    }

}
