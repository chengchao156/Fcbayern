package cn.fcbayern.android.model;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by chengchao on 2016/10/10.
 */
public class RankTabAdapter extends FragmentPagerAdapter {

    List<Fragment> fragments;
    List<String> tabName;

    public RankTabAdapter(FragmentManager fm, List<Fragment> fragments, List<String> tabName) {
        super(fm);
        this.fragments = fragments;
        this.tabName = tabName;
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments == null ? 0 : this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null != tabName && !tabName.isEmpty() ? tabName.get(position) : null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }
}