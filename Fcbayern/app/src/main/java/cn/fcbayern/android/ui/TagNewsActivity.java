package cn.fcbayern.android.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.GlideImageLoader;
import cn.fcbayern.android.util.ToastUtils;
import cn.fcbayern.android.util.image.RecyclingImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TagNewsActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout back;
    private PullRefreshListView2 lv;

    private List<NewsBean> news = new ArrayList<>();
    private NewsAdapter adapter;
    private Dialog dialog;
    private TextView title;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_news);
        id = getIntent().getStringExtra("tag_id");
        initView();
        adapter = new NewsAdapter(this);
        lv.setAdapter(adapter);
        initNews("");
        title.setText("加载中");
        dialog.show();
        lv.setMode(PullToRefreshBase.Mode.BOTH);
        lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                news.clear();
                lv.setAdapter(adapter);
                initNews("");
                complete();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (!(news.size() - 1 < 0)) {
                    initNews(news.get(news.size() - 1).getId());
                    complete();
                } else {
                    ToastUtils.showToast(TagNewsActivity.this, "没有更多了");
                    initNews("");
                    complete();
                }
            }
        });
    }

    private void complete() {
        lv.postDelayed(new Runnable() {
            @Override
            public void run() {
                lv.onRefreshComplete();
            }
        }, 1000);
    }

    private void initNews(String last) {

        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "get_tag_list");
        params.put("tag_id", id);
        params.put("last_time", "");
        params.put("last_news_id", last);
        params.put("limit", "");

        Request request = new Request.Builder()
                .url(AddressUtils.NEWS_URL + NetworkOper.buildQueryParam("get_tag_list", 0, "", 0, params))
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
                                    JSONArray data = object.getJSONArray("data");
                                    if (data.length() == 0) {
                                        dialog.dismiss();
                                    } else {
                                        for (int i = 0; i < data.length(); i++) {
                                            JSONObject obj = data.getJSONObject(i);
                                            int id = Integer.valueOf(obj.getString("id"));
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
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

    }

    private void initView() {
        back = (RelativeLayout) this.findViewById(R.id.back);
        back.setOnClickListener(this);
        lv = (PullRefreshListView2) this.findViewById(R.id.tagNewsLv);
        View view = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        dialog = new MyDialog(this, R.style.dialog);
        dialog.setContentView(view);
        title = (TextView) view.findViewById(R.id.title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
        }
    }

    class NewsAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public NewsAdapter(Context context) {
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
            GlideImageLoader.loadImage(TagNewsActivity.this, AddressUtils.IMAGE_PREFIX + bean.getPic(), image);
            title.setText(bean.getTitle());
            content.setContentText(bean.getContent());
            time.setText(DateUtils.getShowTime(bean.getDate()));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TagNewsActivity.this, NewsWebActivity2.class);
                    intent.putExtra("nid", bean.getId());
                    intent.putExtra("url", AddressUtils.DETAIL_NEWS_URL + bean.getId() + AddressUtils.APP_PARAM);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }
}
