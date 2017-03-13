package cn.fcbayern.android.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ContentTextView;
import cn.fcbayern.android.common.MyDialog;
import cn.fcbayern.android.common.PullRefreshListView2;
import cn.fcbayern.android.model.NewsBean;
import cn.fcbayern.android.model.PhotoBean;
import cn.fcbayern.android.model.VideoBean;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.GlideImageLoader;
import cn.fcbayern.android.util.HintUtils;
import cn.fcbayern.android.util.ToastUtils;
import cn.fcbayern.android.util.image.RecyclingImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SerachResultctivity extends AppCompatActivity implements View.OnClickListener {


    private EditText etSerach;
    private TextView cancle;

    private PullRefreshListView2 listView;

    private ImageView serach;
    private TextView type;
    private TextView resultCount;

    private List<NewsBean> news = new ArrayList<>();
    private List<VideoBean> video = new ArrayList<>();
    private List<PhotoBean> photo = new ArrayList<>();
    private List<String> thum;

    PopupWindow popupWindow;
    TextView tvNews;
    TextView tvVideo;
    TextView tvMatch;

    private String value = "";
    private String ty = "";
    static List<TextView> textViews = new ArrayList<>();
    private RelativeLayout select;

    private Dialog dialog;
    private TextView title;
    private int count;
    private SerachNewsAdapter newsAdapter;
    private SerachVideoAdapter videoAdapter;
    private SerachPhotoAdapter photoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serach_resultctivity);
        initView();
        title.setText("搜索中请稍后");
        dialog.show();
        setOnClick();
        setPopWindow();
        View view = LayoutInflater.from(this).inflate(R.layout.list_serach_head, null);
        resultCount = (TextView) view.findViewById(R.id.result);
        listView.getRefreshableView().addHeaderView(view);
        listView.setEmptyView(new TextView(this));
        listView.setAnimation(null);
        newsAdapter = new SerachNewsAdapter(this);
        videoAdapter = new SerachVideoAdapter(this);
        photoAdapter = new SerachPhotoAdapter(this);
        if (getIntent() != null) {
            value = this.getIntent().getStringExtra("value");
            ty = this.getIntent().getStringExtra("type");
        }

        etSerach.setText(value);
        if (ty.equals("新闻")) {
            listView.setAdapter(newsAdapter);
            initNews("");
        } else if (ty.equals("视频")) {
            listView.setAdapter(videoAdapter);
            initVideo("");
        } else {
            listView.setAdapter(photoAdapter);
            initPhoto("");
        }
        listView.setMode(PullToRefreshBase.Mode.BOTH);
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (type.getText().toString().equals("新闻")) {
                    news.clear();
                    listView.setAdapter(newsAdapter);
                    initNews("");
                    complete();
                } else if (type.getText().toString().equals("视频")) {
                    video.clear();
                    listView.setAdapter(videoAdapter);
                    initVideo("");
                    complete();
                } else {
                    photo.clear();
                    listView.setAdapter(photoAdapter);
                    initPhoto("");
                    complete();
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (type.getText().toString().equals("新闻")) {
                    listView.setAdapter(newsAdapter);
                    if (!(news.size() - 1 < 0)) {
                        initNews(news.get(news.size() - 1).getId());
                        complete();
                    } else {
                        ToastUtils.showToast(SerachResultctivity.this, "没有更多了");
                        initNews("");
                        complete();
                    }
                } else if (type.getText().toString().equals("视频")) {
                    listView.setAdapter(videoAdapter);
                    if (!(video.size() - 1 < 0)) {
                        initVideo(video.get(video.size() - 1).getId());
                        complete();
                    } else {
                        ToastUtils.showToast(SerachResultctivity.this, "没有更多了");
                        initVideo("");
                        complete();
                    }
                } else {
                    listView.setAdapter(photoAdapter);
                    if (!(photo.size() - 1 < 0)) {
                        initPhoto(photo.get(photo.size() - 1).getId());
                        complete();
                    } else {
                        ToastUtils.showToast(SerachResultctivity.this, "没有更多了");
                        initPhoto("");
                        complete();
                    }
                }
            }
        });
    }

    private void complete() {
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.onRefreshComplete();
            }
        }, 1000);
    }

    private void initPhoto(String last) {
        final OkHttpClient client = new OkHttpClient.Builder()
                .build();
        final int[] serversLoadTimes = {0};
        HashMap<String, String> params = new HashMap<>();
        params.put("search_word", etSerach.getText().toString());
        params.put("search_type", "album");
        params.put("last_id", last);

        Request request = new Request.Builder()
                .url(AddressUtils.OTHER_URL + NetworkOper.buildQueryParam("search", 0, "", 0, params))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String josn = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(josn);
                                String code = object.getString("code");
                                if (code.equals("0")) {
                                    JSONObject info = object.getJSONObject("info");
                                    count = info.getInt("search_count");
                                    JSONArray data = object.getJSONArray("data");
                                    if (data.length() == 0) {
                                        dialog.dismiss();
                                    } else {
                                        for (int i = 0; i < data.length(); i++) {
                                            thum = new ArrayList<>();
                                            JSONObject obj = data.getJSONObject(i);
                                            String id = obj.getString("id");
                                            String pic = obj.getString("pic");
                                            String title = obj.getString("title");
                                            String content = obj.getString("content");
                                            String date = obj.getString("date");
                                            JSONArray thumb = obj.getJSONArray("thmub");
                                            for (int n = 0; n < thumb.length(); n++) {
                                                thum.add(thumb.getString(n));
                                            }
                                            PhotoBean bean = new PhotoBean(id, pic, title, content, date, thum);
                                            photo.add(bean);
                                            dialog.dismiss();
                                        }
                                    }
                                }
                                photoAdapter.notifyDataSetChanged();
                                resultCount.setText("共为您检索到" + count + "条相关结果");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    ToastUtils.showToast(SerachResultctivity.this, " 网络似乎开了小差");
                }
            }
        });
    }

    private void initVideo(String last) {

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        params.put("search_word", etSerach.getText().toString());
        params.put("search_type", "video");
        params.put("last_id", last);

        Request request = new Request.Builder()
                .url(AddressUtils.OTHER_URL + NetworkOper.buildQueryParam("search", 0, "", 0, params))
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String josn = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(josn);
                                String code = object.getString("code");
                                if (code.equals("0")) {
                                    JSONObject info = object.getJSONObject("info");
                                    count = info.getInt("search_count");
                                    JSONArray data = object.getJSONArray("data");
                                    if (data.length() == 0) {
                                        dialog.dismiss();
                                    } else {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject obj = data.getJSONObject(i);
                                            String id = obj.getString("id");
                                            String pic = obj.getString("pic");
                                            String title = obj.getString("title");
                                            String content = obj.getString("content");
                                            String src = obj.getString("src");
                                            String date = obj.getString("date");
                                            String link = obj.getString("link");
                                            VideoBean bean = new VideoBean(id, pic, title, content, src, date, link);
                                            video.add(bean);
                                            dialog.dismiss();
                                        }
                                    }
                                }
                                videoAdapter.notifyDataSetChanged();
                                resultCount.setText("共为您检索到" + count + "条相关结果");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    ToastUtils.showToast(SerachResultctivity.this, " 网络似乎开了小差");
                }
            }
        });
    }

    private void initNews(String last) {

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        params.put("search_word", etSerach.getText().toString());
        params.put("search_type", "news");
        params.put("last_id", last);

        Request request = new Request.Builder()
                .url(AddressUtils.OTHER_URL + NetworkOper.buildQueryParam("search", 0, "", 0, params))
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String josn = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(josn);
                                String code = object.getString("code");
                                if (code.equals("0")) {
                                    JSONObject info = object.getJSONObject("info");
                                    count = info.getInt("search_count");
                                    JSONArray data = object.getJSONArray("data");
                                    if (data.length() == 0) {
                                        dialog.dismiss();
                                    } else {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject obj = data.getJSONObject(i);
                                            int id = obj.getInt("id");
                                            String pic = obj.getString("pic");
                                            String title = obj.getString("title");
                                            String content = obj.getString("content");
                                            String date = obj.getString("date");
                                            NewsBean bean = new NewsBean(String.valueOf(id), pic, title, content, date);
                                            news.add(bean);
                                            dialog.dismiss();
                                        }
                                    }
                                }
                                newsAdapter.notifyDataSetChanged();
                                resultCount.setText("共为您检索到" + count + "条相关结果");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    ToastUtils.showToast(SerachResultctivity.this, " 网络似乎开了小差");
                }
            }
        });

    }

    private void setOnClick() {
        cancle.setOnClickListener(this);
        select.setOnClickListener(this);
        serach.setOnClickListener(this);
    }

    private void initView() {
        etSerach = (EditText) this.findViewById(R.id.etSerach);
        HintUtils.setHintTextSize(etSerach, "搜索", 14);
        cancle = (TextView) this.findViewById(R.id.cancle);
        serach = (ImageView) this.findViewById(R.id.iv_serach);
        select = (RelativeLayout) this.findViewById(R.id.select);
        type = (TextView) this.findViewById(R.id.type);
        listView = (PullRefreshListView2) this.findViewById(R.id.searchListView);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        dialog = new MyDialog(this, R.style.dialog);
        dialog.setContentView(view);
        title = (TextView) view.findViewById(R.id.title);
    }

    /**
     * 设置PopupWindow
     */
    private void setPopWindow() {
        popupWindow = new PopupWindow(LayoutInflater.from(this).inflate(R.layout.pop_type_layout, null),
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        View contentView = popupWindow.getContentView();
        tvNews = (TextView) contentView.findViewById(R.id.tv_news);
        tvVideo = (TextView) contentView.findViewById(R.id.tv_video);
        tvMatch = (TextView) contentView.findViewById(R.id.tv_match);
        tvNews.setOnClickListener(this);
        tvVideo.setOnClickListener(this);
        tvMatch.setOnClickListener(this);
        textViews.add(tvNews);
        textViews.add(tvVideo);
        textViews.add(tvMatch);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_news:
                popupWindow.dismiss();
                type.setText("新闻");
                news.clear();
                dialog.show();
                listView.setAdapter(newsAdapter);
                initNews("");
                break;
            case R.id.tv_video:
                popupWindow.dismiss();
                type.setText("视频");
                video.clear();
                dialog.show();
                listView.setAdapter(videoAdapter);
                initVideo("");
                break;
            case R.id.tv_match:
                popupWindow.dismiss();
                type.setText("图集");
                photo.clear();
                dialog.show();
                listView.setAdapter(photoAdapter);
                initPhoto("");
                break;
            case R.id.cancle:
                onBackPressed();
                break;
            case R.id.select:
                popupWindow.showAsDropDown(select, -60, 30);
                break;
            case R.id.iv_serach:
                if (etSerach.getText().toString().equals("")) {
                    ToastUtils.showToast(this, "请键入关键字");
                } else {
                    if (type.getText().toString().equals("新闻")) {
                        news.clear();
                        dialog.show();
                        listView.setAdapter(newsAdapter);
                        initNews("");
                    } else if (type.getText().toString().equals("视频")) {
                        video.clear();
                        dialog.show();
                        listView.setAdapter(videoAdapter);
                        initVideo("");
                    } else {
                        photo.clear();
                        dialog.show();
                        listView.setAdapter(photoAdapter);
                        initPhoto("");
                    }
                }

                break;
        }
    }

    class SerachNewsAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public SerachNewsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return news.size();
        }

        @Override
        public Object getItem(int position) {
            return news.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_news, parent, false);
            }
            RecyclingImageView image = (RecyclingImageView) convertView.findViewById(R.id.image);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView time = (TextView) convertView.findViewById(R.id.time);
            ContentTextView content = (ContentTextView) convertView.findViewById(R.id.content);

            final NewsBean bean = news.get(position);
            GlideImageLoader.loadImage(SerachResultctivity.this, AddressUtils.IMAGE_PREFIX + bean.getPic(), image);
            title.setText(bean.getTitle());
            content.setContentText(bean.getContent());
            time.setText(DateUtils.getShowTime(bean.getDate()));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SerachResultctivity.this, NewsWebActivity2.class);
                    intent.putExtra("nid",bean.getId());
                    intent.putExtra("url", AddressUtils.DETAIL_NEWS_URL + bean.getId() + AddressUtils.APP_PARAM);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }

    class SerachVideoAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public SerachVideoAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return video.size();
        }

        @Override
        public Object getItem(int position) {
            return video.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_video, parent, false);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtils.showToast(SerachResultctivity.this, "视频");
                }
            });
            RecyclingImageView image = (RecyclingImageView) convertView.findViewById(R.id.image);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView time = (TextView) convertView.findViewById(R.id.time);
            TextView tag = (TextView) convertView.findViewById(R.id.tags);

            final VideoBean bean = video.get(position);
            GlideImageLoader.loadImage(SerachResultctivity.this, AddressUtils.IMAGE_PREFIX + bean.getPic(), image);
            title.setText(bean.getContent());
            tag.setText(bean.getTitle());
            time.setText(DateUtils.getShowTime(bean.getDate()));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bean.getLink() != null) {
                        Intent intent = new Intent(SerachResultctivity.this, WebActivity2.class);
                        intent.putExtra("url", bean.getLink());
                        startActivity(intent);
                    } else {
                        ToastUtils.showToast(SerachResultctivity.this, "这个视频不能播诶");
                    }
                }
            });
            return convertView;
        }
    }

    class SerachPhotoAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public SerachPhotoAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return photo.size();
        }

        @Override
        public Object getItem(int position) {
            return photo.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_photos, parent, false);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtils.showToast(SerachResultctivity.this, "图集");
                }
            });
            RecyclingImageView image1 = (RecyclingImageView) convertView.findViewById(R.id.image1);
            RecyclingImageView image2 = (RecyclingImageView) convertView.findViewById(R.id.image2);
            RecyclingImageView image3 = (RecyclingImageView) convertView.findViewById(R.id.image3);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView time = (TextView) convertView.findViewById(R.id.time);

            final PhotoBean bean = photo.get(position);
            title.setText(bean.getTitle());
            time.setText(DateUtils.getShowTime(bean.getDate()));
            if (bean.getThumb().size() == 0) {
                image1.setImageResource(R.mipmap.ic_placeholder);
                image2.setImageResource(R.mipmap.ic_placeholder);
                image3.setImageResource(R.mipmap.ic_placeholder);
            } else if (bean.getThumb().size() == 1) {
                image2.setImageResource(R.mipmap.ic_placeholder);
                image3.setImageResource(R.mipmap.ic_placeholder);
                GlideImageLoader.loadImage(SerachResultctivity.this, AddressUtils.IMAGE_PREFIX + bean.getThumb().get(0), image1);
            } else if (bean.getThumb().size() == 2) {
                image3.setImageResource(R.mipmap.ic_placeholder);
                GlideImageLoader.loadImage(SerachResultctivity.this, AddressUtils.IMAGE_PREFIX + bean.getThumb().get(0), image1);
                GlideImageLoader.loadImage(SerachResultctivity.this, AddressUtils.IMAGE_PREFIX + bean.getThumb().get(1), image2);
            } else if (bean.getThumb().size() == 3) {
                GlideImageLoader.loadImage(SerachResultctivity.this, AddressUtils.IMAGE_PREFIX + bean.getThumb().get(0), image1);
                GlideImageLoader.loadImage(SerachResultctivity.this, AddressUtils.IMAGE_PREFIX + bean.getThumb().get(1), image2);
                GlideImageLoader.loadImage(SerachResultctivity.this, AddressUtils.IMAGE_PREFIX + bean.getThumb().get(2), image3);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SerachResultctivity.this, PhotoActivity.class);
                    intent.putExtra(PhotoActivity.KEY_CONTENT, Integer.valueOf(bean.getId()));
                    intent.putExtra(PhotoActivity.KEY_TYPE, NetworkOper.Req.PHOTO);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }
}