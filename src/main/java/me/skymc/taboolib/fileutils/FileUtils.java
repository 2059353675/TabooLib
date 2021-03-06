package me.skymc.taboolib.fileutils;

import ch.njol.util.Closeable;
import com.ilummc.eagletdl.EagletTask;
import com.ilummc.eagletdl.ProgressEvent;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.tlib.util.IO;
import me.skymc.taboolib.Main;
import org.apache.commons.io.IOUtils;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;

/**
 * @author sky
 */
public class FileUtils {

    /**
     * 获取本地 IP 地址
     *
     * @return {@link String}
     */
    public static String ip() {
        URL url;
        URLConnection con;
        try {
            url = new URL("http://1212.ip138.com/ic.asp");
            con = url.openConnection();
        } catch (Exception ignored) {
            return "[IP ERROR]";
        }
        InputStream ins = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            ins = con.getInputStream();
            inputStreamReader = new InputStreamReader(ins, "GB2312");
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder webContent = new StringBuilder();
            bufferedReader.lines().forEach(webContent::append);
            int start = webContent.indexOf("[") + 1;
            int end = webContent.indexOf("]");
            return webContent.substring(start, end);
        } catch (Exception ignored) {
            return "[IP ERROR]";
        } finally {
            IOUtils.close(con);
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(inputStreamReader);
            IOUtils.closeQuietly(ins);
        }
    }

    /**
     * 获取插件所有类
     *
     * @return {@link List<Class>}
     */
    public static List<Class> getClasses(Class<?> obj) {
        List<Class> classes = new ArrayList<>();
        URL url = getCaller(obj).getProtectionDomain().getCodeSource().getLocation();
        try {
            File src;
            try {
                src = new File(url.toURI());
            } catch (URISyntaxException e) {
                src = new File(url.getPath());
            }
            new JarFile(src).stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry -> {
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                try {
                    classes.add(Class.forName(className, false, obj.getClassLoader()));
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
        return classes;
    }

    /**
     * 获取插件所有类
     *
     * @return {@link List<Class>}
     */
    public static List<Class> getClasses(Plugin plugin) {
        List<Class> classes = new CopyOnWriteArrayList<>();
        URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            File src;
            try {
                src = new File(url.toURI());
            } catch (URISyntaxException e) {
                src = new File(url.getPath());
            }
            new JarFile(src).stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry -> {
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                try {
                    classes.add(Class.forName(className, false, plugin.getClass().getClassLoader()));
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
        return classes;
    }

    /**
     * 获取资源文件
     *
     * @param filename 文件名
     * @return {@link InputStream}
     */
    public static InputStream getResource(String filename) {
        return getResource(Main.getInst(), filename);
    }

    /**
     * 获取插件资源文件
     *
     * @param plugin   插件
     * @param filename 文件名
     * @return {@link InputStream}
     */
    public static InputStream getResource(Plugin plugin, String filename) {
        return plugin.getClass().getClassLoader().getResourceAsStream(filename);
    }

    /**
     * 写入文件
     *
     * @param inputStream 输入流
     * @param file        文件
     */
    public static void inputStreamToFile(InputStream inputStream, File file) {
        try (FileOutputStream fos = new FileOutputStream(file) ; BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buf = new byte[1024];
            int len;
            while((len = inputStream.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            bos.flush();
        } catch (Exception ignored) {
        }
    }

    /**
     * 释放资源文件
     *
     * @param plugin  所属插件
     * @param path    地址
     * @param replace 是否替换
     */
    public static void releaseResource(Plugin plugin, String path, boolean replace) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists() || replace) {
            inputStreamToFile(getResource(plugin, path), file);
        }
    }

    /**
     * 检测文件并创建
     *
     * @param file 文件
     */
    public static File createNewFile(File file) {
        if (file != null && !file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception ignored) {
            }
        }
        return file;
    }

    /**
     * 检测文件并创建（目录）
     *
     * @param file 文件
     */
    public static void createNewFileAndPath(File file) {
        if (!file.exists()) {
            String filePath = file.getPath();
            int index = filePath.lastIndexOf(File.separator);
            String folderPath;
            File folder;
            if ((index >= 0) && (!(folder = new File(filePath.substring(0, index))).exists())) {
                folder.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 创建并获取目录
     *
     * @param path 目录文件
     * @return
     */
    public static File folder(File path) {
        if (!path.exists()) {
            path.mkdirs();
        }
        return path;
    }

    /**
     * 创建并获取目录
     *
     * @param path 目录地址
     * @return
     */
    public static File folder(String path) {
        return folder(new File(path));
    }

    /**
     * 创建并获取文件
     *
     * @param path     目录
     * @param filePath 地址
     * @return
     */
    public static File file(File path, String filePath) {
        return createNewFile(new File(path, filePath));
    }

    /**
     * 创建并获取文件
     *
     * @param filePath 地址
     * @return {@link File}
     */
    public static File file(String filePath) {
        return createNewFile(new File(filePath));
    }

    /**
     * 删除文件夹
     *
     * @param file 文件夹
     */
    public static void deleteAllFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        for (File file1 : Objects.requireNonNull(file.listFiles())) {
            deleteAllFile(file1);
        }
        file.delete();
    }

    /**
     * 复制文件夹
     *
     * @param originFileName 文件1
     * @param targetFileName 文件2
     */
    public static void copyAllFile(String originFileName, String targetFileName) {
        File originFile = new File(originFileName);
        File targetFile = new File(targetFileName);
        if (!targetFile.exists()) {
            if (!originFile.isDirectory()) {
                createNewFile(targetFile);
            } else {
                targetFile.mkdirs();
            }
        }
        if (originFile.isDirectory()) {
            for (File file : Objects.requireNonNull(originFile.listFiles())) {
                if (file.isDirectory()) {
                    copyAllFile(file.getAbsolutePath(), targetFileName + "/" + file.getName());
                } else {
                    fileChannelCopy(file, new File(targetFileName + "/" + file.getName()));
                }
            }
        } else {
            fileChannelCopy(originFile, targetFile);
        }
    }

    /**
     * 复制文件（通道）
     *
     * @param file1 文件1
     * @param file2 文件2
     */
    public static void fileChannelCopy(File file1, File file2) {
        try (FileInputStream fileIn = new FileInputStream(file1);
             FileOutputStream fileOut = new FileOutputStream(file2);
             FileChannel channelIn = fileIn.getChannel();
             FileChannel channelOut = fileOut.getChannel()) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException ignored) {
        }
    }

    /**
     * 通过 URL 读取文本
     *
     * @param url 地址
     * @param def 默认值
     * @return 文本
     */
    public static String getStringFromURL(String url, String def) {
        String s = getStringFromURL(url, 1024);
        return s == null ? def : s;
    }

    /**
     * 通过 URL 读取文本
     *
     * @param url  地址
     * @param size 大小
     * @return 文本
     */
    public static String getStringFromURL(String url, int size) {
        URLConnection conn = null;
        BufferedInputStream bin = null;
        try {
            conn = new URL(url).openConnection();
            bin = new BufferedInputStream(conn.getInputStream());
            return getStringFromInputStream(bin, size, conn.getContentEncoding() == null ? "UTF-8" : conn.getContentEncoding());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(conn);
            IOUtils.closeQuietly(bin);
        }
        return null;
    }

    /**
     * 通过文件读取文本
     *
     * @param file   文件
     * @param size   大小
     * @param encode 编码
     * @return 文本
     */
    public static String getStringFromFile(File file, int size, String encode) {
        try (FileInputStream fin = new FileInputStream(file); BufferedInputStream bin = new BufferedInputStream(fin)) {
            return getStringFromInputStream(fin, size, encode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过输入流读取文本
     *
     * @param in     输入流
     * @param size   大小
     * @param encode 编码
     * @return 文本
     */
    public static String getStringFromInputStream(InputStream in, int size, String encode) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] b = new byte[size];
            int i;
            while ((i = in.read(b)) > 0) {
                bos.write(b, 0, i);
            }
            return new String(bos.toByteArray(), encode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载文件
     *
     * @param url  下载地址
     * @param file 下载位置
     */
    public static void download(String url, File file) {
        download(url, file, false);
    }

    /**
     * 下载文件
     *
     * @param url   下载地址
     * @param file  下载位置
     * @param async 是否异步
     */
    public static void download(String url, File file, boolean async) {
        EagletTask eagletTask = new EagletTask()
                .url(url)
                .file(file)
                .setThreads(8)
                .setOnError(event -> {
                })
                .setOnConnected(event -> TLocale.Logger.info("UTIL.DOWNLOAD-CONNECTED", file.getName(), ProgressEvent.format(event.getContentLength())))
                .setOnProgress(event -> TLocale.Logger.info("UTIL.DOWNLOAD-PROGRESS", event.getSpeedFormatted(), event.getPercentageFormatted()))
                .setOnComplete(event -> {
                    if (event.isSuccess()) {
                        TLocale.Logger.info("UTIL.DOWNLOAD-SUCCESS", file.getName());
                    } else {
                        TLocale.Logger.error("UTIL.DOWNLOAD-FAILED", file.getName());
                    }
                }).start();
        if (!async) {
            eagletTask.waitUntil();
        }
    }

    @Deprecated
    public static void download(String url, String filename, File saveDir) {
        download(url, new File(saveDir, filename));
    }

    @Deprecated
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignored) {
        }
    }

    @Deprecated
    public static byte[] read(InputStream in) {
        byte[] buffer = new byte[1024];
        int len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while ((len = in.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } catch (Exception ignored) {
        }
        return bos.toByteArray();
    }

    // *********************************
    //
    //         Private Methods
    //
    // *********************************

    private static Class getCaller(Class<?> obj) {
        try {
            return Class.forName(Thread.currentThread().getStackTrace()[3].getClassName(), false, obj.getClassLoader());
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}
