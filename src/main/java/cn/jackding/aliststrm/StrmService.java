package cn.jackding.aliststrm;

import cn.jackding.aliststrm.alist.AlistService;
import cn.jackding.aliststrm.util.Utils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @Author Jack
 * @Date 2024/6/10 20:13
 * @Version 1.0.0
 */
@Service
@Slf4j
public class StrmService {


    @Value("${alistScanPath}")
    private String path;

    @Value("${alistServerUrl}")
    private String url;

    @Value("${slowMode:0}")
    private String slowMode;

    @Value("${output.dir}")
    private String outputDir;

    @Value("${encode:1}")
    private String encode;

    @Value("${isDownSub:0}")
    private String isDownSub;

    @Autowired
    private AlistService alistService;

    public void strm() {
        log.info("开始执行strm任务{}", LocalDateTime.now());
        try {
            getData(path, outputDir + File.separator + path.replace("/", File.separator));
        } catch (Exception e) {
            log.error("", e);
        } finally {
            log.info("任务执行完成{}", LocalDateTime.now());
        }
    }

    public void strmDir(String path) {
        log.info("开始执行指定路径strm任务{}", LocalDateTime.now());
        try {
            getData(path, outputDir + File.separator + path.replace("/", File.separator));
        } catch (Exception e) {
            log.error("", e);
        } finally {
            log.info("任务执行完成{}", LocalDateTime.now());
        }
    }

    public void getData(String path, String localPath) {

        File outputDirFile = new File(localPath);
        outputDirFile.mkdirs();

        JSONObject jsonObject = alistService.getAlist(path);
        if (jsonObject != null && 200 == jsonObject.getInteger("code")) {
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("content");
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
                System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "49");
                stream = jsonArray.stream().parallel();
            }

            stream.forEach(obj -> {
                JSONObject object = (JSONObject) obj;
                String name = object.getString("name");
                if (object.getBoolean("is_dir")) {
                    String newLocalPath = localPath + File.separator + (name.length() > 100 ? name.substring(0, 20) : name);
                    File file = new File(newLocalPath);
                    file.mkdirs();
                    getData(path + "/" + name, newLocalPath);
                } else {
                    //视频文件
                    if (Utils.isVideo(name)) {
                        String fileName = name.substring(0, name.lastIndexOf(".")).replaceAll("[\\\\/:*?\"<>|]", "");
                        try (FileWriter writer = new FileWriter(localPath + File.separator + (fileName.length() > 62 ? fileName.substring(0, 60) : fileName) + ".strm")) {
                            String encodePath = path + "/" + name;
                            if ("1".equals(encode)) {
                                encodePath = URLEncoder.encode(path + "/" + name, "UTF-8").replace("+", "%20").replace("%2F", "/");
                            }
                            writer.write(url + "/d" + encodePath);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }

                    //字幕文件
                    if ("1".equals(isDownSub) && Utils.isSrt(name)) {
                        String url = alistService.getFile(path + "/" + name).getJSONObject("data").getString("raw_url");
                        String fileName = name.replaceAll("[\\\\/:*?\"<>|]", "");
                        downloadFile(url, localPath + File.separator + (fileName.length() > 62 ? fileName.substring(0, 60) : fileName) + name.substring(name.lastIndexOf(".")));
                    }
                }
            });

        }

    }

    public static void downloadFile(String fileURL, String saveDir) {
        // 创建URL对象
        URL url = null;
        try {
            url = new URL(fileURL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        // 打开连接
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (InputStream inputStream = new BufferedInputStream(connection.getInputStream()); FileOutputStream outputStream = new FileOutputStream(saveDir);) {

            byte[] buffer = new byte[1024];
            int bytesRead = -1;

            // 读取文件并写入到本地
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

        } catch (IOException ex) {
            log.error("", ex);
        }
    }

}
