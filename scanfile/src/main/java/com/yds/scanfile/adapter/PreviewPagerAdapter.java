package com.yds.scanfile.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.yds.scanfile.R;
import com.yds.scanfile.entity.MediaBean;
import com.yds.scanfile.views.GestureImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片预览-适配器
 * Created by Simon on 2017/10/11.
 */
@SuppressWarnings("all")
public class PreviewPagerAdapter extends PagerAdapter {
    private List<ImageView> views = new ArrayList<>();

    public PreviewPagerAdapter(Context context, List<MediaBean> files, final PreviewImageCallBack<Boolean> callBack) {
        for (MediaBean bean : files) {
            GestureImageView imageview = new GestureImageView(context);
            Uri uri = Uri.fromFile(new File(bean.filePath));
            Glide.with(context).load(uri).error(R.drawable.rect_image_broken).into(imageview);
            imageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callBack.receiveMsg(true);
                }
            });
            views.add(imageview);
        }
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object arg1) {
        return view == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    /**
     * @auther 于德水
     * created at 2021/6/2 17:23
     * 方法描述：浏览图片回调
     */
    public interface PreviewImageCallBack<T> {
        public void receiveMsg(T msg);
    }

}

