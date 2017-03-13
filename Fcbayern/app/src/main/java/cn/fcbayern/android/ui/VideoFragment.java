package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
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
import java.util.List;

import cn.fcbayern.android.common.ViewPager2;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.HomeBanner;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.R;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.VideoModel;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.AppConfig;
import cn.fcbayern.android.util.DataReport;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.DeviceUtils;
import cn.fcbayern.android.util.Utils;
import cn.fcbayern.android.util.ViewHolder.VideoHolder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by chenzhan on 15/5/26.
 */
public class VideoFragment extends RefreshFragment {

    private VideoAdapter mAdapter;

    private Handler mScrollHandler;
    private ConvenientBanner banner;

    private List<HomeBanner> banners = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_content, container, false);
        mListView = (PullToRefreshListView) root.findViewById(R.id.list);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_head_layout, null);
        mListView.getRefreshableView().addHeaderView(view);
        banner = (ConvenientBanner) view.findViewById(R.id.topConvenientBanner);
        initData();
        mAdapter = new VideoAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(new TextView(getActivity()));
        mListView.setMode(PullToRefreshBase.Mode.BOTH);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                NetworkOper.getList(NetworkOper.Req.VIDEO, VideoFragment.this, true);
                DataReport.report(getActivity(), DataReport.VIDEO_EVENTID, DataReport.VIDEO_KEY, DataReport.VIDEO_REFRESH_VAL);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ArrayList<BaseModel> list = DataManager.getData(NetworkOper.Req.VIDEO);
                if (list.size() > 0) {
                    BaseModel obj = list.get(list.size() - 1);
                    NetworkOper.getList(NetworkOper.Req.VIDEO, obj.id, obj.time, AppConfig.VIDEO_PAGE_COUNT, VideoFragment.this);
                    DataReport.report(getActivity(), DataReport.VIDEO_EVENTID, DataReport.VIDEO_KEY, DataReport.VIDEO_LOAD_VAL);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoModel model = (VideoModel) mAdapter.getItem((int) id);
                if (model != null && !TextUtils.isEmpty(model.url)) {
                    ActionWebActivity.browse(getActivity(), model.url, getString(R.string.video), model.title, true);
                }
                DataReport.report(getActivity(), DataReport.VIDEO_EVENTID, DataReport.VIDEO_KEY, DataReport.VIDEO_VIDEO_VAL);
            }
        });

        mMainOperator = NetworkOper.Req.VIDEO;
        NetworkOper.getList(NetworkOper.Req.VIDEO, this, false);
        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSlideMenu().addIgnoredView(banner);
        }
        return root;
    }

    private void initData() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.VIDEO_URL + NetworkOper.buildQueryParam("get_focus_2016", 0, "", 0, null))
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

//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if (!hidden) {
//            if(getActivity() != null) {
//                checkRefreshList();
//            }
//        }
//    }

    @Override
    public void loadComplete(final int errorCode, boolean fromCache, int operator, boolean append) {
//        if (from == DataManager.DataFrom.LOCAL && errorCode != 0) {
//            NetworkOper.getList(DataManager.VIDEO_TYPE, VideoFragment.this);
//            return;
//        }
        if (getView() == null) return;

        super.loadComplete(errorCode, fromCache, operator, append);

        getView().post(new Runnable() {
            @Override
            public void run() {

                mAdapter.notifyDataSetChanged();
                mListView.onRefreshComplete();
            }
        });
    }

    static class VideoAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public VideoAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return DataManager.getData(NetworkOper.Req.VIDEO).size();
        }

        @Override
        public Object getItem(int position) {
            return DataManager.getData(NetworkOper.Req.VIDEO).get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VideoHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_video, parent, false);
                holder = new VideoHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.tags = (TextView) convertView.findViewById(R.id.tags);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            } else {
                holder = (VideoHolder) convertView.getTag();
            }
            VideoModel model = (VideoModel) getItem(position);
            holder.title.setText(model.title);
            holder.tags.setCompoundDrawables(null, null, null, null);
            if (TextUtils.isEmpty(model.tags)) {
                holder.tags.setVisibility(View.GONE);
            } else {
                holder.tags.setVisibility(View.VISIBLE);
                holder.tags.setText(model.tags);
            }
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
                    Intent intent = new Intent(context, WebActivity2.class);
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
