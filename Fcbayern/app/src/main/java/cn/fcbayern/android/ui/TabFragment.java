package cn.fcbayern.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerSlidingTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import cn.fcbayern.android.data.DataManager;
import cn.fcbayern.android.R;


/**
 * Created by chenzhan on 15/5/26.
 */
public abstract class TabFragment extends RefreshFragment implements DataManager.DataLoadListListener {

    protected TabPageAdapter mTabAdapter;
    protected PagerSlidingTabStrip mTabStrip;
    protected ViewPager mPager;

    public interface ItemClickListener {
        public void onItemClick(int tabId, int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tab, container, false);

        mPager = (ViewPager) root.findViewById(R.id.pager);
        mTabAdapter = new TabPageAdapter(getChildFragmentManager());
        mPager.setAdapter(mTabAdapter);
        mTabStrip = (PagerSlidingTabStrip) root.findViewById(R.id.pager_tab_strip);

        ((MainActivity) getActivity()).getSlideMenu().addIgnoredView(mPager);

        fetchData(this);

        return root;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if(getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).getSlideMenu().addIgnoredView(mPager);
            }
        }
    }

    @Override
    public void loadComplete(final int errorCode, boolean fromCache, int type, boolean append) {

        if (getView() == null) return;

        super.loadComplete(errorCode, fromCache, type, append);

        getView().post(new Runnable() {
            @Override
            public void run() {
                mTabAdapter.notifyDataSetChanged();
                mTabStrip.setViewPager(mPager);
//                ((MainActivity) getActivity()).getSlideMenu().setMode(SlidingMenu.LEFT);
//                ((MainActivity) getActivity()).getSlideMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            }
        });
    }

    class TabPageAdapter extends FragmentStatePagerAdapter {

        public TabPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            return  new Fragment() {
                @Override
                public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                    View root = inflater.inflate(R.layout.fragment_list, container, false);
                    ListView list = (ListView) root.findViewById(android.R.id.list);
                    list.setEmptyView(root.findViewById(R.id.empty_view));
                    ViewGroup fixHeader = (ViewGroup) root.findViewById(R.id.fix_header);
                    BaseAdapter adapter = getSubAdapter(container.getContext(), position);
                    View header = inflateHeader(position);
                    if (header != null) {
//                        list.addHeaderView(Header);
                        fixHeader.addView(header, new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    list.setAdapter(adapter);
                    final ItemClickListener clickListener = getItemClickListener();
                    if (clickListener != null) {
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                clickListener.onItemClick(mPager.getCurrentItem(), position);
                            }
                        });
                    }
                    return root;
                }
            };
        }

        @Override
        public int getCount() {
            return getTabCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getTabTitle(position);
        }
    }

    protected View inflateHeader(int pos) {
        return null;
    }

    protected ItemClickListener getItemClickListener() {
        return null;
    }

    protected abstract String getTabTitle(int position);

    protected abstract int getTabCount();

    protected abstract BaseAdapter getSubAdapter(Context context, int position);

    protected abstract void fetchData(DataManager.DataLoadListListener listener);

}
