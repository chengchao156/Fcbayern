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
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.R;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.PhotoModel;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.AppConfig;
import cn.fcbayern.android.util.DataReport;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.DeviceUtils;
import cn.fcbayern.android.util.ViewHolder.PhotosHolder;

/**
 * Created by chenzhan on 15/5/26.
 */
public class PhotosFragment extends RefreshFragment {

    private PhotosAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_content, container, false);
        mListView = (PullToRefreshListView) root.findViewById(R.id.list);
        mAdapter = new PhotosAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(new TextView(getActivity()));
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.getRefreshableView().setPadding(0, DeviceUtils.dip2px(getActivity(), 5), 0, 0);
        mListView.setBackgroundColor(getResources().getColor(R.color.photo_item_color));
        //mListView.getRefreshableView().setDividerHeight(0);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                NetworkOper.getList(NetworkOper.Req.PHOTO, PhotosFragment.this, true);
                DataReport.report(getActivity(), DataReport.PHOTO_EVENTID, DataReport.PHOTO_KEY, DataReport.PHOTO_REFRESH_VAL);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ArrayList<BaseModel> list = DataManager.getData(NetworkOper.Req.PHOTO);
                if (list.size() > 0) {
                    BaseModel obj = list.get(list.size() - 1);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("last_album_id", String.valueOf(obj.id));
                    NetworkOper.getList(NetworkOper.Req.PHOTO, params, obj.time, AppConfig.PHOTO_PAGE_COUNT, PhotosFragment.this);
                    DataReport.report(getActivity(), DataReport.PHOTO_EVENTID, DataReport.PHOTO_KEY, DataReport.PHOTO_LOAD_VAL);
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoModel model = (PhotoModel) mAdapter.getItem(position-1);
                Intent intent = new Intent(getActivity(), PhotoActivity.class);
                intent.putExtra(PhotoActivity.KEY_CONTENT, model.id);
                intent.putExtra(PhotoActivity.KEY_TYPE, NetworkOper.Req.PHOTO);
                startActivity(intent);
                DataReport.report(getActivity(), DataReport.PHOTO_EVENTID, DataReport.PHOTO_KEY, DataReport.PHOTO_PHOTOS_VAL);
            }
        });

        mMainOperator = NetworkOper.Req.PHOTO;
        NetworkOper.getList(NetworkOper.Req.PHOTO, this, false);

        return root;
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

    static class PhotosAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public PhotosAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return DataManager.getData(NetworkOper.Req.PHOTO).size();
        }

        @Override
        public Object getItem(int position) {
            return DataManager.getData(NetworkOper.Req.PHOTO).get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PhotosHolder holder;
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_photos, parent, false);
                holder = new PhotosHolder();
                holder.image1 = (ImageView) convertView.findViewById(R.id.image1);
                holder.image2 = (ImageView) convertView.findViewById(R.id.image2);
                holder.image3 = (ImageView) convertView.findViewById(R.id.image3);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            } else {
                holder = (PhotosHolder)convertView.getTag();
            }

            PhotoModel model = (PhotoModel)getItem(position);
            holder.title.setText(model.title);
            holder.image1.setImageBitmap(null);
            holder.image2.setImageBitmap(null);
            holder.image3.setImageBitmap(null);
            holder.time.setText(DateUtils.getShowTime(model.time));

            switch (model.thumbsPic.size()) {
                case 3:
                    DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.thumbsPic.get(2), holder.image3);
                case 2:
                    DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.thumbsPic.get(1), holder.image2);
                case 1:
                    DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.thumbsPic.get(0), holder.image1);
            }
            return convertView;
        }
    }

}
