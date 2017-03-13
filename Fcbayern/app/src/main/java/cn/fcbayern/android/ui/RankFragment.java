package cn.fcbayern.android.ui;

import android.content.Context;
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
import cn.fcbayern.android.model.RankModel;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.util.ViewHolder.RankHolder;


/**
 * Created by chenzhan on 15/5/26.
 */
public class RankFragment extends TabFragment {

    private ArrayMap<String, ArrayList<RankModel>> mDatas = new ArrayMap<>();

    @Override
    public void loadComplete(int errorCode, boolean fromCache, int type, boolean append) {

        ArrayList<BaseModel> lists = DataManager.getData(NetworkOper.Req.STAND);

        for (String leagueName : RankModel.leagues) {
            ArrayList<RankModel> list = new ArrayList<>();
            mDatas.put(leagueName, list);
        }
        for (int i = 0; i < lists.size(); i++) {
            RankModel model = (RankModel) lists.get(i);
            String tabName = model.league;
            mDatas.get(tabName).add(model);
        }

        super.loadComplete(errorCode, fromCache, type, append);
    }

    @Override
    protected String getTabTitle(int position) {
        return mDatas.keyAt(position);
    }

    @Override
    protected int getTabCount() {
        return mDatas.size();
    }

    @Override
    protected BaseAdapter getSubAdapter(Context context, int position) {
        StandAdapter adapter = new StandAdapter(context);
        adapter.setData(mDatas.valueAt(position));
        return adapter;
    }

    @Override
    protected void fetchData(DataManager.DataLoadListListener listener) {
        NetworkOper.getList(NetworkOper.Req.STAND, listener, true);
    }

    @Override
    protected View inflateHeader(int pos) {
        ViewGroup group = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.list_header_rank, null);
        ((TextView)group.findViewById(R.id.name)).setText(R.string.team_name);
        ((TextView)group.findViewById(R.id.score)).setText(R.string.score_name);
        ((TextView)group.findViewById(R.id.goal)).setText(R.string.goal_name);
        ((TextView)group.findViewById(R.id.win)).setText(R.string.win_name);
        ((TextView)group.findViewById(R.id.draw)).setText(R.string.draw_name);
        ((TextView)group.findViewById(R.id.lost)).setText(R.string.lost_name);
        return group;
    }

    public class StandAdapter extends BaseAdapter {

        private ArrayList<RankModel> mDatas;
        private LayoutInflater mInflater;

        public StandAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<RankModel> list) {
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
            RankHolder rankHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_rank, parent, false);
                rankHolder = new RankHolder();
                rankHolder.container = convertView.findViewById(R.id.container);
                rankHolder.image = (ImageView) convertView.findViewById(R.id.image);
                rankHolder.name = (TextView) convertView.findViewById(R.id.name);
                rankHolder.pos = (TextView) convertView.findViewById(R.id.pos);
                rankHolder.win = (TextView) convertView.findViewById(R.id.win);
                rankHolder.draw = (TextView) convertView.findViewById(R.id.draw);
                rankHolder.lost = (TextView) convertView.findViewById(R.id.lost);
                rankHolder.goals = (TextView) convertView.findViewById(R.id.goal);
                rankHolder.score = (TextView) convertView.findViewById(R.id.score);
                convertView.setTag(rankHolder);
            } else {
                rankHolder = (RankHolder)convertView.getTag();
            }

            final RankModel model = mDatas.get(position);
            rankHolder.name.setText(model.name);
            rankHolder.pos.setText(String.valueOf(position + 1));
            rankHolder.win.setText(model.win);
            rankHolder.draw.setText(model.draw);
            rankHolder.lost.setText(model.lost);
            rankHolder.goals.setText(model.hits + "/" + model.miss);
            rankHolder.score.setText(model.score);

            rankHolder.container.setSelected(model.teamId == BaseModel.BAYERN_ID);
//            int textColor;
//            if (model.teamId == BaseModel.BAYERN_ID) {
//                textColor = Color.WHITE;
//                convertView.setBackgroundResource(R.color.colorPrimaryDark);
//            } else {
//                textColor = mInflater.getContext().getResources().getColor(R.color.item_title_color);
//                convertView.setBackgroundResource(R.drawable.list_item_bg);
//            }
//            rankHolder.name.setTextColor(textColor);
//            rankHolder.pos.setTextColor(textColor);
//            rankHolder.win.setTextColor(textColor);
//            rankHolder.draw.setTextColor(textColor);
//            rankHolder.lost.setTextColor(textColor);
//            rankHolder.goals.setTextColor(textColor);
//            rankHolder.score.setTextColor(textColor);

            DataManager.getImageLoader().loadImage(model.teamLogo, rankHolder.image);
            return convertView;
        }
    }

}
