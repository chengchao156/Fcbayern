package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.fcbayern.android.common.MyListView;
import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.Rank;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.R;
import cn.fcbayern.android.model.MatchModel;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.GlideImageLoader;
import cn.fcbayern.android.util.image.RecyclingImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by chenzhan on 15/5/26.
 */
public class MatchCenterFragment extends Fragment implements DataManager.DataLoadListListener,OnClickListener {

    private MyListView mMatchList;

    private MyListView sList;

    private List<Rank> data = new ArrayList<>();
    private Button more;

    public static MatchCenterFragment newInstance() {

        Bundle args = new Bundle();

        MatchCenterFragment fragment = new MatchCenterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match, container, false);
        mMatchList = (MyListView) view.findViewById(R.id.match_list);
        sList = (MyListView) view.findViewById(R.id.sListView);
        more = (Button) view.findViewById(R.id.more);
        more.setOnClickListener(this);
        sList.setAdapter(new RankAdapter(getActivity()));
        mMatchList.setEnabled(false);
        mMatchList.setAdapter(new MatchAdapter(getActivity()));
//        NetworkOper.getList(NetworkOper.Req.LAST_SCHEDULE, this, true);
        NetworkOper.getList(NetworkOper.Req.LAST_SCHEDULE, null, "", 2, new DataManager.DataLoadListListener() {
            @Override
            public void loadComplete(int errorCode, boolean fromCache, int operator, boolean append) {

            }
        });
        initData();
        return view;
    }

    private void initData() {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(AddressUtils.MATCH_URL + NetworkOper.buildQueryParam("right_team_rank", 0, "", 0, null))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(json);
                                JSONArray da = object.getJSONArray("data");
                                for (int i = 0; i < da.length(); i++) {
                                    JSONObject obj = da.getJSONObject(i);
                                    String team_group = obj.getString("team_group");
                                    int team_id = obj.getInt("team_id");
                                    String name_zh = obj.getString("name_zh");
                                    String known_name_zh = obj.getString("known_name_zh");
                                    int played = obj.getInt("played");
                                    int rank_index = obj.getInt("rank_index");
                                    int win = obj.getInt("win");
                                    int draw = obj.getInt("draw");
                                    int lost = obj.getInt("lost");
                                    int hits = obj.getInt("hits");
                                    int miss = obj.getInt("miss");
                                    int difference = obj.getInt("difference");
                                    int score = obj.getInt("score");
                                    int avg_goal_hit = obj.getInt("avg_goal_hit");
                                    int avg_goal_lost = obj.getInt("avg_goal_lost");
                                    int avg_goal_win = obj.getInt("avg_goal_win");
                                    int avg_score = obj.getInt("avg_score");
                                    String promotion_id = obj.getString("promotion_id");
                                    String promotion_name = obj.getString("promotion_name");
                                    String team_logo = obj.getString("team_logo");
                                    int isHomeTeam = obj.getInt("isHomeTeam");
                                    Rank rank = new Rank(team_group, team_id, name_zh, known_name_zh, played, rank_index, win, draw, lost, hits, miss, difference, score, avg_goal_hit, avg_goal_lost, avg_goal_win, avg_score, promotion_id, promotion_name, team_logo, isHomeTeam);
                                    data.add(rank);
                                }
                                ((RankAdapter) sList.getAdapter()).notifyDataSetChanged();
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
    public void loadComplete(int errorCode, boolean fromCache, int type, boolean append) {
        if (getView() == null) return;
        getView().post(new Runnable() {
            @Override
            public void run() {
                ((MatchAdapter) mMatchList.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private OnClickListener mClick = new OnClickListener() {
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.more:
                Intent intent = new Intent(getActivity(),RankActivity.class);
                startActivity(intent);
                break;
        }

    }

    class MatchAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public MatchAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return DataManager.getData(NetworkOper.Req.LAST_SCHEDULE).size();
        }

        @Override
        public Object getItem(int position) {
            return DataManager.getData(NetworkOper.Req.LAST_SCHEDULE).get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_schedule, parent, false);
            }
            TextView hostName = (TextView) convertView.findViewById(R.id.host_n);
            TextView awayName = (TextView) convertView.findViewById(R.id.away_name);
            TextView scoreView = (TextView) convertView.findViewById(R.id.whole_score);
            TextView halfView = (TextView) convertView.findViewById(R.id.half_score);
            ImageView hostImage = (ImageView) convertView.findViewById(R.id.host_image);
            ImageView awayImage = (ImageView) convertView.findViewById(R.id.away_image);
            //View timePanel = convertView.findViewById(R.id.time_panel);
            TextView matchName = (TextView) convertView.findViewById(R.id.match_type);
            TextView matchDate = (TextView) convertView.findViewById(R.id.match_date);
            TextView matchTime = (TextView) convertView.findViewById(R.id.match_time);

            TextView newsInfo = (TextView) convertView.findViewById(R.id.news_info);
            TextView albumInfo = (TextView) convertView.findViewById(R.id.album_info);
            TextView liveInfo = (TextView) convertView.findViewById(R.id.live_info);

            MatchModel model = (MatchModel) getItem(position);
            hostName.setText(model.hostName);
            awayName.setText(model.awayName);
//            if (model.hostScore < 0) {
//                scoreView.setText("-:-");
//                halfView.setText("-:-");
//            } else {
            scoreView.setText(model.hostScore + ":" + model.awayScore);
            halfView.setText(model.halfScore);

            if (model.newsId <= 0 && model.albumId <= 0) {
                liveInfo.setText(model.liveInfo);
                newsInfo.setVisibility(View.GONE);
                albumInfo.setVisibility(View.GONE);
                liveInfo.setVisibility(View.VISIBLE);
            } else {
                newsInfo.setTag(model.newsId);
                albumInfo.setTag(model.albumId);
                liveInfo.setVisibility(View.GONE);
                albumInfo.setVisibility(model.albumId > 0 ? View.VISIBLE : View.GONE);
                newsInfo.setVisibility(model.newsId > 0 ? View.VISIBLE : View.GONE);
                albumInfo.setOnClickListener(mClick);
                newsInfo.setOnClickListener(mClick);
            }

            matchName.setText(model.leagueName);
            matchDate.setText(DateUtils.getDate(model.matchTime));
            matchTime.setText(DateUtils.getTime(model.matchTime));

            DataManager.getImageLoader().loadImage(model.awayIconUrl, awayImage);
            DataManager.getImageLoader().loadImage(model.hostIconUrl, hostImage);

            return convertView;
        }
    }

    class RankAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public RankAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size() == 0 ? 0 : data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.list_item_rank2, parent, false);
            }
            TextView pos = (TextView) convertView.findViewById(R.id.pos);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            RecyclingImageView image = (RecyclingImageView) convertView.findViewById(R.id.image);
            TextView wdl = (TextView) convertView.findViewById(R.id.wdl);
            TextView goal = (TextView) convertView.findViewById(R.id.goal);
            TextView score = (TextView) convertView.findViewById(R.id.score);

            Rank rank = data.get(position);
            pos.setText(rank.getRank_index() + "");
            switch (data.get(position).getIsHomeTeam()) {
                case 1:
                    convertView.setBackgroundResource(R.color.colorPrimary);
                    break;
                case 0:
                    convertView.setBackgroundResource(R.color.black);
                    break;
            }
            name.setText(rank.getKnown_name_zh());
            GlideImageLoader.loadImage(getActivity(), rank.getTeam_logo(), image);
            wdl.setText(rank.getWin() + "/" + rank.getDraw() + "/" + rank.getLost());
            goal.setText(rank.getHits() + "/" + rank.getMiss());
            score.setText(rank.getScore() + "");
            return convertView;
        }
    }

}
