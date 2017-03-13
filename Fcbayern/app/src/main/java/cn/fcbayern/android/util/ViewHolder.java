package cn.fcbayern.android.util;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ContentTextView;

/**
 * Created by chenzhan on 15/6/5.
 */
public class ViewHolder {

    public static class PhotosHolder {
        public ImageView image1;
        public ImageView image2;
        public ImageView image3;
        public TextView title;
        public TextView time;
    }

    public static class NewsHolder {
        public ImageView image;
        public TextView title;
        public ContentTextView content;
        public TextView time;
    }

    public static class AdsHolder {
        public ImageView image;
    }

    public static class VideoHolder {
        public ImageView image;
        public TextView tags;
        public TextView title;
        public TextView time;
    }

    public static class PlayerHolder {
        public ImageView image;
        public TextView type;
        public TextView name;
        public TextView nameEn;
        public TextView birth;
        public TextView number;
    }

    public static class RankHolder {
        public View container;
        public ImageView image;
        public TextView name;
        public TextView win;
        public TextView lost;
        public TextView draw;
        public TextView goals;
        public TextView score;
        public TextView pos;
    }

    public static class ScheduleHolder {
        public TextView hostName;
        public TextView awayName;
        public TextView scoreView;
        public TextView halfView;
        public ImageView hostImage;
        public ImageView awayImage;

        public TextView matchName;
        public TextView matchDate;
        public TextView matchTime;

        public TextView newsInfo;
        public TextView albumInfo;
        public TextView liveInfo;
    }
}
