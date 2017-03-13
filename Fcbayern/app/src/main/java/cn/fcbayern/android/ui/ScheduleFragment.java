package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.fcbayern.android.R;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.MatchModel;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.LogUtils;
import cn.fcbayern.android.util.ViewHolder.ScheduleHolder;


/**
 * Created by chenzhan on 15/5/26.
 */
public class ScheduleFragment extends TabFragment {

    private ArrayMap<String, ArrayList<MatchModel>> mDatas = new ArrayMap<>();

    private View.OnClickListener mClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = (Integer) v.getTag();
            switch (v.getId()) {
                case R.id.news_info:
                    if (id > 0) {
                        NewsWebActivity.browse(getActivity(), AddressUtils.DETAIL_NEWS_URL + id, getString(R.string.news), NetworkOper.Req.HOME);
                    }
                    break;
                case R.id.album_info:
                    if (id > 0) {
                        Intent intent = new Intent(getActivity(), PhotoActivity.class);
                        intent.putExtra(PhotoActivity.KEY_CONTENT, id);
                        intent.putExtra(PhotoActivity.KEY_TYPE, NetworkOper.Req.HOME);
                        startActivity(intent);
                    }
                    break;
            }
        }
    };

    @Override
    public void loadComplete(int errorCode, boolean fromCache, int type, boolean append) {

        ArrayList<BaseModel> lists = DataManager.getData(NetworkOper.Req.SCHEDULE);

        for (String name : MatchModel.leagueList) {
            mDatas.put(name, new ArrayList<MatchModel>());
        }
        for (int i = 0; i < lists.size(); i++) {
            MatchModel model = (MatchModel) lists.get(i);
            String tabName = model.leagueName;
            if (MatchModel.leagueList.contains(tabName)) {
                mDatas.get(tabName).add(model);
            } else {
                mDatas.get(MatchModel.leagueList.get(MatchModel.leagueList.size() - 1)).add(model);
            }
        }

        super.loadComplete(errorCode, fromCache, type, append);
    }

    @Override
    protected String getTabTitle(int position) {
        return MatchModel.leagueList.get(position);
    }

    @Override
    protected int getTabCount() {
        return mDatas.size();
    }

    @Override
    protected BaseAdapter getSubAdapter(Context context, int position) {
        ScheduleAdapter adapter = new ScheduleAdapter(context);
        adapter.setData(mDatas.get(MatchModel.leagueList.get(position)));
        return adapter;
    }

    @Override
    protected void fetchData(DataManager.DataLoadListListener listener) {
        mMainOperator = NetworkOper.Req.SCHEDULE;
        NetworkOper.getList(NetworkOper.Req.SCHEDULE, listener, true);
    }

    @Override
    public void refreshData() {
        NetworkOper.getList(NetworkOper.Req.SCHEDULE, this, true);
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

    public class ScheduleAdapter extends BaseAdapter {

        private ArrayList<MatchModel> mDatas;
        private LayoutInflater mInflater;

        public ScheduleAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<MatchModel> list) {
            mDatas = list;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ScheduleHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_schedule2, parent, false);
                holder = new ScheduleHolder();
                holder.hostName = (TextView) convertView.findViewById(R.id.host_name);
                holder.awayName = (TextView) convertView.findViewById(R.id.away_name);
                holder.scoreView = (TextView) convertView.findViewById(R.id.whole_score);
                holder.halfView = (TextView) convertView.findViewById(R.id.half_score);
                holder.hostImage = (ImageView) convertView.findViewById(R.id.host_image);
                holder.awayImage = (ImageView) convertView.findViewById(R.id.away_image);

                holder.matchName = (TextView) convertView.findViewById(R.id.match_type);
                holder.matchDate = (TextView) convertView.findViewById(R.id.match_date);
                holder.matchTime = (TextView) convertView.findViewById(R.id.match_time);

                holder.newsInfo = (TextView) convertView.findViewById(R.id.news_info);
                holder.albumInfo = (TextView) convertView.findViewById(R.id.album_info);
                holder.liveInfo = (TextView) convertView.findViewById(R.id.live_info);

                convertView.setTag(holder);
            } else {
                holder = (ScheduleHolder)convertView.getTag();
            }

            MatchModel model = mDatas.get(position);

            holder.hostName.setText(model.hostName);
            holder.awayName.setText(model.awayName);

            holder.scoreView.setText(model.hostScore + ":" + model.awayScore);
            holder.halfView.setText(model.halfScore);

//            if (model.newsId > 0) {
//                holder.newsInfo.setTag(model.newsId);
//                holder.liveInfo.setVisibility(View.GONE);
//                holder.newsInfo.setVisibility(View.VISIBLE);
//                holder.newsInfo.setOnClickListener(mClick);
//            } else {
//                holder.newsInfo.setTag(null);
//                holder.liveInfo.setText(model.liveInfo);
//                holder.newsInfo.setVisibility(View.GONE);
//                holder.liveInfo.setVisibility(View.VISIBLE);
//            }
            if (model.newsId <= 0 && model.albumId <= 0) {
                holder.liveInfo.setText(model.liveInfo);
                holder.newsInfo.setVisibility(View.GONE);
                holder.albumInfo.setVisibility(View.GONE);
                holder.liveInfo.setVisibility(View.VISIBLE);
            } else {
                holder.newsInfo.setTag(model.newsId);
                holder.albumInfo.setTag(model.albumId);
                holder.liveInfo.setVisibility(View.GONE);
                holder.albumInfo.setVisibility(model.albumId > 0 ? View.VISIBLE : View.GONE);
                holder.newsInfo.setVisibility(model.newsId > 0 ? View.VISIBLE : View.GONE);
                holder.albumInfo.setOnClickListener(mClick);
                holder.newsInfo.setOnClickListener(mClick);
            }

            holder.matchName.setText(model.leagueName);
            holder.matchDate.setText(DateUtils.getDate(model.matchTime));
            holder.matchTime.setText(DateUtils.getTime(model.matchTime));

            DataManager.getImageLoader().loadImage(model.awayIconUrl, holder.awayImage);
            DataManager.getImageLoader().loadImage(model.hostIconUrl, holder.hostImage);

            return convertView;
        }
    }

}
