package com.example.firebaseapp.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.firebaseapp.fragments.ArticlesFragment;
import com.example.firebaseapp.fragments.ForumFragment;
import com.example.firebaseapp.fragments.FeedsFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new FeedsFragment();
                return fragment;

            case 1:
                fragment = new ArticlesFragment();
                return fragment;

            case 2:
                fragment = new ForumFragment();
                return fragment;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String[] title = {"Feeds", "Articles", "Forum"};
        return title[position];
    }
}
