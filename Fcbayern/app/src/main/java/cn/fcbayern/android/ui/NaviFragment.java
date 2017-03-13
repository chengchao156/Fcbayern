package cn.fcbayern.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.fcbayern.android.MainApp;
import cn.fcbayern.android.R;
import cn.fcbayern.android.data.NaviMenuData;
import cn.fcbayern.android.util.GlideImageLoader;
import cn.fcbayern.android.util.image.RecyclingImageView;

/**
 * Created by chenzhan on 15/5/25.
 */
public class NaviFragment extends ListFragment {


    SharedPreferences loginInfo;

    private RecyclingImageView headImage;
    private TextView userN;

    public interface NaviSelListener {
        void onNaviItemClick(int pos);
    }

    //private int mSelPos = 0;
    private NaviSelListener mListener;

    public static NaviFragment createInstance(NaviSelListener listener) {
        NaviFragment fragment = new NaviFragment();
        fragment.setNaviSelListener(listener);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final LinearLayout loginView = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.left_slide_header, null);
        headImage = (RecyclingImageView) loginView.findViewById(R.id.headImage);
        userN = (TextView) loginView.findViewById(R.id.userName);
        loginInfo = getActivity().getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        MainApp.isLogin = loginInfo.getBoolean("isLogin", false);
        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainApp.isLogin) {
                    ((MainActivity) getActivity()).getSlideMenu().toggle();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    ((MainActivity) getActivity()).getSlideMenu().toggle();
                    Intent info = new Intent(getActivity(), PersonInfoActivity.class);
                    startActivity(info);
                }
            }
        });
        if (!MainApp.isLogin) {
            headImage.setImageResource(R.mipmap.leftnav_avatar);
            userN.setText("登 录 / 注 册");
        } else {
            MainApp.UserInfo info = MainApp.UserInfo.getUserInfo();
            if (info.getUsername() != null && info.getAvatar() != null) {
                String userNmae = info.getUsername();
                String avatar = info.getAvatar();
                GlideImageLoader.loadHeadImage(getActivity(), avatar, headImage);
                userN.setText(userNmae);
            }else {
                headImage.setImageResource(R.mipmap.leftnav_avatar);
                userN.setText("登 录 / 注 册");
                MainApp.isLogin = false;
            }
        }
        getListView().addHeaderView(loginView);
        getListView().setAdapter(new NaviAdapter(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_navi, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setItemChecked(1, true);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                 @Override
                                                 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                     if (mListener != null && position != 0)
                                                         mListener.onNaviItemClick(position - 1);
                                                 }
                                             }
        );
    }

    public void setNaviSel(int pos) {
        getListView().setItemChecked(pos - 1, true);
    }

    public void setNaviSelListener(NaviSelListener listener) {
        mListener = listener;
    }

    class NaviAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public NaviAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return NaviMenuData.modelsList.size();
        }

        @Override
        public Object getItem(int position) {
            return NaviMenuData.modelsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_navi, parent, false);
            }
            NaviMenuData model = NaviMenuData.modelsList.get(position);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(model.iconRes);
            TextView text = (TextView) convertView.findViewById(R.id.name);
            text.setText(model.strRes);
            TextView txtTag = (TextView) convertView.findViewById(R.id.nid);
            txtTag.setText(model.alias);
            return convertView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainApp.isLogin == false) {
            headImage.setImageResource(R.mipmap.leftnav_avatar);
            userN.setText("登 录 / 注 册");
        } else {
            MainApp.UserInfo info = MainApp.UserInfo.getUserInfo();
            if (info != null) {
                String userNmae = info.getUsername();
                String avatar = info.getAvatar();
                GlideImageLoader.loadHeadImage(getActivity(), avatar, headImage);
                userN.setText(userNmae);
            } else {
                headImage.setImageResource(R.mipmap.leftnav_avatar);
                userN.setText("登 录 / 注 册");
            }
        }
    }
}
