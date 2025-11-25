package com.app.bemyrider.viewpager;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public abstract class WrappingFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private int mCurrentPosition = -1;

    public WrappingFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        if (!(container instanceof WrappingViewPager)) {
            throw new UnsupportedOperationException("ViewPager is not a WrappingViewPager");
        }

        Fragment fragment = (Fragment) object;
        WrappingViewPager pager = (WrappingViewPager) container;
        if (fragment != null && fragment.getView() != null) {
            if (position != mCurrentPosition) {
                mCurrentPosition = position;
            }
            pager.onPageChanged(fragment.getView());
        }
    }
}