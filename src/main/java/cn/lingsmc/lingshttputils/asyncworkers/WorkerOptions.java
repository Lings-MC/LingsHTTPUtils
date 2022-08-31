package cn.lingsmc.lingshttputils.asyncworkers;

import cn.lingsmc.lingshttputils.LingsHTTPUtils;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/30
 * @apiNote
 */
public class WorkerOptions {
    @Getter
    static boolean started = true;
    private static ExecutorService newFixedThreadPool;

    private WorkerOptions() {
    }

    public static void runWorkers() {
        started = true;
        Set<String> modules = LingsHTTPUtils.getInstance().getConfig().getKeys(false);
        newFixedThreadPool = Executors.newFixedThreadPool(modules.size());
        FileConfiguration config = LingsHTTPUtils.getInstance().getConfig();
        for (String module : modules) {
            if (Objects.equals(config.getString(String.format("%s.reqMode", module)), "Cycle")) {
                // 一定要为每个module设置一个worker
                AsyncWorkers asyncWorkers = new AsyncWorkers();
                final int reqTime = config.getInt(String.format("%s.reqTime", module));
                final String url = config.getString(String.format("%s.url", module));
                final String method;
                final int refInterval = config.getInt(String.format("%s.refInterval", module));
                if (config.getString(String.format("%s.method", module)) != null) {
                    method = config.getString(String.format("%s.method", module));
                } else {
                    method = "GET";
                }
                String[] keys = new String[0];
                if ("json".equals(config.getString(String.format("%s.mode", module)))) {
                    keys = config.getString(String.format("%s.key", module)).split("\\.");
                }
                String[] finalKeys = keys;
                Runnable runnable = () -> asyncWorkers.asyncworker(module, reqTime, url, method, refInterval, finalKeys);
                LingsHTTPUtils.getInstance().getLogger().info("尝试启动Cycle Worker...");
                newFixedThreadPool.execute(runnable);
            }
        }
    }

    public static void stopWorkers() {
        // TODO 修复无法停止线程的问题
        LingsHTTPUtils.getInstance().getLogger().info("尝试关闭Cycle Worker...");
        newFixedThreadPool.shutdownNow();
        started = false;
    }
}