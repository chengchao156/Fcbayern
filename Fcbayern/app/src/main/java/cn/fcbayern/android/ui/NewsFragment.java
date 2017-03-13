package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ContentTextView;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.HomeBanner;
import cn.fcbayern.android.model.NewsModel;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.AppConfig;
import cn.fcbayern.android.util.DataReport;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.ViewHolder.NewsHolder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by chenzhan on 15/5/26.
 */
public class NewsFragment extends RefreshFragment {

    private NewsAdapter mAdapter;
    private ConvenientBanner banner;
    private List<HomeBanner> banners = new ArrayList<>();


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_content, container, false);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_head_layout, null);
        mListView = (PullToRefreshListView) root.findViewById(R.id.list);
        mListView.getRefreshableView().addHeaderView(view);
        banner = (ConvenientBanner) view.findViewById(R.id.topConvenientBanner);
        initData();

        mAdapter = new NewsAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(new TextView(getActivity()));
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                NetworkOper.getList(NetworkOper.Req.NEWS, NewsFragment.this, true);
                DataReport.report(getActivity(), DataReport.NEWS_EVENTID, DataReport.NEWS_KEY, DataReport.NEWS_REFRESH_VAL);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ArrayList<BaseModel> list = DataManager.getData(NetworkOper.Req.NEWS);
                if (list.size() > 0) {
                    BaseModel obj = list.get(list.size() - 1);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("last_news_id", String.valueOf(obj.id));
                    NetworkOper.getList(NetworkOper.Req.NEWS, params, obj.time, AppConfig.NEWS_PAGE_COUNT, NewsFragment.this);
                    DataReport.report(getActivity(), DataReport.NEWS_EVENTID, DataReport.NEWS_KEY, DataReport.NEWS_LOAD_VAL);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), NewsWebActivity2.class);
                intent.putExtra("nid",String.valueOf(id));
                intent.putExtra("url", AddressUtils.DETAIL_NEWS_URL + id + AddressUtils.APP_PARAM);
                startActivity(intent);
                DataReport.report(getActivity(), DataReport.NEWS_EVENTID, DataReport.NEWS_KEY, DataReport.NEWS_NEWS_VAL);
            }
        });
        mMainOperator = NetworkOper.Req.NEWS;
        NetworkOper.getList(NetworkOper.Req.NEWS, this, false);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSlideMenu().addIgnoredView(banner);
        }
        return root;
    }

    private void initData() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.NEWS_URL + NetworkOper.buildQueryParam("get_focus_2016", 0, "", 0, null))
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String string = response.body().string();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(string);
                                JSONArray data = object.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);
                                    String id = obj.getString("id");
                                    String type = obj.getString("type");
                                    String title = obj.getString("title");
                                    String content = obj.getString("content");
                                    String pic = obj.getString("pic");
                                    String url = obj.getString("url");
                                    String orderby = obj.getString("orderby");
                                    String date = obj.getString("date");
                                    int show_type = obj.getInt("show_type");
                                    int show_id = obj.getInt("show_id");
                                    HomeBanner homeBanner = new HomeBanner(id, type, title, content, pic, url, orderby, date, show_type, show_id);
                                    banners.add(homeBanner);
                                    banner.setPages(new CBViewHolderCreator<TopBannerHolder>() {
                                        @Override
                                        public TopBannerHolder createHolder() {
                                            return new TopBannerHolder();
                                        }
                                    }, banners).setPageIndicator(new int[]{R.mipmap.in_unfocus, R.mipmap.in_focus}).setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void loadComplete(final int errorcode, boolean fromCache, int operator, boolean append) {
        if (getView() == null) return;
        super.loadComplete(errorcode, fromCache, operator, append);
        getView().post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                mListView.onRefreshComplete();
            }
        });
    }

    static class NewsAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public NewsAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return DataManager.getData(NetworkOper.Req.NEWS).size();
        }

        @Override
        public Object getItem(int position) {
            return DataManager.getData(NetworkOper.Req.NEWS).get(position);
        }

        @Override
        public long getItemId(int position) {
            BaseModel model = (BaseModel) getItem(position);
            return model.id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NewsHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_news, parent, false);
                holder = new NewsHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.content = (ContentTextView) convertView.findViewById(R.id.content);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            } else {
                holder = (NewsHolder) convertView.getTag();
            }
            NewsModel model = (NewsModel) getItem(position);
            holder.title.setText(model.title);
            holder.content.setContentText(model.content);
            holder.time.setText(DateUtils.getShowTime(model.time));
            DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.imageUrl, holder.image);
            return convertView;
        }
    }

    public class TopBannerHolder implements Holder<HomeBanner> {

        private RelativeLayout relativeLayout;
        private ImageView baner;
        private TextView title;

        @Override
        public View createView(Context context) {
            relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_banner_top, null);
            baner = (ImageView) relativeLayout.findViewById(R.id.banner);
            baner.setScaleType(ImageView.ScaleType.FIT_XY);
            title = (TextView) relativeLayout.findViewById(R.id.title);

            return relativeLayout;
        }

        @Override
        public void UpdateUI(final Context context, final int position, final HomeBanner data) {
            Glide.with(context).load(AddressUtils.IMAGE_PREFIX + data.getPic()).into(baner);
            title.setText(data.getTitle());
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, NewsWebActivity2.class);
                    intent.putExtra("nid",banners.get(position).getId());
                    intent.putExtra("url", banners.get(position).getUrl());
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        banner.startTurning(4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        banner.stopTurning();
    }
}