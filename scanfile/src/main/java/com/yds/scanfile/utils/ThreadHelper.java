package com.yds.scanfile.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author YDS
 * @date 2021/5/28
 * @discribe 线程管理
 */
@SuppressWarnings("all")
public class ThreadHelper {
    public final Handler mHandler;
    private ExecutorService executorService;
    private Future<?> future;

    private ThreadHelper() {
        mHandler = new Handler(Looper.myLooper());
    }

    public static ThreadHelper getInstance() {
        return SafeMode.helper;
    }

    private static class SafeMode {
        private static final ThreadHelper helper = new ThreadHelper();
    }

    /**
     * @auther 于德水
     * created at 2021/5/31 11:46
     * 方法描述：单线程池
     */
    public void initSingleThread(Runnable runnable, ThreadRunDoneListener listener) {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        future = executorService.submit(runnable);
        isThreadRunDone(listener);
    }

    /**
     * @auther 于德水
     * created at 2021/5/31 11:46
     * 方法描述：固定线程池-4
     */
    public void initFixThread(Runnable runnable, ThreadRunDoneListener listener) {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(4);
        }

        future = executorService.submit(runnable);
        isThreadRunDone(listener);
    }

    /**
     * @auther 于德水
     * created at 2021/5/31 10:09
     * 方法描述：线程是否运行完成
     */
    private void isThreadRunDone(ThreadRunDoneListener mThreadRunDoneListener) {
        boolean done = future.isDone();
        if (mThreadRunDoneListener != null) {
            mThreadRunDoneListener.threadRunResult(done);
        }
        //如果未完成100毫秒检测一次
        if (!done) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isThreadRunDone(mThreadRunDoneListener);
                }
            }, 100);
        }
    }

    /**
     * @auther 于德水
     * created at 2021/5/31 11:47
     * 方法描述：线程池结果回调
     */
    public interface ThreadRunDoneListener {
        void threadRunResult(boolean result);
    }

    /**
     * @auther 于德水
     * created at 2021/5/31 11:49
     * 方法描述：销毁线程池
     */
    public void destroyThread() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

}
