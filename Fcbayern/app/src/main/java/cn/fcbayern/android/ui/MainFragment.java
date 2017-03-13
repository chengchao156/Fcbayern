package cn.fcbayern.android.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bumptech.glide.Glide;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.fcbayern.android.R;
import cn.fcbayern.android.common.HorizontalInnerViewPager;
import cn.fcbayern.android.common.MyDialog;
import cn.fcbayern.android.model.HomeBanner;
import cn.fcbayern.android.model.HomeCenterBanner;
import cn.fcbayern.android.model.HomePlayer;
import cn.fcbayern.android.model.HomeScheduleBoaed;
import cn.fcbayern.android.model.HomeVideoBanner;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.GlideImageLoader;
import cn.fcbayern.android.util.image.RecyclingImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainFragment extends Fragment implements View.OnClickListener {

    private HorizontalInnerViewPager vp;
    private HorizontalInnerViewPager videoVp;
    private LinearLayout mainLayout;
    private Dialog dialog;
    private TextView title;

    private ImageView next;
    private ImageView up;

    private ImageView videoUp;
    private ImageView videoNext;

    private ImageView player1;
    private ImageView player2;
    private ImageView player3;
    private ImageView player4;

    private TextView number1;
    private TextView number2;
    private TextView number3;
    private TextView number4;

    private TextView name1;
    private TextView name2;
    private TextView name3;
    private TextView name4;

    private ImageView cBanner;
    private ConvenientBanner topBanner;

    private List<HomeCenterBanner> centerBanner = new ArrayList<>();
    private List<HomeBanner> banner = new ArrayList<>();
    private List<HomeVideoBanner> video = new ArrayList<>();
    private List<HomeScheduleBoaed> schedule = new ArrayList<>();
    private List<HomePlayer> players = new ArrayList<>();

    private List<View> videoView = new ArrayList<>();
    private List<View> scheduleView = new ArrayList<>();

    SharedPreferences preferences;

    private int index = 0;

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        preferences = getActivity().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);

        initView(view);
        setOnClick();
        initBanner();
        initCenterBanner();
        initVideo();
        initSchedule();
        initPlayer();
        ((MainActivity) getActivity()).getSlideMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSlideMenu().addIgnoredView(topBanner);
        }
        vp.requestDisallowInterceptTouchEvent(true);
        videoVp.requestDisallowInterceptTouchEvent(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setOnClick() {

        up.setOnClickListener(this);
        next.setOnClickListener(this);
        videoUp.setOnClickListener(this);
        videoNext.setOnClickListener(this);
        cBanner.setOnClickListener(this);

        player1.setOnClickListener(this);
        player2.setOnClickListener(this);
        player3.setOnClickListener(this);
        player4.setOnClickListener(this);
    }

    private void initView(View view) {
        vp = (HorizontalInnerViewPager) view.findViewById(R.id.homeVp);
        videoVp = (HorizontalInnerViewPager) view.findViewById(R.id.videoVp);
        next = (ImageView) view.findViewById(R.id.next);
        up = (ImageView) view.findViewById(R.id.up);
        videoUp = (ImageView) view.findViewById(R.id.videoUp);
        videoNext = (ImageView) view.findViewById(R.id.videoNext);
        topBanner = (ConvenientBanner) view.findViewById(R.id.topConvenientBanner);
        cBanner = (ImageView) view.findViewById(R.id.centerBanner);

        player1 = (ImageView) view.findViewById(R.id.player1);
        player2 = (ImageView) view.findViewById(R.id.player2);
        player3 = (ImageView) view.findViewById(R.id.player3);
        player4 = (ImageView) view.findViewById(R.id.player4);

        name1 = (TextView) view.findViewById(R.id.playerName1);
        name2 = (TextView) view.findViewById(R.id.playerName2);
        name3 = (TextView) view.findViewById(R.id.playerName3);
        name4 = (TextView) view.findViewById(R.id.playerName4);

        number1 = (TextView) view.findViewById(R.id.palyerNumber1);
        number2 = (TextView) view.findViewById(R.id.palyerNumber2);
        number3 = (TextView) view.findViewById(R.id.palyerNumber3);
        number4 = (TextView) view.findViewById(R.id.palyerNumber4);

        mainLayout = (LinearLayout) view.findViewById(R.id.mainLayout);
        dialog = new MyDialog(getActivity(), R.style.dialog);
        View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.loading_dialog, null);
        dialog.setContentView(view2);
        title = (TextView) view2.findViewById(R.id.title);
        title.setText("加载中");
        dialog.show();
    }

    @Override
    public void onClick(View view) {


        int i = vp.getCurrentItem();
        int c = videoVp.getCurrentItem();
        switch (view.getId()) {
            case R.id.up:
                index = i;
                if (i != 0) {
                    i = i - 1;
                    vp.setCurrentItem(i);
                }
                break;
            case R.id.next:
                index = i;
                if (index != scheduleView.size()) {
                    index++;
                    vp.setCurrentItem(index);
                }
                break;
            case R.id.videoUp:
                index = c;
                if (c != 0) {
                    index--;
                    videoVp.setCurrentItem(index);
                }
                break;
            case R.id.videoNext:
                index = c;
                if (c != videoView.size()) {
                    index++;
                    videoVp.setCurrentItem(index);
                }
                break;
            case R.id.centerBanner:
                Intent intent = new Intent(getActivity(), WebActivity2.class);
                intent.putExtra("url", centerBanner.get(0).getUrl());
                startActivity(intent);
                break;
            case R.id.player1:
                Intent intent1 = new Intent(getActivity(), PlayerDetailActivity.class);
                intent1.putExtra(PlayerDetailActivity.ID_KEY, Integer.valueOf(players.get(0).getPlayer_id()));
                boolean iscoach;
                int is_coach = players.get(0).getIs_coach();
                if (is_coach == 1) {
                    iscoach = true;
                } else {
                    iscoach = false;
                }
                intent1.putExtra(PlayerDetailActivity.ID_POS, iscoach);
                startActivity(intent1);
                break;
            case R.id.player2:
                Intent player2 = new Intent(getActivity(), PlayerDetailActivity.class);
                player2.putExtra(PlayerDetailActivity.ID_KEY, Integer.valueOf(players.get(1).getPlayer_id()));
                boolean iscoach1;
                int is_coach1 = players.get(1).getIs_coach();
                if (is_coach1 == 1) {
                    iscoach1 = true;
                } else {
                    iscoach1 = false;
                }
                player2.putExtra(PlayerDetailActivity.ID_POS, iscoach1);
                startActivity(player2);
                break;
            case R.id.player3:
                Intent player3 = new Intent(getActivity(), PlayerDetailActivity.class);
                player3.putExtra(PlayerDetailActivity.ID_KEY, Integer.valueOf(players.get(2).getPlayer_id()));
                boolean iscoach2;
                int is_coach2 = players.get(2).getIs_coach();
                if (is_coach2 == 1) {
                    iscoach2 = true;
                } else {
                    iscoach2 = false;
                }
                player3.putExtra(PlayerDetailActivity.ID_POS, iscoach2);
                startActivity(player3);
                break;
            case R.id.player4:
                Intent player4 = new Intent(getActivity(), PlayerDetailActivity.class);
                player4.putExtra(PlayerDetailActivity.ID_KEY, Integer.valueOf(players.get(3).getPlayer_id()));
                boolean iscoach3;
                int is_coach3 = players.get(3).getIs_coach();
                if (is_coach3 == 1) {
                    iscoach3 = true;
                } else {
                    iscoach3 = false;
                }
                player4.putExtra(PlayerDetailActivity.ID_POS, iscoach3);
                startActivity(player4);
                break;
        }
    }

    class HomeVpAdapter extends PagerAdapter {

        public HomeVpAdapter(Context context) {

        }

        @Override
        public int getCount() {
            return scheduleView.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = scheduleView.get(position);
            if (view.getParent() == null) {
                container.addView(view);
            }

            return view;
        }
    }

    private void initBanner() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.HOME_URL + NetworkOper.buildQueryParam("get_focus_2016", 10, "", 0, null))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
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
                                banner.add(homeBanner);
                                topBanner.setPages(new CBViewHolderCreator<TopBannerHolder>() {
                                    @Override
                                    public TopBannerHolder createHolder() {
                                        return new TopBannerHolder();
                                    }
                                }, banner).setPageIndicator(new int[]{R.mipmap.in_unfocus, R.mipmap.in_focus}).setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void initCenterBanner() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.HOME_URL + NetworkOper.buildQueryParam("get_mid_adv", 0, "", 0, null))
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String json = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject object = new JSONObject(json);
                            JSONArray data = object.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                String id = obj.getString("id");
                                String pic = obj.getString("pic");
                                String url = obj.getString("url");
                                HomeCenterBanner hBanner = new HomeCenterBanner(id, pic, url);
                                centerBanner.add(hBanner);
                            }
                            String pic = AddressUtils.IMAGE_PREFIX + centerBanner.get(0).getPic();
                            GlideImageLoader.loadImage(getActivity(), pic, cBanner);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void initVideo() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.HOME_URL + NetworkOper.buildQueryParam("get_home_album", 0, "", 0, null))
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
                                JSONArray data = object.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);
                                    String title = obj.getString("title");
                                    String content = obj.getString("content");
                                    String pic = obj.getString("pic");
                                    String url = obj.getString("url");
                                    int orderby = obj.getInt("orderby");
                                    String date = obj.getString("date");
                                    int show_type = obj.getInt("show_type");
                                    int show_id = obj.getInt("show_id");
                                    HomeVideoBanner hvm = new HomeVideoBanner(title, content, pic, url, orderby, date, show_type, show_id);
                                    video.add(hvm);
                                }
                                for (int i = 0; i < video.size(); i++) {
                                    RelativeLayout layout = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.layout_video_home, null);
                                    TextView title = (TextView) layout.findViewById(R.id.title);
                                    ImageView videoPic = (ImageView) layout.findViewById(R.id.videoPic);
                                    title.setText(video.get(i).getTitle());
                                    GlideImageLoader.loadImage(getActivity(), AddressUtils.IMAGE_PREFIX + video.get(i).getPic(), videoPic);
                                    videoView.add(layout);
                                }
                                VideoVpAdapter adapter = new VideoVpAdapter();
                                videoVp.setAdapter(adapter);
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

    private void initSchedule() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.HOME_URL + NetworkOper.buildQueryParam("get_home_schedule_board", 0, "", 0, null))
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {

                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                final String json = response.body().string();
                                                getActivity().runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    try {
                                                                                        JSONObject object = new JSONObject(json);
                                                                                        JSONArray data = object.getJSONArray("data");
                                                                                        for (int i = 0; i < data.length(); i++) {
                                                                                            JSONObject obj = data.getJSONObject(i);
                                                                                            String game_id = obj.getString("game_id");
                                                                                            String league_id = obj.getString("league_id");
                                                                                            String match_day = obj.getString("match_day");
                                                                                            String match_date_cn = obj.getString("match_date_cn");
                                                                                            String home_id = obj.getString("home_id");
                                                                                            String home_name = obj.getString("home_name");
                                                                                            String home_score = obj.getString("home_score");
                                                                                            String away_id = obj.getString("away_id");
                                                                                            String away_name = obj.getString("away_name");
                                                                                            String away_score = obj.getString("away_score");
                                                                                            String half_score = obj.getString("half_score");
                                                                                            String game_status = obj.getString("game_status");
                                                                                            int news_link = obj.getInt("news_link");
                                                                                            int album_link = obj.getInt("album_link");
                                                                                            String relay_info = obj.getString("relay_info");
                                                                                            String home_logo = obj.getString("home_logo");
                                                                                            String away_logo = obj.getString("away_logo");
                                                                                            String league_title = obj.getString("league_title");
                                                                                            int show_default = obj.getInt("show_default");
                                                                                            HomeScheduleBoaed borad = new HomeScheduleBoaed(game_id, league_id, match_day, match_date_cn, home_id, home_name, home_score, away_id, away_name, away_score, half_score, game_status, news_link, album_link, relay_info, home_logo, away_logo, league_title, show_default);
                                                                                            schedule.add(borad);
                                                                                        }
                                                                                        for (int i = 0; i < schedule.size(); i++) {
                                                                                            LinearLayout layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.list_item_schedule3, null);
                                                                                            TextView match_type = (TextView) layout.findViewById(R.id.match_type);
                                                                                            TextView match_date = (TextView) layout.findViewById(R.id.match_date);
                                                                                            TextView match_time = (TextView) layout.findViewById(R.id.match_time);
                                                                                            TextView whole_score = (TextView) layout.findViewById(R.id.whole_score);
                                                                                            TextView homeName = (TextView) layout.findViewById(R.id.host_name);
                                                                                            TextView awyName = (TextView) layout.findViewById(R.id.away_name);
                                                                                            TextView half_score = (TextView) layout.findViewById(R.id.half_score);

                                                                                            RecyclingImageView home = (RecyclingImageView) layout.findViewById(R.id.host_image);
                                                                                            RecyclingImageView away = (RecyclingImageView) layout.findViewById(R.id.away_image);


                                                                                            match_type.setText(schedule.get(i).getLeague_title());

                                                                                            GlideImageLoader.loadImage(getActivity(), schedule.get(i).getHome_logo(), home);
                                                                                            GlideImageLoader.loadImage(getActivity(), schedule.get(i).getAway_logo(), away);

                                                                                            match_date.setText(DateUtils.getDate(Long.valueOf(schedule.get(i).getMatch_date_cn())));
                                                                                            match_time.setText(DateUtils.getTime(Long.valueOf(schedule.get(i).getMatch_date_cn())));
                                                                                            homeName.setText(schedule.get(i).getHome_name());
                                                                                            awyName.setText(schedule.get(i).getAway_name());
                                                                                            whole_score.setText(schedule.get(i).getHome_score() + ":" + schedule.get(i).getAway_score());
                                                                                            half_score.setText(schedule.get(i).getHalf_score());
                                                                                            scheduleView.add(layout);
                                                                                        }
                                                                                        HomeVpAdapter adapter = new HomeVpAdapter(getActivity());
                                                                                        vp.setAdapter(adapter);
                                                                                        for (int i = 0; i < schedule.size(); i++) {
                                                                                            if (schedule.get(i).getShow_default() == 1) {
                                                                                                vp.setCurrentItem(i);
                                                                                            }
                                                                                        }

                                                                                        adapter.notifyDataSetChanged();
                                                                                    } catch (JSONException e) {
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                }
                                                                            }
                                                );
                                            }
                                        }
        );
    }

    private void initPlayer() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(AddressUtils.HOME_URL + NetworkOper.buildQueryParam("get_home_player", 0, "", 0, null))
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    final String json = response.body().string();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mainLayout.setVisibility(View.VISIBLE);
                                JSONObject object = new JSONObject(json);
                                JSONArray data = object.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);
                                    String name = obj.getString("name");
                                    String player_id = obj.getString("player_id");
                                    String number = obj.getString("number");
                                    String pic = obj.getString("pic");
                                    int is_coach = obj.getInt("is_coach");
                                    HomePlayer player = new HomePlayer(name, player_id, number, pic, is_coach);
                                    players.add(player);
                                }
                                if (players.size() != 0) {

                                    GlideImageLoader.loadImage(getActivity(), players.get(0).getPic(), player1);
                                    GlideImageLoader.loadImage(getActivity(), players.get(1).getPic(), player2);
                                    GlideImageLoader.loadImage(getActivity(), players.get(2).getPic(), player3);
                                    GlideImageLoader.loadImage(getActivity(), players.get(3).getPic(), player4);

                                    name1.setText(players.get(0).getName());
                                    name2.setText(players.get(1).getName());
                                    name3.setText(players.get(2).getName());
                                    name4.setText(players.get(3).getName());

                                    number1.setText(players.get(0).getNumber());
                                    number2.setText(players.get(1).getNumber());
                                    number3.setText(players.get(2).getNumber());
                                    number4.setText(players.get(3).getNumber());
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
                    if (banner.get(position).getShow_type() == 1) {
                        Intent intent = new Intent(context, NewsWebActivity2.class);
                        intent.putExtra("nid", String.valueOf(banner.get(position).getShow_id()));
                        intent.putExtra("url", banner.get(position).getUrl());
                        startActivity(intent);
                    } else if (banner.get(position).getShow_type() == 2) {
                        Intent intent = new Intent(getActivity(), PhotoActivity.class);
                        intent.putExtra(PhotoActivity.KEY_CONTENT, banner.get(position).getShow_id());
                        intent.putExtra(PhotoActivity.KEY_TYPE, NetworkOper.Req.PHOTO);
                        startActivity(intent);
                    } else if (banner.get(position).getShow_type() == 3 || banner.get(position).getShow_type() == 0) {
                        Intent intent = new Intent(context, WebActivity2.class);
                        intent.putExtra("url", banner.get(position).getUrl());
                        startActivity(intent);
                    }
                }
            });
        }
    }

    public class VideoVpAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return videoView.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(videoView.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            container.addView(videoView.get(position));
            View view = videoView.get(position);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), PhotoActivity.class);
                    intent.putExtra(PhotoActivity.KEY_CONTENT, video.get(position).getShow_id());
                    intent.putExtra(PhotoActivity.KEY_TYPE, NetworkOper.Req.PHOTO);
                    startActivity(intent);
                }
            });
            return view;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        topBanner.startTurning(4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        topBanner.stopTurning();
    }


}