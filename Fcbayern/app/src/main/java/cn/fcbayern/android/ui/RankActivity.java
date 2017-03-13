package cn.fcbayern.android.ui;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.fcbayern.android.R;
import cn.fcbayern.android.model.Rank;
import cn.fcbayern.android.model.RankTabAdapter;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RankActivity extends AppCompatActivity implements View.OnClickListener, TabLayout.OnTabSelectedListener {

    private TabLayout rankTab;
    private ViewPager rankVp;

    private List<String> name = new ArrayList<>();

    private List<Fragment> fragments = new ArrayList<>();
    private ArrayList<Rank> rank1 = new ArrayList<>();
    private ArrayList<Rank> rank2 = new ArrayList<>();

    private RankTabAdapter adapter;
    private RelativeLayout back;

    private String name1;
    private String name2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        initView();
        rankTab.setOnTabSelectedListener(this);
        rankTab.setTabMode(TabLayout.MODE_FIXED);
        rankVp.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(rankTab));
        initData();
    }

    private void initData() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.MATCH_URL + NetworkOper.buildQueryParam("team_rank", 0, "", 0, null))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String json = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject object = new JSONObject(json);
                                String code = object.getString("code");
                                if (code.equals("0")) {
                                    JSONObject data = object.getJSONObject("data");
                                    JSONObject leagues = data.getJSONObject("leagues");
                                    name1 = leagues.getString("5");
                                    name2 = leagues.getString("9");
                                    name.add(name1);
                                    name.add(name2);
                                    JSONObject ran = data.getJSONObject("rank");
                                    JSONArray dejia = ran.getJSONArray("5");
                                    JSONArray ouguan = ran.getJSONArray("9");
                                    for (int i = 0; i < dejia.length(); i++) {
                                        JSONObject obj = dejia.getJSONObject(i);
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
                                        Rank rank = new Rank(team_group, team_id, name_zh, known_name_zh, played, rank_index, win, draw, lost, hits, miss, difference, score, avg_goal_hit, avg_goal_lost, avg_goal_win, avg_score, promotion_id, promotion_name, team_logo, 0);
                                        rank1.add(rank);
                                    }

                                    for (int i = 0; i < ouguan.length(); i++) {
                                        JSONObject obj = ouguan.getJSONObject(i);
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
                                        Rank rank = new Rank(team_group, team_id, name_zh, known_name_zh, played, rank_index, win, draw, lost, hits, miss, difference, score, avg_goal_hit, avg_goal_lost, avg_goal_win, avg_score, promotion_id, promotion_name, team_logo, 0);
                                        rank2.add(rank);
                                    }
                                }
                                fragments.add(RankTabFragment.newInstance(rank1));
                                fragments.add(RankTabFragment.newInstance(rank2));
                                adapter = new RankTabAdapter(getSupportFragmentManager(), fragments, name);
                                rankVp.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                rankTab.setupWithViewPager(rankVp);
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
        rankTab = (TabLayout) this.findViewById(R.id.RankTab);
        rankVp = (ViewPager) this.findViewById(R.id.RankVp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        rankVp.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


}
