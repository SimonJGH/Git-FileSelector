package com.yds.scanfile;

import android.content.Context;

/**
 * @author YDS
 * @date 2021/6/7
 * @discribe 扫描文件功能
 */
public interface IScanFileFunctionListener {

    /**
     * @auther 于德水
     * created at 2021/6/4 10:22
     * 方法描述：扫描本地文件
     *
     * @param context
     * @param callBack
     */
    void scanFile(Context context, ScanFileCallBack callBack);

}
