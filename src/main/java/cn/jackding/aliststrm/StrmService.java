package cn.jackding.aliststrm;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
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

    @Value("${alistServerToken}")
    private String token;

    @Value("${slowMode:0}")
    private String slowMode;

    @Value("${output.dir}")
    private String outputDir;

    @Value("${encode:1}")
    private String encode;

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

        JSONObject jsonObject = getAlist(path);
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
                if (object.getBoolean("is_dir")) {
                    String newLocalPath = localPath + File.separator + (object.getString("name").length() > 100 ? object.getString("name").substring(0, 20) : object.getString("name"));
                    File file = new File(newLocalPath);
                    file.mkdirs();
                    getData(path + "/" + object.getString("name"), newLocalPath);
                } else {
                    //视频文件
                    if (object.getString("name").toLowerCase().endsWith(".mp4") || object.getString("name").toLowerCase().endsWith(".mkv")
                            || object.getString("name").toLowerCase().endsWith(".avi") || object.getString("name").toLowerCase().endsWith(".mov")
                            || object.getString("name").toLowerCase().endsWith(".rmvb") || object.getString("name").toLowerCase().endsWith(".flv")
                            || object.getString("name").toLowerCase().endsWith(".webm") || object.getString("name").toLowerCase().endsWith(".m3u8")
                            || object.getString("name").toLowerCase().endsWith(".wmv") || object.getString("name").toLowerCase().endsWith(".iso")
                    ) {
                        String fileName = object.getString("name").substring(0, object.getString("name").lastIndexOf(".")).replaceAll("[\\\\/:*?\"<>|]", "");
                        try (FileWriter writer = new FileWriter(localPath + File.separator + (fileName.length() > 62 ? fileName.substring(0, 60) : fileName) + ".strm")) {
                            String encodePath = path + "/" + object.getString("name");
                            if ("1".equals(encode)) {
                                encodePath = URLEncoder.encode(path + "/" + object.getString("name"), "UTF-8").replace("+", "%20").replace("%2F", "/");
                            }
                            writer.write(url + "/d" + encodePath);
                        } catch (Exception e) {
                            log.error("", e);
                        }
                    }
                }
            });

        }

    }

    public JSONObject getAlist(String path) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS) // 连接超时时间为90秒
                .readTimeout(90, TimeUnit.SECONDS)    // 读取超时时间为90秒
                .writeTimeout(90, TimeUnit.SECONDS)   // 写入超时时间为90秒
                .build();
        JSONObject jsonResponse = null;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 构建请求体数据
        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("path", path);
        requestBodyJson.put("password", "");
        requestBodyJson.put("page", 1);
        requestBodyJson.put("per_page", 0);
        requestBodyJson.put("refresh", true);
        String requestBodyString = requestBodyJson.toJSONString();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/fs/list")
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyString))
                .build();

        log.info("开始获取{}", path);
        for (int i = 0; i < 3; i++) {
            // 发送请求并处理响应
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    // 获取响应体
                    String responseBody = response.body().string();

                    // 解析 JSON 响应
                    jsonResponse = JSONObject.parseObject(responseBody);

                    // 处理响应数据
                    if (200 == jsonResponse.getInteger("code")) {
                        log.info("获取完成{}", path);
                        return jsonResponse;
                    } else {
                        log.info("Response Body: " + jsonResponse.toJSONString());
                        log.error("获取{}第{}次失败", path, i + 1);
                        TimeUnit.SECONDS.sleep(1);
                    }

                } else {
                    log.info("Request failed with code: " + response.code());
                    return jsonResponse;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.info("获取失败{}", path);
                log.error("", e);
            }
        }
        return jsonResponse;
    }

}
