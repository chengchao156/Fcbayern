package cn.fcbayern.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.fcbayern.android.R;
import cn.fcbayern.android.model.Rank;
import cn.fcbayern.android.model.RankModel;
import cn.fcbayern.android.util.GlideImageLoader;
import cn.fcbayern.android.util.image.RecyclingImageView;


public class RankTabFragment extends Fragment {

    public static final String TAG = "list";
    private String url;

    private ArrayList<Rank>data;
    private ListView lv;

public static RankTabFragment newInstance(ArrayList<Rank>data) {

    Bundle args = new Bundle();

    RankTabFragment fragment = new RankTabFragment();
    fragment.setArguments(args);

    args.putParcelableArrayList(TAG,data);

    return fragment;
}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments()) {
            data = getArguments().getParcelableArrayList(TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rank_tab, container, false);
        lv = (ListView) view.findViewById(R.id.rankLv);
        RankAdapter adapter = new RankAdapter(getActivity());
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return view;
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
                convertView = mInflater.inflate(R.layout.list_item_rank, parent, false);
            }

            TextView pos = (TextView) convertView.findViewById(R.id.pos);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            RecyclingImageView image = (RecyclingImageView) convertView.findViewById(R.id.image);
            TextView win = (TextView) convertView.findViewById(R.id.win);
            TextView draw = (TextView) convertView.findViewById(R.id.draw);
            TextView lost = (TextView) convertView.findViewById(R.id.lost);
            TextView goal = (TextView) convertView.findViewById(R.id.goal);
            TextView score = (TextView) convertView.findViewById(R.id.score);

            Rank rank =data.get(position);
            pos.setText(rank.getRank_index() + "");
            if (rank.getTeam_id()== RankModel.BAYERN_ID){
                convertView.setBackgroundResource(R.color.colorPrimary);
                pos.setTextColor(getResources().getColor(R.color.textColorPrimary));
                name.setTextColor(getResources().getColor(R.color.textColorPrimary));
                win.setTextColor(getResources().getColor(R.color.textColorPrimary));
                lost.setTextColor(getResources().getColor(R.color.textColorPrimary));
                draw.setTextColor(getResources().getColor(R.color.textColorPrimary));
                goal.setTextColor(getResources().getColor(R.color.textColorPrimary));
                score.setTextColor(getResources().getColor(R.color.textColorPrimary));
            }else {
                convertView.setBackgroundResource(R.color.textColorPrimary);
                pos.setTextColor(getResources().getColor(R.color.black));
                name.setTextColor(getResources().getColor(R.color.black));
                win.setTextColor(getResources().getColor(R.color.black));
                lost.setTextColor(getResources().getColor(R.color.black));
                draw.setTextColor(getResources().getColor(R.color.black));
                goal.setTextColor(getResources().getColor(R.color.black));
                score.setTextColor(getResources().getColor(R.color.black));
            }
            name.setText(rank.getKnown_name_zh());
            GlideImageLoader.loadImage(getActivity(), rank.getTeam_logo(), image);
            win.setText(rank.getWin()+"");
            draw.setText(rank.getDraw()+"");
            lost.setText(rank.getLost()+"");
            goal.setText(rank.getHits() + "/" + rank.getMiss());
            score.setText(rank.getScore() + "");
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {

            return false;
        }
    }
}