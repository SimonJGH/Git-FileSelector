package com.yds.scanfile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.yds.scanfile.adapter.PreviewPagerAdapter;
import com.yds.scanfile.adapter.ScanFileAdapter;
import com.yds.scanfile.adapter.ScanFileViewHolder;
import com.yds.scanfile.entity.MediaBean;
import com.yds.scanfile.views.FunctionEasyDialog;
import com.yds.scanfile.utils.PopupWindowUtil;
import com.yds.scanfile.utils.ThreadHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther 于德水
 * created at 2021/6/2 11:26
 * 方法描述：扫描文件
 */
@SuppressWarnings("all")
public class ScanFileActivity extends AppCompatActivity implements View.OnClickListener {

    private ConstraintLayout mClScanFileHeader;
    private LinearLayout mLlScanFileFilter;
    private TextView mTvScanFileFilterName, mTvScanFilePreview, mTvScanFileConfirm;
    private RecyclerView mRecyclerViewFile;
    private ScanFileAdapter mScanFileAdapterFile;

    private int MAX_SELECT_NUM = 9;               //最大选择文件数量
    private int CURRENT_SELECT_NUM = 0;           //当前文件选择数量
    private int ALL_FILE_NUM = 0;                 //所有文件数量
    private int previewPosition = 0;              //浏览全部文件下标
    private int previewIndex = 0;                 //浏览选择文件下标

    private Map<String, MediaBean> mMapSelectedFile = new LinkedHashMap<>();            //有序的已选择文件存储集合

    private List<MediaBean> mListScanFile = new ArrayList<>();                           //搜索的所有文件
    private HashMap<String, List<MediaBean>> mMapScanFileFolder = new LinkedHashMap<>(); //搜索文件所在的文件夹名称及对应文件
    private List<String> mListFilterTemp = new ArrayList<>();                            //筛选文件夹列表
    private HashMap<String, Boolean> mMapSelectFolder = new HashMap<>();                 //已选中筛选文件夹

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_file);

        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion > 29) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setMessage("暂不支持大于29的SDK版本");
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.show();
        } else {
            requestPermissions();
        }

    }

    /**
     * @auther 于德水
     * created at 2021/6/4 11:31
     * 方法描述：请求必要权限
     */
    private void requestPermissions() {
        PermissionX.init(ScanFileActivity.this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            findViewByIds();

                            initFileRecyclerView();
                            startScan();
                        } else {
                            Toast.makeText(ScanFileActivity.this, "如无权限，功能无法使用。", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    private void findViewByIds() {
        mRecyclerViewFile = findViewById(R.id.recyclerViewFile);
        mClScanFileHeader = findViewById(R.id.clScanFileHeader);
        mLlScanFileFilter = findViewById(R.id.llScanFileFilter);
        mLlScanFileFilter.setOnClickListener(this);
        mTvScanFileFilterName = findViewById(R.id.tvScanFileFilterName);
        mTvScanFilePreview = findViewById(R.id.tvScanFilePreview);
        mTvScanFilePreview.setOnClickListener(this);
        mTvScanFileConfirm = findViewById(R.id.tvScanFileConfirm);
        mTvScanFileConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llScanFileFilter) {
            //文件夹筛选
            initFilterPop();
        } else if (id == R.id.tvScanFilePreview) {
            //预览
            previewSelectedImageDialog();
        } else if (id == R.id.tvScanFileConfirm) {
            //完成
            confirmDone();
        }
    }

    /**
     * @auther 于德水
     * created at 2021/6/2 11:18
     * 方法描述：展示文件列表
     */
    private void initFileRecyclerView() {
        GridLayoutManager llm = new GridLayoutManager(ScanFileActivity.this, 4);
        mRecyclerViewFile.setLayoutManager(llm);
        // 如果Item够简单，高度是确定的，打开FixSize将提高性能
        mRecyclerViewFile.setHasFixedSize(true);
        // 设置Item默认动画，加也行，不加也行
        mRecyclerViewFile.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewFile.setNestedScrollingEnabled(false);

        mScanFileAdapterFile = new ScanFileAdapter(ScanFileActivity.this, R.layout.layout_scan_file_item, mListScanFile);
        mScanFileAdapterFile.setItemDatasListener(new ScanFileAdapter.ItemDatasListener<MediaBean>() {
            @Override
            public void setItemDatas(ScanFileViewHolder holder, MediaBean bean, int position) {
                ImageView ivScanFileCover = holder.getView(R.id.ivScanFileCover);
                TextView tvScanFileSelected = holder.getView(R.id.tvScanFileSelected);
                TextView tvScanFileName = holder.getView(R.id.tvScanFileName);

                String path = bean.filePath;
                tvScanFileName.setText(bean.fileName);
                if (path.endsWith(".txt")) {
                    ivScanFileCover.setImageResource(R.mipmap.txt);
                    tvScanFileName.setVisibility(View.VISIBLE);
                } else if (path.endsWith(".xls")) {
                    ivScanFileCover.setImageResource(R.mipmap.xls);
                    tvScanFileName.setVisibility(View.VISIBLE);
                } else if (path.endsWith(".doc")) {
                    ivScanFileCover.setImageResource(R.mipmap.doc);
                    tvScanFileName.setVisibility(View.VISIBLE);
                } else if (path.endsWith(".mp4")) {
                    Uri uri = Uri.fromFile(new File(path));
                    Glide.with(ScanFileActivity.this).load(uri).into(ivScanFileCover);
                    tvScanFileName.setVisibility(View.GONE);
                } else if (path.endsWith(".png") || path.endsWith(".jpg")) {
                    Glide.with(ScanFileActivity.this).load(new File(path)).error(R.drawable.rect_image_broken).into(ivScanFileCover);
                    tvScanFileName.setVisibility(View.GONE);
                }

                //选择文件点击事件
                MediaBean mediaBeanTemp = mMapSelectedFile.get(bean.filePath);
                if (mediaBeanTemp != null) {
                    tvScanFileSelected.setText(mediaBeanTemp.selectFileIndex + "");
                    tvScanFileSelected.setBackgroundResource(R.drawable.rect_file_select_num_selected);
                } else {
                    tvScanFileSelected.setText("");
                    tvScanFileSelected.setBackgroundResource(R.drawable.rect_file_select_num_unselected);
                }
                tvScanFileSelected.setOnClickListener(view -> {
                    MediaBean mediaBean = mMapSelectedFile.get(bean.filePath);
                    if (mediaBean == null) {
                        if (CURRENT_SELECT_NUM >= MAX_SELECT_NUM) {
                            Toast.makeText(ScanFileActivity.this, "你最多只能选择" + MAX_SELECT_NUM + "张图片", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mMapSelectedFile.put(bean.filePath, bean);
                        //更新选择数量
                        CURRENT_SELECT_NUM = mMapSelectedFile.size();
                        //设置选择文件下标
                        bean.selectFileIndex = CURRENT_SELECT_NUM;
                    } else {
                        mMapSelectedFile.remove(bean.filePath);
                        //更新选择数量
                        CURRENT_SELECT_NUM = mMapSelectedFile.size();
                        //修改其它已选择文件下标
                        Iterator<Map.Entry<String, MediaBean>> iterator = mMapSelectedFile.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, MediaBean> entry = iterator.next();
                            String key = entry.getKey();
                            MediaBean value = entry.getValue();
                            if (value.selectFileIndex > mediaBean.selectFileIndex) {
                                value.selectFileIndex -= 1;
                            }

                            int notifyIndex = 0;
                            for (int i = 0; i < mListScanFile.size(); i++) {
                                if (mListScanFile.get(i).filePath.equals(key)) {
                                    notifyIndex = i;
                                }
                            }
                            mScanFileAdapterFile.notifyItemChanged(notifyIndex);
                        }
                        //设置取消选择文件下标
                        mediaBean.selectFileIndex = -1;
                    }
                    //更新右上角文件数量
                    mTvScanFileConfirm.setSelected(CURRENT_SELECT_NUM == 0 ? false : true);
                    mTvScanFileConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
                    mScanFileAdapterFile.notifyItemChanged(position);
                });

            }
        });

        mScanFileAdapterFile.setOnItemClickListener(new ScanFileAdapter.OnItemClickListener() {

            @Override
            public void setOnItemClickListener(View view, int position) {
                previewAllImageDialog(position);
            }

            @Override
            public void setOnItemLongClickListener(View view, int position) {
            }
        });

        mScanFileAdapterFile.setHasStableIds(true);
        mRecyclerViewFile.setAdapter(mScanFileAdapterFile);
    }

    /**
     * @auther 于德水
     * created at 2021/6/4 10:22
     * 方法描述：考试扫描资源文件
     */
    private void startScan() {
        ThreadHelper.getInstance().initFixThread(new Runnable() {
            @Override
            public void run() {
               ScanFileManager.scanLocalFile().scanFile(ScanFileActivity.this, new ScanFileCallBack() {
                   @Override
                   public void scanFileResult(HashMap<String, List<MediaBean>> result) {
                       mMapScanFileFolder.putAll(result);
                   }
               });
            }
        }, new ThreadHelper.ThreadRunDoneListener() {
            @Override
            public void threadRunResult(boolean result) {
                if (result) {
                    mScanFileAdapterFile.notifyDataSetChanged();

                    //转换数据
                    Iterator<Map.Entry<String, List<MediaBean>>> iterator = mMapScanFileFolder.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, List<MediaBean>> entry = iterator.next();
                        String key = entry.getKey();
                        mListFilterTemp.add(key);
                        mMapSelectFolder.put(key, false);
                        //计算全部文件数量
                        if (entry.getValue() != null) {
                            ALL_FILE_NUM += entry.getValue().size();
                        }

                        //用于展示相册初始化界面
                        mListScanFile.addAll(entry.getValue());
                    }
                }
            }
        });
    }

    /**
     * @auther 于德水
     * created at 2021/6/4 10:21
     * 方法描述：预览筛选条件-文件夹
     */
    private void initFilterPop() {
        View inflate = LayoutInflater.from(ScanFileActivity.this).inflate(R.layout.pop_scan_file_filter, null);
        RecyclerView rvFilter = inflate.findViewById(R.id.rvFilter);

        LinearLayoutManager llm = new LinearLayoutManager(ScanFileActivity.this);
        rvFilter.setLayoutManager(llm);
        // 如果Item够简单，高度是确定的，打开FixSize将提高性能
        rvFilter.setHasFixedSize(true);
        // 设置Item默认动画，加也行，不加也行
        rvFilter.setItemAnimator(new DefaultItemAnimator());
        rvFilter.setNestedScrollingEnabled(false);

        ScanFileAdapter mScanFileAdapterFilter = new ScanFileAdapter(ScanFileActivity.this, R.layout.adapter_pop_filter_item, mListFilterTemp);
        mScanFileAdapterFilter.setItemDatasListener(new ScanFileAdapter.ItemDatasListener<String>() {
            @Override
            public void setItemDatas(ScanFileViewHolder holder, String bean, int position) {
                ImageView ivPopFilterIcon = holder.getView(R.id.ivPopFilterIcon);
                TextView tvPopFilterTitle = holder.getView(R.id.tvPopFilterTitle);
                ImageView ivPopFilterSelectFlag = holder.getView(R.id.ivPopFilterSelectFlag);

                //展示该文件夹第一个文件
                List<MediaBean> mediaBeanList = mMapScanFileFolder.get(bean);
                if (mediaBeanList != null && !mediaBeanList.isEmpty()) {
                    Glide.with(ScanFileActivity.this)
                            .load(new File(mediaBeanList.get(0).filePath))
                            .error(R.drawable.rect_image_broken)
                            .into(ivPopFilterIcon);

                    String substring = bean.substring(bean.lastIndexOf("/") + 1, bean.length());
                    tvPopFilterTitle.setText(substring + " (" + mediaBeanList.size() + ")");
                } else {
                    tvPopFilterTitle.setText("全部" + " (" + ALL_FILE_NUM + ")");
                }


                //标记已选文件夹
                Boolean aBoolean = mMapSelectFolder.get(bean);
                if (aBoolean) {
                    ivPopFilterSelectFlag.setVisibility(View.VISIBLE);
                } else {
                    ivPopFilterSelectFlag.setVisibility(View.INVISIBLE);
                }

            }
        });

        mScanFileAdapterFilter.setOnItemClickListener(new ScanFileAdapter.OnItemClickListener() {

            @Override
            public void setOnItemClickListener(View view, int position) {
                Iterator<Map.Entry<String, List<MediaBean>>> iterator = mMapScanFileFolder.entrySet().iterator();
                String folderName = mListFilterTemp.get(position);
                Boolean aBoolean = mMapSelectFolder.get(folderName);
                //清空文件列表
                mListScanFile.clear();
                //标记筛选文件夹
                if (aBoolean) {
                    mMapSelectFolder.put(folderName, false);
                    //重置文件列表
                    while (iterator.hasNext()) {
                        Map.Entry<String, List<MediaBean>> entry = iterator.next();
                        mListScanFile.addAll(entry.getValue());
                    }
                    mTvScanFileFilterName.setText("全部");
                } else {
                    for (String s : mListFilterTemp) {
                        mMapSelectFolder.put(s, false);
                    }
                    mMapSelectFolder.put(folderName, true);
                    //重置文件列表
                    String substring = folderName.substring(folderName.lastIndexOf("/") + 1, folderName.length());
                    mTvScanFileFilterName.setText(substring);
                    if (substring.equals("全部")) {
                        while (iterator.hasNext()) {
                            Map.Entry<String, List<MediaBean>> entry = iterator.next();
                            mListScanFile.addAll(entry.getValue());
                        }
                    } else {
                        mListScanFile.addAll(mMapScanFileFolder.get(folderName));
                    }
                }
                // mCommonEmptyAdapterFilter.notifyDataSetChanged();
                mScanFileAdapterFile.notifyDataSetChanged();
                PopupWindowUtil.getInstance().closePop();
            }

            @Override
            public void setOnItemLongClickListener(View view, int position) {
            }
        });

        mScanFileAdapterFilter.setHasStableIds(true);
        rvFilter.setAdapter(mScanFileAdapterFilter);

        PopupWindowUtil.getInstance().createScalePopupWindow(ScanFileActivity.this, inflate, mClScanFileHeader);
    }

    /**
     * 预览所有文件dialog
     */
    private void previewAllImageDialog(int position) {
        previewPosition = position;
        View inflate = LayoutInflater.from(ScanFileActivity.this).inflate(R.layout.dialog_preview_image, null);
        ViewPager mVpPreviewImage = inflate.findViewById(R.id.mVpPreviewImage);
        TextView mTvPreviewPosition = inflate.findViewById(R.id.mTvPreviewPosition);
        TextView mTvPreviewConfirm = inflate.findViewById(R.id.mTvPreviewConfirm);
        mTvPreviewConfirm.setSelected(true);
        CheckBox mCbPreviewSelected = inflate.findViewById(R.id.mCbPreviewSelected);

        // 返回点击事件
        inflate.findViewById(R.id.mIvPreviewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FunctionEasyDialog.getInstance().exitDialog();
            }
        });

        // 确定点击事件
        mTvPreviewConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
        mTvScanFileConfirm.setSelected(CURRENT_SELECT_NUM == 0 ? false : true);
        mTvScanFileConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
        mTvPreviewConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapSelectedFile.isEmpty()) {
                    MediaBean bean = mListScanFile.get(previewPosition);
                    mMapSelectedFile.put(bean.filePath, bean);
                    mTvPreviewConfirm.setText("完成(" + 1 + "/" + MAX_SELECT_NUM + ")");
                }
                ThreadHelper.getInstance().mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FunctionEasyDialog.getInstance().exitDialog();
                        confirmDone();
                    }
                }, 500);
            }
        });

        //图片浏览
        PreviewPagerAdapter previewAdapter = new PreviewPagerAdapter(ScanFileActivity.this, mListScanFile, new PreviewPagerAdapter.PreviewImageCallBack<Boolean>() {
            @Override
            public void receiveMsg(Boolean flag) {
                if (flag) {
                    FunctionEasyDialog.getInstance().exitDialog();
                }
            }
        });
        mVpPreviewImage.setAdapter(previewAdapter);
        mTvPreviewPosition.setText((position + 1) + "/" + mListScanFile.size());
        mVpPreviewImage.setCurrentItem(position, false);
        mVpPreviewImage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                previewPosition = position;
                MediaBean bean = mListScanFile.get(position);
                MediaBean mediaBean = mMapSelectedFile.get(bean.filePath);
                mCbPreviewSelected.setChecked(mediaBean == null ? false : true);
                mTvPreviewPosition.setText((position + 1) + "/" + mListScanFile.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //浏览时选择与取消选择文件
        mCbPreviewSelected.setChecked(mMapSelectedFile.get(mListScanFile.get(position).filePath) == null ? false : true);
        mCbPreviewSelected.setOnClickListener(view -> {
            MediaBean bean = mListScanFile.get(previewPosition);
            MediaBean mediaBean = mMapSelectedFile.get(mListScanFile.get(previewPosition).filePath);
            if (mediaBean == null) {
                if (CURRENT_SELECT_NUM >= MAX_SELECT_NUM) {
                    Toast.makeText(ScanFileActivity.this, "你最多只能选择" + MAX_SELECT_NUM + "张图片", Toast.LENGTH_SHORT).show();
                    mCbPreviewSelected.setChecked(false);
                    return;
                }
                mMapSelectedFile.put(bean.filePath, bean);
                //更新选择数量
                CURRENT_SELECT_NUM = mMapSelectedFile.size();
                //设置选择文件下标
                bean.selectFileIndex = CURRENT_SELECT_NUM;
            } else {
                mMapSelectedFile.remove(mediaBean.filePath);
                //更新选择数量
                CURRENT_SELECT_NUM = mMapSelectedFile.size();
                //修改其它已选择文件下标
                Iterator<Map.Entry<String, MediaBean>> iterator = mMapSelectedFile.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, MediaBean> entry = iterator.next();
                    String key = entry.getKey();
                    MediaBean value = entry.getValue();
                    if (value.selectFileIndex > mediaBean.selectFileIndex) {
                        value.selectFileIndex -= 1;
                    }

                    int notifyIndex = 0;
                    for (int i = 0; i < mListScanFile.size(); i++) {
                        if (mListScanFile.get(i).filePath.equals(key)) {
                            notifyIndex = i;
                        }
                    }
                    mScanFileAdapterFile.notifyItemChanged(notifyIndex);
                }
                //设置取消选择文件下标
                mediaBean.selectFileIndex = -1;
            }

            mScanFileAdapterFile.notifyItemChanged(previewPosition);
            mTvPreviewConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
            mTvScanFileConfirm.setSelected(CURRENT_SELECT_NUM == 0 ? false : true);
            mTvScanFileConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
        });

        FunctionEasyDialog.getInstance().createDialog(ScanFileActivity.this, inflate, Gravity.CENTER, 1.0, 1.0, false);
    }

    /**
     * 预览选中文件dialog
     */
    private void previewSelectedImageDialog() {
        if (mMapSelectedFile.isEmpty()) return;
        //首次进来默认展示第一个文件
        previewIndex = 0;
        //构建已选文件列表
        List<MediaBean> mListSelectedFile = new ArrayList<>();
        Iterator<Map.Entry<String, MediaBean>> iterator = mMapSelectedFile.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MediaBean> entry = iterator.next();
            mListSelectedFile.add(entry.getValue());
        }

        View inflate = LayoutInflater.from(ScanFileActivity.this).inflate(R.layout.dialog_preview_image, null);
        ViewPager mVpPreviewImage = inflate.findViewById(R.id.mVpPreviewImage);
        TextView mTvPreviewPosition = inflate.findViewById(R.id.mTvPreviewPosition);
        TextView mTvPreviewConfirm = inflate.findViewById(R.id.mTvPreviewConfirm);
        mTvPreviewConfirm.setSelected(true);
        CheckBox mCbPreviewSelected = inflate.findViewById(R.id.mCbPreviewSelected);

        // 返回点击事件
        inflate.findViewById(R.id.mIvPreviewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FunctionEasyDialog.getInstance().exitDialog();
            }
        });

        // 确定点击事件
        mTvPreviewConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
        mTvScanFileConfirm.setSelected(CURRENT_SELECT_NUM == 0 ? false : true);
        mTvScanFileConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
        mTvPreviewConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FunctionEasyDialog.getInstance().exitDialog();
                confirmDone();
            }
        });

        //图片浏览
        PreviewPagerAdapter previewAdapter = new PreviewPagerAdapter(ScanFileActivity.this, mListSelectedFile, new PreviewPagerAdapter.PreviewImageCallBack<Boolean>() {
            @Override
            public void receiveMsg(Boolean flag) {
                if (flag) {
                    FunctionEasyDialog.getInstance().exitDialog();
                }
            }
        });
        mVpPreviewImage.setAdapter(previewAdapter);
        mTvPreviewPosition.setText((previewIndex + 1) + "/" + mListSelectedFile.size());
        mVpPreviewImage.setCurrentItem(previewIndex, false);
        mVpPreviewImage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                previewIndex = position;
                MediaBean bean = mListSelectedFile.get(position);
                MediaBean mediaBean = mMapSelectedFile.get(bean.filePath);
                mCbPreviewSelected.setChecked(mediaBean == null ? false : true);
                mTvPreviewPosition.setText((position + 1) + "/" + mListSelectedFile.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //浏览时选择与取消选择文件
        mCbPreviewSelected.setChecked(mMapSelectedFile.get(mListSelectedFile.get(previewIndex).filePath) == null ? false : true);
        mCbPreviewSelected.setOnClickListener(view -> {
            MediaBean bean = mListSelectedFile.get(previewIndex);
            MediaBean mediaBean = mMapSelectedFile.get(mListSelectedFile.get(previewIndex).filePath);
            if (mediaBean == null) {
                if (CURRENT_SELECT_NUM >= MAX_SELECT_NUM) {
                    Toast.makeText(ScanFileActivity.this, "你最多只能选择" + MAX_SELECT_NUM + "张图片", Toast.LENGTH_SHORT).show();
                    mCbPreviewSelected.setChecked(false);
                    return;
                }
                mMapSelectedFile.put(bean.filePath, bean);
                //更新选择数量
                CURRENT_SELECT_NUM = mMapSelectedFile.size();
                //设置选择文件下标
                bean.selectFileIndex = CURRENT_SELECT_NUM;
            } else {
                mMapSelectedFile.remove(mediaBean.filePath);
                //更新选择数量
                CURRENT_SELECT_NUM = mMapSelectedFile.size();
                //修改其它已选择文件下标
                Iterator<Map.Entry<String, MediaBean>> iteratorTemp = mMapSelectedFile.entrySet().iterator();
                while (iteratorTemp.hasNext()) {
                    Map.Entry<String, MediaBean> entry = iteratorTemp.next();
                    String key = entry.getKey();
                    MediaBean value = entry.getValue();
                    if (value.selectFileIndex > mediaBean.selectFileIndex) {
                        value.selectFileIndex -= 1;
                    }

                    int notifyIndex = 0;
                    for (int i = 0; i < mListScanFile.size(); i++) {
                        if (mListScanFile.get(i).filePath.equals(key)) {
                            notifyIndex = i;
                        }
                    }
                    //mCommonEmptyAdapterFile.notifyItemChanged(notifyIndex);
                }
                //设置取消选择文件下标
                mediaBean.selectFileIndex = -1;
            }
            mScanFileAdapterFile.notifyDataSetChanged();
            //mCommonEmptyAdapterFile.notifyItemChanged(previewPosition);
            mTvPreviewConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
            mTvScanFileConfirm.setSelected(CURRENT_SELECT_NUM == 0 ? false : true);
            mTvScanFileConfirm.setText("完成(" + CURRENT_SELECT_NUM + "/" + MAX_SELECT_NUM + ")");
        });

        FunctionEasyDialog.getInstance().createDialog(ScanFileActivity.this, inflate, Gravity.CENTER, 1.0, 1.0, false);
    }

    /**
     * @auther 于德水
     * created at 2021/6/7 15:38
     * 方法描述：完成确认选择
     */
    private void confirmDone() {
        Intent intent = new Intent();
        ArrayList<MediaBean> list = new ArrayList<>();
        Iterator<Map.Entry<String, MediaBean>> iterator = mMapSelectedFile.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MediaBean> entry = iterator.next();
            list.add(entry.getValue());
        }
        intent.putExtra("YDS_FILES", list);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}