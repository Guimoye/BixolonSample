package com.bixolon.sample;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

public class TabPagerAdapter extends FragmentStatePagerAdapter
{
    private Context mContext;
    SparseArray<Fragment> registeredFragments = new SparseArray<>();
    private TextFragment mTextFragment = null;
    private int[] mTabTitle = {R.string.Text};

    public TabPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if(mTextFragment == null) {
                    mTextFragment = mTextFragment.newInstance();
                }
                return mTextFragment;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(mTabTitle[position]);
    }

    @Override
    public int getCount() {
        return mTabTitle.length;
    }
}
