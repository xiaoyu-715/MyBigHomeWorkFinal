package com.example.mybighomework.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 应用级线程池管理类
 * 统一管理所有后台任务的执行，避免创建过多线程
 * 
 * 使用方式：
 * AppExecutorsYSJ.getInstance().diskIO().execute(() -> {
 *     // 数据库操作
 * });
 * 
 * AppExecutorsYSJ.getInstance().mainThread().execute(() -> {
 *     // UI更新
 * });
 */
public class AppExecutorsYSJ {
    
    private static volatile AppExecutorsYSJ INSTANCE;
    
    // 磁盘IO线程池（单线程，保证数据库操作顺序）
    private final ExecutorService diskIO;
    
    // 网络IO线程池（多线程，支持并发请求）
    private final ExecutorService networkIO;
    
    // 主线程执行器
    private final Executor mainThread;
    
    // 定时任务线程池
    private final ScheduledExecutorService scheduler;
    
    // 计算密集型任务线程池
    private final ExecutorService computation;
    
    private AppExecutorsYSJ() {
        // 单线程用于数据库操作，保证顺序执行
        this.diskIO = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DiskIO-Thread");
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });
        
        // 固定大小线程池用于网络请求
        this.networkIO = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r, "NetworkIO-Thread");
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });
        
        // 主线程执行器
        this.mainThread = new MainThreadExecutor();
        
        // 定时任务调度器
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Scheduler-Thread");
            t.setDaemon(true);
            return t;
        });
        
        // CPU核心数决定计算线程池大小
        int cpuCount = Runtime.getRuntime().availableProcessors();
        this.computation = Executors.newFixedThreadPool(cpuCount, r -> {
            Thread t = new Thread(r, "Computation-Thread");
            t.setPriority(Thread.NORM_PRIORITY + 1);
            return t;
        });
    }
    
    public static AppExecutorsYSJ getInstance() {
        if (INSTANCE == null) {
            synchronized (AppExecutorsYSJ.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppExecutorsYSJ();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * 磁盘IO操作（数据库读写）
     */
    public ExecutorService diskIO() {
        return diskIO;
    }
    
    /**
     * 网络IO操作
     */
    public ExecutorService networkIO() {
        return networkIO;
    }
    
    /**
     * 主线程操作（UI更新）
     */
    public Executor mainThread() {
        return mainThread;
    }
    
    /**
     * 定时任务调度
     */
    public ScheduledExecutorService scheduler() {
        return scheduler;
    }
    
    /**
     * 计算密集型任务
     */
    public ExecutorService computation() {
        return computation;
    }
    
    /**
     * 延迟执行任务
     */
    public void executeDelayed(Runnable task, long delayMillis) {
        scheduler.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 在磁盘IO线程执行，完成后在主线程回调
     */
    public <T> void executeWithCallback(
            @NonNull IOTask<T> backgroundTask,
            @NonNull MainCallback<T> mainCallback) {
        
        diskIO.execute(() -> {
            try {
                T result = backgroundTask.execute();
                mainThread.execute(() -> mainCallback.onComplete(result));
            } catch (Exception e) {
                mainThread.execute(() -> mainCallback.onError(e));
            }
        });
    }
    
    /**
     * 关闭所有线程池（在Application销毁时调用）
     */
    public void shutdown() {
        diskIO.shutdown();
        networkIO.shutdown();
        scheduler.shutdown();
        computation.shutdown();
    }
    
    /**
     * 主线程执行器实现
     */
    private static class MainThreadExecutor implements Executor {
        private final Handler mainHandler = new Handler(Looper.getMainLooper());
        
        @Override
        public void execute(@NonNull Runnable command) {
            mainHandler.post(command);
        }
    }
    
    /**
     * 后台任务接口
     */
    public interface IOTask<T> {
        T execute() throws Exception;
    }
    
    /**
     * 主线程回调接口
     */
    public interface MainCallback<T> {
        void onComplete(T result);
        default void onError(Exception e) {
            e.printStackTrace();
        }
    }
}
