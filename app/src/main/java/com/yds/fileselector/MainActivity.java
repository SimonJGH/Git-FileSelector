package com.yds.fileselector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.yds.scanfile.ScanFileManager;
import com.yds.scanfile.adapter.ScanFileViewHolder;
import com.yds.scanfile.adapter.ScanFileAdapter;
import com.yds.fileselector.databinding.ActivityMainBinding;
import com.yds.scanfile.entity.MediaBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private ScanFileAdapter mScanFileAdapterFile;
    private List<MediaBean> mListScanFile = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initClickListener();
        initFileRecyclerView();
    }

    private void initClickListener() {
        binding.btBrowseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanFileManager.showBottomDialogPicture(MainActivity.this);
//                ScanFileManager.showBottomDialogVideo(MainActivity.this);
            }
        });
    }

    private void initFileRecyclerView() {
        GridLayoutManager llm = new GridLayoutManager(MainActivity.this, 3);
        binding.recyclerViewShow.setLayoutManager(llm);
        // 如果Item够简单，高度是确定的，打开FixSize将提高性能
        binding.recyclerViewShow.setHasFixedSize(true);
        // 设置Item默认动画，加也行，不加也行
        binding.recyclerViewShow.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerViewShow.setNestedScrollingEnabled(false);

        mScanFileAdapterFile = new ScanFileAdapter(MainActivity.this, R.layout.layout_scan_file_item, mListScanFile);
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
                    Glide.with(MainActivity.this).load(uri).into(ivScanFileCover);
                    tvScanFileName.setVisibility(View.GONE);
                } else if (path.endsWith(".png") || path.endsWith(".jpg")) {
                    Glide.with(MainActivity.this).load(new File(path)).error(R.drawable.rect_image_broken).into(ivScanFileCover);
                    tvScanFileName.setVisibility(View.GONE);
                }

            }
        });

        mScanFileAdapterFile.setOnItemClickListener(new ScanFileAdapter.OnItemClickListener() {

            @Override
            public void setOnItemClickListener(View view, int position) {
                MediaBean mediaBean = mListScanFile.get(position);

                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, new File(mediaBean.filePath).getAbsolutePath());
                Uri fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setDataAndType(fileUri, "image/*");
                startActivity(it);
            }

            @Override
            public void setOnItemLongClickListener(View view, int position) {
            }
        });

        mScanFileAdapterFile.setHasStableIds(true);
        binding.recyclerViewShow.setAdapter(mScanFileAdapterFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ScanFileManager.REQUEST_FILE_CODE) {
                Bundle bundle = data.getExtras();
                if (bundle == null) return;
                mListScanFile.clear();
                ArrayList<MediaBean> files = (ArrayList<MediaBean>) bundle.getSerializable(ScanFileManager.REQUEST_FILE_KEY);
                mListScanFile.addAll(files);
                mScanFileAdapterFile.notifyDataSetChanged();
            } else if (requestCode == ScanFileManager.TAKE_PHOTOS_CODE) {
                mListScanFile.clear();
                mListScanFile.add(ScanFileManager.TAKE_PHOTOS_FILE);
                mScanFileAdapterFile.notifyDataSetChanged();
            } else if (requestCode == ScanFileManager.TAKE_VIDEOS_CODE) {
                mListScanFile.clear();
                mListScanFile.add(ScanFileManager.TAKE_VIDEOS_FILE);
                mScanFileAdapterFile.notifyDataSetChanged();
            }
        }
    }

}