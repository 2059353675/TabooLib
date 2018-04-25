package com.ilummc.tlib.dependency;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ilummc.eagletdl.EagletTask;
import com.ilummc.eagletdl.ProgressEvent;
import com.ilummc.tlib.TLib;

import me.skymc.taboolib.Main;

public class TDependency {

    public static final String MAVEN_REPO = "http://repo.maven.apache.org/maven2";

    /**
     * 请求一个插件作为依赖，这个插件将会在所有已经添加的 Jenkins 仓库、Maven 仓库寻找
     * <p>
     * 阻塞线程进行下载/加载
     *
     * @param args 插件名称，下载地址（可选）
     * @return 是否成功加载了依赖
     */
    public static boolean requestPlugin(String... args) {
        return false;
    }

    /**
     * 请求一个库作为依赖，这个库将会在 Maven Central、oss.sonatype 以及自定义的 Maven 仓库寻找
     * <p>
     * 阻塞线程进行下载/加载
     *
     * @param type 依赖名，格式为 groupId:artifactId:version
     * @return 是否成功加载库，如果加载成功，插件将可以任意调用使用的类
     */
    public static boolean requestLib(String type, String repo, String url) {
        if (type.matches(".*:.*:.*")) {
            String[] arr = type.split(":");
            File file = new File(Main.getInst().getDataFolder(), "/libs/" + String.join("-", arr) + ".jar");
            if (file.exists()) {
                TDependencyLoader.addToPath(Main.getInst(), file);
                return true;
            } else {
                if (downloadMaven(repo, arr[0], arr[1], arr[2], file, url)) {
                    TDependencyLoader.addToPath(Main.getInst(), file);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean downloadMaven(String url, String groupId, String artifactId, String version, File target, String dl) {
        if (Main.getInst().getConfig().getBoolean("OFFLINE-MODE")) {
            TLib.getTLib().getLogger().warn("已启用离线模式, 将不会下载第三方依赖库");
            return false;
        }
        AtomicBoolean failed = new AtomicBoolean(false);
        String link = dl.length() == 0 ? url + "/" + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar" : dl;
        new EagletTask()
                .url(link)
                .file(target)
                .setThreads(getDownloadPoolSize())
                .setOnError(event -> {})
                .setOnConnected(event -> TLib.getTLib().getLogger().info("  正在下载 " + String.join(":", new String[]{groupId, artifactId, version}) + " 大小 " + ProgressEvent.format(event.getContentLength())))
                .setOnProgress(event -> TLib.getTLib().getLogger().info("    下载速度 " + event.getSpeedFormatted() + " 进度 " + event.getPercentageFormatted()))
                .setOnComplete(event -> {
                    if (event.isSuccess()) {
                        TLib.getTLib().getLogger().info("  下载 " + String.join(":", new String[]{groupId, artifactId, version}) + " 完成");
                    } else {
                        failed.set(true);
                        TLib.getTLib().getLogger().error("  下载 " + String.join(":", new String[]{groupId, artifactId, version}) + " 失败");
                        TLib.getTLib().getLogger().error("  请手动下载 " + link + " 并重命名为 " + target.getName() + " 后放在 /TabooLib/libs 文件夹内");
                    }
                }).start().waitUntil();
        return !failed.get();
    }
    
    private static int getDownloadPoolSize() {
    	return Main.getInst().getConfig().contains("DOWNLOAD-POOL-SIZE") ? Main.getInst().getConfig().getInt("DOWNLOAD-POOL-SIZE") : 4;
    }
}