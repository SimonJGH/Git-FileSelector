package com.yds.scanfile.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yds.scanfile.R;

/**
 * @author YDS
 * @date 2020/12/4
 * @discribe 底部对话框
 */
@SuppressWarnings("all")
public class FunctionBottomDialog extends Dialog implements View.OnClickListener {

    private TextView tvBottomDialogTakeCamera, tvBottomDialogTakeVideo, tvBottomDialogAlbum, tvBottomDialogCancel;
    private View vBottomDialogTakeCamera, vBottomDialogTakeVideo;
    private OnItemClickLintener itemClickListener;

    private int type = 0;

    public FunctionBottomDialog(@NonNull Context context, int type) {
        this(context, R.style.normal_dialog, type);
        this.type = type;
    }

    public FunctionBottomDialog(@NonNull Context context, int themeResId, int type) {
        super(context, themeResId);
        this.type = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.layout_custom_dialog_function_bottom, null);

        tvBottomDialogTakeCamera = inflate.findViewById(R.id.tvBottomDialogTakeCamera);
        vBottomDialogTakeCamera = inflate.findViewById(R.id.vBottomDialogTakeCamera);
        tvBottomDialogTakeCamera.setOnClickListener(this);
        tvBottomDialogTakeVideo = inflate.findViewById(R.id.tvBottomDialogTakeVideo);
        vBottomDialogTakeVideo = inflate.findViewById(R.id.vBottomDialogTakeVideo);
        tvBottomDialogTakeVideo.setOnClickListener(this);
        tvBottomDialogAlbum = inflate.findViewById(R.id.tvBottomDialogAlbum);
        tvBottomDialogAlbum.setOnClickListener(this);
        tvBottomDialogCancel = inflate.findViewById(R.id.tvBottomDialogCancel);
        tvBottomDialogCancel.setOnClickListener(this);

        if (type == 0) {
            tvBottomDialogTakeVideo.setVisibility(View.GONE);
            vBottomDialogTakeVideo.setVisibility(View.GONE);
        } else if (type == 1) {
            tvBottomDialogTakeCamera.setVisibility(View.GONE);
            vBottomDialogTakeCamera.setVisibility(View.GONE);
        }

        Window window = getWindow();
        window.getDecorView().setPadding(0, 20, 0, 0); //消除边距
        window.setWindowAnimations(R.style.AnimBottom);
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(inflate);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvBottomDialogTakeCamera) {
            itemClickListener.onItemClick(0);
        } else if (id == R.id.tvBottomDialogTakeVideo) {
            itemClickListener.onItemClick(1);
        } else if (id == R.id.tvBottomDialogAlbum) {
            itemClickListener.onItemClick(2);
        } else if (id == R.id.tvBottomDialogCancel) {
            itemClickListener.onItemClick(5);
        }
    }

    //设置选项点击回调
    public void setOnItemClickListener(OnItemClickLintener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    //选项点击回调
    public interface OnItemClickLintener {

        //item点击
        void onItemClick(int position);
    }

}
