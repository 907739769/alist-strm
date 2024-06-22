package cn.jackding.aliststrm.util;

/**
 * @Author Jack
 * @Date 2024/6/22 19:23
 * @Version 1.0.0
 */
public class Utils {

    public static boolean isVideo(String name) {
        return name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".mkv")
                || name.toLowerCase().endsWith(".avi") || name.toLowerCase().endsWith(".mov")
                || name.toLowerCase().endsWith(".rmvb") || name.toLowerCase().endsWith(".flv")
                || name.toLowerCase().endsWith(".webm") || name.toLowerCase().endsWith(".m3u8")
                || name.toLowerCase().endsWith(".wmv") || name.toLowerCase().endsWith(".iso");

    }

    public static boolean isSrt(String name) {
        return name.toLowerCase().endsWith(".ass") || name.toLowerCase().endsWith(".srt");
    }

}
