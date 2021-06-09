package com.yds.scanfile;

import com.yds.scanfile.entity.MediaBean;

import java.util.HashMap;
import java.util.List;

/**
 * @author YDS
 * @date 2021/6/7
 * @discribe 扫描文件结果回调
 */
public interface ScanFileCallBack {

    void scanFileResult(HashMap<String, List<MediaBean>> result);

}
