package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.model.VideoModel;
import cn.fcbayern.android.network.NetworkOper;
import cn.fcbayern.android.R;
import cn.fcbayern.android.common.ContentTextView;
import cn.fcbayern.android.common.ViewPager2;
import cn.fcbayern.android.common.pageindicator.CirclePageIndicator;
import cn.fcbayern.android.model.AdsModel;
import cn.fcbayern.android.model.BaseModel;
import cn.fcbayern.android.model.NewsModel;
import cn.fcbayern.android.model.PhotoModel;
import cn.fcbayern.android.util.AddressUtils;
import cn.fcbayern.android.util.AppConfig;
import cn.fcbayern.android.util.DataReport;
import cn.fcbayern.android.util.DateUtils;
import cn.fcbayern.android.util.DeviceUtils;
import cn.fcbayern.android.util.Utils;
import cn.fcbayern.android.util.ViewHolder.NewsHolder;
import cn.fcbayern.android.util.ViewHolder.PhotosHolder;
import cn.fcbayern.android.util.ViewHolder.AdsHolder;
import cn.fcbayern.android.util.ViewHolder.VideoHolder;


/**
 * Created by chenzhan on 15/5/26.
 */
public class HomeFragment extends RefreshFragment {

    private HomeAdapter mAdapter;
    private FocusPagerAdapter mFocusAdapter;
    private ViewPager mFocusPager;
    private Handler mScrollHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScrollHandler = new Handler();
        ((MainActivity) getActivity()).showLoadingDlg();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_content, container, false);
        mListView = (PullToRefreshListView) root.findViewById(R.id.list);

        View header = inflater.inflate(R.layout.layout_home_focus, null);
        mListView.getRefreshableView().addHeaderView(header);

        mFocusPager = (ViewPager2) header.findViewById(R.id.pics);
        mFocusAdapter = new FocusPagerAdapter(getActivity());
        mFocusPager.setAdapter(mFocusAdapter);

        RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) mFocusPager.getLayoutParams();
        layout.height = (int) (DeviceUtils.getScreenWidth(getActivity()) * Utils.FOCUS_RATE);

        CirclePageIndicator indicator = (CirclePageIndicator) header.findViewById(R.id.banner_indicator);
        indicator.setViewPager(mFocusPager);

        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getSlideMenu().addIgnoredView(mFocusPager);
        }

        mAdapter = new HomeAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(new TextView(getActivity()));
        mListView.setMode(PullToRefreshBase.Mode.BOTH);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                NetworkOper.getList(NetworkOper.Req.HOME, HomeFragment.this, true);
                NetworkOper.getList(NetworkOper.Req.FOCUS, null, "", 5, HomeFragment.this);
                DataReport.report(getActivity(), DataReport.INDEX_EVENTID, DataReport.INDEX_KEY, DataReport.INDEX_REFRESH_VAL);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ArrayList<BaseModel> list = DataManager.getData(NetworkOper.Req.HOME);
                if (list.size() > 0) {
                    BaseModel lastNews = null;
                    BaseModel lastPhoto = null;
                    HashMap<String, String> starts = new HashMap<>();
                    for (int i = list.size() - 1; i >= 0; i--) {
                        if (lastNews == null && list.get(i).type == BaseModel.NEWS_ITEM) {
                            lastNews = list.get(i);
                            starts.put("last_news_id", String.valueOf(lastNews.id));
                        }
                        if (lastPhoto == null && list.get(i).type == BaseModel.PHOTO_ITEM) {
                            lastPhoto = list.get(i);
                            starts.put("last_album_id", String.valueOf(lastPhoto.id));
                        }
                        if (lastNews != null && lastPhoto != null) {
                            break;
                        }
                    }
                    String lastTime = list.get(list.size() - 1).time;
                    NetworkOper.getList(NetworkOper.Req.HOME, starts, lastTime, AppConfig.HOME_PAGE_COUNT, HomeFragment.this);
                    DataReport.report(getActivity(), DataReport.INDEX_EVENTID, DataReport.INDEX_KEY, DataReport.INDEX_LOAD_VAL);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BaseModel model = (BaseModel) mAdapter.getItem((int) id);//DataManager.getDataList(DataManager.HOME_TYPE).get((int) id);
                if (model.type == BaseModel.PHOTO_ITEM) {
                    Intent intent = new Intent(getActivity(), PhotoActivity.class);
                    intent.putExtra(PhotoActivity.KEY_CONTENT, model.id);
                    intent.putExtra(PhotoActivity.KEY_TYPE, NetworkOper.Req.HOME);
                    startActivity(intent);
                    DataReport.report(getActivity(), DataReport.INDEX_EVENTID, DataReport.INDEX_KEY, DataReport.INDEX_PHOTO_VAL);
                } else if (model.type == BaseModel.NEWS_ITEM) {
                    NewsWebActivity.browse(getActivity(), AddressUtils.DETAIL_NEWS_URL + model.id, getString(R.string.news), NetworkOper.Req.HOME);
                    DataReport.report(getActivity(), DataReport.INDEX_EVENTID, DataReport.INDEX_KEY, DataReport.INDEX_NEWS_VAL);
                } else if (model.type == BaseModel.ADS_ITEM) {
                    AdsModel ads = (AdsModel) model;
                    WebActivity.browse(getActivity(), ads.url, "", -1);
                } else if (model.type == BaseModel.VIDEO_ITEM) {
                    VideoModel video = (VideoModel) model;
                    ActionWebActivity.browse(getActivity(), video.url, getString(R.string.video), video.title, true);
                }
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                rotateFocus(firstVisibleItem <= 1);
            }
        });

        mMainOperator = NetworkOper.Req.HOME;
        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                NetworkOper.getList(NetworkOper.Req.FOCUS, HomeFragment.this, false);
                NetworkOper.getList(NetworkOper.Req.HOME, HomeFragment.this, false);
                NetworkOper.getList(NetworkOper.Req.HOME_ADS, HomeFragment.this, true);
            }
        }, 200);

        return root;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if(getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).getSlideMenu().addIgnoredView(mFocusPager);
            }
        }
    }

    private void rotateFocus(boolean begin) {
        if (mScrollHandler != null) {
            if (begin) {
                mScrollHandler.removeCallbacksAndMessages(null);
                mScrollHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int index = mFocusPager.getCurrentItem();
                        index = index + 1;
                        mFocusPager.setCurrentItem(index);
                        mScrollHandler.postDelayed(this, 5000);
                    }
                }, 5000);
            } else {
                mScrollHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    @Override
    public void loadComplete(final int errorCode, final boolean fromCache, final int operator, final boolean append) {

        if (getActivity() == null) return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (getActivity() != null) {
                    ((MainActivity) getActivity()).dismissDlg();
                }

                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                if (mFocusAdapter != null) {
                    mFocusPager.setAdapter(mFocusAdapter);
                }
                mListView.onRefreshComplete();
            }
        });

        super.loadComplete(errorCode, fromCache, operator, append);
    }

    static class HomeAdapter extends BaseAdapter {

        private static final int NEWS_TYPE = 0;
        private static final int PHOTO_TYPE = 1;
        private static final int VIDEO_TYPE = 2;
        private static final int ADS_TYPE = 3;
        private static final int SUM = 4;

        private static final int ADS_1 = 0;
        private static final int ADS_2 = 8;

        private LayoutInflater mInflater;

        private ArrayList<BaseModel> mListData = new ArrayList<>();

        public HomeAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void notifyDataSetChanged() {
            mListData.clear();
            if (DataManager.getData(NetworkOper.Req.HOME).size() > 0) {
                if (DataManager.getData(NetworkOper.Req.HOME_ADS).size() > 0) {
                    mListData.add(ADS_1, DataManager.getData(NetworkOper.Req.HOME_ADS).get(0));
                    mListData.addAll(DataManager.getData(NetworkOper.Req.HOME));
                    mListData.add(ADS_2, DataManager.getData(NetworkOper.Req.HOME_ADS).get(1));
                } else {
                    mListData.addAll(DataManager.getData(NetworkOper.Req.HOME));
                }
            }
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            int type = ((BaseModel)getItem(position)).type;
            if (type == BaseModel.ADS_ITEM) {
                return ADS_TYPE;
            } else if (type == BaseModel.VIDEO_ITEM) {
                return VIDEO_TYPE;
            } else if (type == BaseModel.NEWS_ITEM) {
                return NEWS_TYPE;
            }
            return PHOTO_TYPE;
        }

        @Override
        public int getViewTypeCount() {
            return SUM;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int itemType = getItemViewType(position);
            Object item = getItem(position);
            if (itemType == NEWS_TYPE) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_news, parent, false);
                    final NewsHolder holder = new NewsHolder();
                    holder.image = (ImageView) convertView.findViewById(R.id.image);
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.content = (ContentTextView) convertView.findViewById(R.id.content);
                    holder.time = (TextView) convertView.findViewById(R.id.time);
                    convertView.setTag(holder);
                }
            } else if (itemType == ADS_TYPE) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_ads, parent, false);
                    final AdsHolder holder = new AdsHolder();
                    holder.image = (ImageView) convertView.findViewById(R.id.image);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.image.getLayoutParams();
                    lp.width = DeviceUtils.getScreenWidth(mInflater.getContext())
                            - convertView.getPaddingLeft() - convertView.getPaddingRight();
                    lp.height = (int) (lp.width * Utils.ADS_RATE);
                    convertView.setTag(holder);
                }
            } else if (itemType == VIDEO_TYPE) {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_video, parent, false);
                    final VideoHolder holder = new VideoHolder();
                    holder.image = (ImageView) convertView.findViewById(R.id.image);
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.tags = (TextView) convertView.findViewById(R.id.tags);
                    holder.time = (TextView) convertView.findViewById(R.id.time);
                    convertView.setTag(holder);
                }
            } else {
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.list_item_photos2, parent, false);
                    PhotosHolder holder = new PhotosHolder();
                    holder.image1 = (ImageView) convertView.findViewById(R.id.image1);
                    holder.image2 = (ImageView) convertView.findViewById(R.id.image2);
                    holder.image3 = (ImageView) convertView.findViewById(R.id.image3);
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.time = (TextView) convertView.findViewById(R.id.time);
                    convertView.setTag(holder);
                }
            }
            bindView(convertView, itemType, item);

            return convertView;
        }

        private void bindView(View v, int type, Object item) {
            Object obj = v.getTag();
            if (type == NEWS_TYPE && obj instanceof NewsHolder) {
                NewsHolder holder = (NewsHolder)obj;
                NewsModel model = (NewsModel)item;
                if (model != null) {
                    holder.title.setText(model.title);
                    holder.content.setContentText(model.content);
                    holder.time.setText(DateUtils.getShowTime(model.time));
                    DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.imageUrl, holder.image);
                }
            } else if (type == ADS_TYPE && obj instanceof  AdsHolder) {
                AdsHolder holder = (AdsHolder) obj;
                AdsModel model = (AdsModel)item;
                if (model != null) {
                    DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.pic, holder.image);
                }
            } else if (type == VIDEO_TYPE && obj instanceof  VideoHolder) {
                VideoHolder holder = (VideoHolder) obj;
                VideoModel model = (VideoModel)item;
                if (model != null) {
                    holder.title.setText(model.title);
                    holder.tags.setText(model.tags);
                    holder.tags.setTextColor(mInflater.getContext().getResources().getColor(R.color.colorPrimary));
                    holder.time.setText(DateUtils.getShowTime(model.time));
                    DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.imageUrl, holder.image);
                }
            } else if (type == PHOTO_TYPE && obj instanceof PhotosHolder) {
                PhotosHolder holder = (PhotosHolder)obj;
                PhotoModel model = (PhotoModel)item;
                if (model != null) {
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
                }
            }
        }
    }

    static class FocusPagerAdapter extends PagerAdapter  {

        private LayoutInflater mInflater;
        private Context mContext;

        private View.OnClickListener mClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (Integer) v.getTag();
                PhotoModel model = (PhotoModel) DataManager.getData(NetworkOper.Req.FOCUS).get(index);
                WebActivity.browse(mContext, model.url, "", -1);
            }
        };

        public FocusPagerAdapter(Context context) {
            super();
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            ArrayList<BaseModel> model = DataManager.getData(NetworkOper.Req.FOCUS);
            if (model == null || model.size() <= 0) {
                return 0;
            }
            return DataManager.getData(NetworkOper.Req.FOCUS).size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (getCount() <= 0) return null;
            PhotoModel model = (PhotoModel)DataManager.getData(NetworkOper.Req.FOCUS).get(position);
            View view = mInflater.inflate(R.layout.layout_item_focus, null);
            ImageView image = (ImageView) view.findViewById(R.id.image);
            TextView title = (TextView) view.findViewById(R.id.title);
            view.setTag(position);
            title.setText(model.title);
            view.setOnClickListener(mClick);
            DataManager.getImageLoader().loadImage(AddressUtils.IMAGE_PREFIX + model.imageUrl, image);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
