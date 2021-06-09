package com.yds.scanfile;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.yds.scanfile.entity.MediaBean;
import com.yds.scanfile.views.FunctionBottomDialog;

import java.io.File;

/**
 * @author YDS
 * @date 2021/6/7
 * @discribe 扫码文件管理工具
 */
@SuppressWarnings("all")
public class ScanFileManager {
    public static final int REQUEST_FILE_CODE = 1990;
    public static final String REQUEST_FILE_KEY = "YDS_FILES";

    public static final int TAKE_PHOTOS_CODE = 199;
    public static MediaBean TAKE_PHOTOS_FILE = new MediaBean();            //拍照后保存文件路径

    public static final int TAKE_VIDEOS_CODE = 990;
    public static MediaBean TAKE_VIDEOS_FILE = new MediaBean();            //拍摄后保存文件路径
    private static int VIDEOS_RECORD_TIME = 10;

    /**
     * @auther 于德水
     * created at 2021/6/8 9:35
     * 方法描述：扫描本地文件 如果sdk大于29 储存策略发生变化，需强制分离。
     * 对外提供扫描文件资源用户可自定义页面样式
     */
    public static IScanFileFunctionListener scanLocalFile() {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion > 29) {
            return null;
        } else {
            return new ScanFileEngine();
        }
    }

    /**
     * @auther 于德水
     * created at 2021/6/8 10:33
     * 方法描述：选择文件-图片
     */
    public static void showBottomDialogPicture(Activity context) {
        bottomDialog(context, 0);
    }

    /**
     * @auther 于德水
     * created at 2021/6/8 10:33
     * 方法描述：选择文件-视频
     */
    public static void showBottomDialogVideo(Activity context) {
        bottomDialog(context, 1);
    }

    /**
     * @auther 于德水
     * created at 2021/6/8 14:32
     * 方法描述：处理dialog显示
     */
    /**
     * @param context
     * @param type    0-图片 1-视频
     */
    private static void bottomDialog(Activity context, int type) {
        FunctionBottomDialog bottomDialog = new FunctionBottomDialog(context, type);

        bottomDialog.setOnItemClickListener(new FunctionBottomDialog.OnItemClickLintener() {
            @Override
            public void onItemClick(int position) {
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;    // 获取版本号
                if (position == 0) {//拍摄照片
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    String fileName = System.currentTimeMillis() + ".jpg";       // 使用系统时间来对照片进行命名，保证唯一性
                    File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName); // 建立文件的保存路径
                    TAKE_PHOTOS_FILE.filePath = tempFile.getPath();
                    if (currentapiVersion < 24) { // Android 7.0 以下版本的设置方式
                        Uri fileUri = Uri.fromFile(tempFile);
                        // 配置了之后，在 onActivityResult 中返回的 data 为 null
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    } else { // 兼容 Android 7.0 使用共享文件的形式
                        ContentValues contentValues = new ContentValues(1);
                        contentValues.put(MediaStore.Images.Media.DATA, tempFile.getAbsolutePath());
                        Uri fileUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    }
                    context.startActivityForResult(intent, TAKE_PHOTOS_CODE);
                } else if (position == 1) {//拍摄视频
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    // 录制视频最大时长
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VIDEOS_RECORD_TIME);
                    // intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,10*1024*1024L);
                    // 使用系统时间来对照片进行命名，保证唯一性
                    String fileName = System.currentTimeMillis() + ".mp4";
                    // 建立文件的保存路径
                    File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
                    TAKE_VIDEOS_FILE.filePath = tempFile.getPath();
                    if (currentapiVersion < 24) { // Android 7.0 以下版本的设置方式
                        Uri fileUri = Uri.fromFile(tempFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    } else { // 兼容 Android 7.0 使用共享文件的形式
                        ContentValues contentValues = new ContentValues(1);
                        contentValues.put(MediaStore.Video.Media.DATA, tempFile.getAbsolutePath());
                        Uri fileUri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    }
                    context.startActivityForResult(intent, TAKE_VIDEOS_CODE);
                } else if (position == 2) {//调用相册
                    context.startActivityForResult(new Intent(context, ScanFileActivity.class), REQUEST_FILE_CODE);
                }
                bottomDialog.dismiss();
            }
        });
        bottomDialog.show();
    }

}
