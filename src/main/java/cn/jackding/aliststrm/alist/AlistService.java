package cn.jackding.aliststrm.alist;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author Jack
 * @Date 2024/6/22 18:36
 * @Version 1.0.0
 */
@Service
@Slf4j
public class AlistService {

    @Value("${alistServerToken}")
    private String token;

    @Value("${alistServerUrl}")
    private String url;

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

    public JSONObject getFile(String path) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS) // 连接超时时间为90秒
                .readTimeout(90, TimeUnit.SECONDS)    // 读取超时时间为90秒
                .writeTimeout(90, TimeUnit.SECONDS)   // 写入超时时间为90秒
                .build();
        JSONObject jsonResponse;

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
                .url(url + "/api/fs/get")
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyString))
                .build();

        log.info("开始获取文件{}", path);

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                String responseBody = response.body().string();

                // 解析 JSON 响应
                jsonResponse = JSONObject.parseObject(responseBody);


                log.info("获取文件完成{}", path);
                return jsonResponse;


            } else {
                log.info("Request failed with code: " + response.code());
                return null;
            }
        } catch (Exception e) {
            log.info("获取文件失败{}", path);
            log.error("", e);
        }

        return null;
    }


    public JSONObject copyAlist(String srcDir, String dstDir, List<String> names) {
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
        requestBodyJson.put("src_dir", srcDir);
        requestBodyJson.put("dst_dir", dstDir);
        requestBodyJson.put("names", names);
        String requestBodyString = requestBodyJson.toJSONString();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/fs/copy")
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyString))
                .build();

        log.info("开始复制[{}]=>[{}]", srcDir, dstDir);
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
                        log.info("复制[{}]=>[{}]成功", srcDir, dstDir);
                        return jsonResponse;
                    } else {
                        log.info("Response Body: " + jsonResponse.toJSONString());
                        log.error("复制[{}]=>[{}]第{}次失败", srcDir, dstDir, i + 1);
                        TimeUnit.SECONDS.sleep(1);
                    }

                } else {
                    log.info("Request failed with code: " + response.code());
                    return jsonResponse;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.info("复制[{}]=>[{}]失败", srcDir, dstDir);
                log.error("", e);
            }
        }
        return jsonResponse;
    }


    public JSONObject mkdir(String path) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(90, TimeUnit.SECONDS) // 连接超时时间为90秒
                .readTimeout(90, TimeUnit.SECONDS)    // 读取超时时间为90秒
                .writeTimeout(90, TimeUnit.SECONDS)   // 写入超时时间为90秒
                .build();
        JSONObject jsonResponse;

        // 设置请求头
        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("Authorization", token)
                .build();

        // 构建请求体数据
        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("path", path);
        String requestBodyString = requestBodyJson.toJSONString();

        // 构建请求
        Request request = new Request.Builder()
                .url(url + "/api/fs/mkdir")
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBodyString))
                .build();

        log.info("开始下载{}", path);

        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                String responseBody = response.body().string();

                // 解析 JSON 响应
                jsonResponse = JSONObject.parseObject(responseBody);


                log.info("创建目录完成{}", path);
                return jsonResponse;


            } else {
                log.info("Request failed with code: " + response.code());
                return null;
            }
        } catch (Exception e) {
            log.info("获取失败{}", path);
            log.error("", e);
        }

        return null;
    }

}
