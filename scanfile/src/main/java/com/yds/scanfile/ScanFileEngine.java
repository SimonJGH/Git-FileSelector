package com.yds.scanfile;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.yds.scanfile.entity.MediaBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author YDS
 * @date 2021/6/7
 * @discribe 扫描文件引擎
 */
@SuppressWarnings("all")
public class ScanFileEngine implements IScanFileFunctionListener {
    private HashMap<String, List<MediaBean>> mMapScanFileFolder = new LinkedHashMap<>(); //搜索文件所在的文件夹名称及对应文件

    @Override
    public void scanFile(Context context, ScanFileCallBack callBack) {
        mMapScanFileFolder.put("全部/全部", new ArrayList<MediaBean>());

        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projImage = {MediaStore.Images.Media._ID
                , MediaStore.Images.Media.DATA
                , MediaStore.Images.Media.SIZE
                , MediaStore.Images.Media.DISPLAY_NAME};
        Cursor mCursor = context.getContentResolver().query(mImageUri,
                projImage,
                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED + " desc");

        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                // 获取图片的路径
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                int size = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.SIZE)) / 1024;
                String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));

                // 获取该图片的父路径名
                String dirPath = new File(path).getParentFile().getAbsolutePath();
                //存储对应关系
                if (mMapScanFileFolder.containsKey(dirPath)) {
                    List<MediaBean> data = mMapScanFileFolder.get(dirPath);
                    data.add(new MediaBean(MediaBean.Type.Image, path, size, displayName));
                    continue;
                } else {
                    List<MediaBean> data = new ArrayList<>();
                    data.add(new MediaBean(MediaBean.Type.Image, path, size, displayName));
                    mMapScanFileFolder.put(dirPath, data);
                }
            }
            mCursor.close();
        }

        callBack.scanFileResult(mMapScanFileFolder);
    }

}
