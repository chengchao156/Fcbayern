package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.R;
import cn.fcbayern.android.model.PlayerModel;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.DeviceUtils;
import cn.fcbayern.android.util.ViewHolder.PlayerHolder;


/**
 * Created by chenzhan on 15/5/26.
 */
public class TeamFragment extends TabFragment {

    private SparseArray<ArrayList<PlayerModel>> mDatas = new SparseArray<>();

    @Override
    public void loadComplete(int errorCode, boolean fromCache, int type, boolean append) {

        ArrayList<BaseModel> lists = DataManager.getData(NetworkOper.Req.TEAM);
        for (int i = 0; i < lists.size(); i++) {
            PlayerModel model = (PlayerModel) lists.get(i);
            int tabName = Integer.parseInt(model.position);
            if (mDatas.get(tabName) == null) {
                ArrayList<PlayerModel> list = new ArrayList<>();
                mDatas.put(tabName, list);
            }
            mDatas.get(tabName).add(model);
        }
        super.loadComplete(errorCode, fromCache, type, append);
    }

    @Override
    protected String getTabTitle(int position) {
        int type = mDatas.keyAt(position);
        return getString(PlayerModel.getPlayerType(type));
    }

    @Override
    protected int getTabCount() {
        return mDatas.size();
    }

    @Override
    protected ItemClickListener getItemClickListener() {
        return new ItemClickListener() {
            @Override
            public void onItemClick(int tabPos, int position) {
                Intent intent = new Intent(getActivity(), PlayerDetailActivity.class);
                int type = mDatas.keyAt(tabPos);
                PlayerModel model =  mDatas.get(type).get(position);
                intent.putExtra(PlayerDetailActivity.ID_KEY, model.id);
                intent.putExtra(PlayerDetailActivity.ID_POS, PlayerModel.isCoach(Integer.valueOf(model.position)));
                startActivity(intent);
            }
        };
    }

    @Override
    protected BaseAdapter getSubAdapter(Context context, int position) {
        PlayAdapter adapter = new PlayAdapter(context);
        int key = mDatas.keyAt(position);
        adapter.setData(mDatas.get(key));
        return adapter;
    }

    @Override
    protected void fetchData(DataManager.DataLoadListListener listener) {
        NetworkOper.getList(NetworkOper.Req.TEAM, listener, true);
    }

    public static class PlayAdapter extends BaseAdapter {

        private ArrayList<PlayerModel> mDatas;
        private LayoutInflater mInflater;

        public PlayAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<PlayerModel> list) {
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
            PlayerHolder playerHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_player, parent, false);
                playerHolder = new PlayerHolder();
                playerHolder.image = (ImageView) convertView.findViewById(R.id.image);
                playerHolder.name = (TextView) convertView.findViewById(R.id.name);
                playerHolder.nameEn = (TextView) convertView.findViewById(R.id.name_en);
                playerHolder.birth = (TextView) convertView.findViewById(R.id.birthday);
                playerHolder.type = (TextView) convertView.findViewById(R.id.type);
                playerHolder.number = (TextView) convertView.findViewById(R.id.number);
                convertView.setTag(playerHolder);
            } else {
                playerHolder = (PlayerHolder) convertView.getTag();
            }

            PlayerModel model = mDatas.get(position);

            int height = convertView.getLayoutParams().height - DeviceUtils.dip2px(parent.getContext(), 10);
            int width = height * model.imageW / model.imageH;
            ViewGroup.LayoutParams lp = playerHolder.image.getLayoutParams();
            lp.width = width;

            playerHolder.number.setText(model.number);
            playerHolder.type.setText(PlayerModel.getPlayerType(Integer.parseInt(model.position)));
            playerHolder.birth.setText(model.birthday);
            playerHolder.nameEn.setText(model.nameEn);
            playerHolder.name.setText(model.name);
            DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.imageUrl, playerHolder.image);
            return convertView;
        }
    }

}
