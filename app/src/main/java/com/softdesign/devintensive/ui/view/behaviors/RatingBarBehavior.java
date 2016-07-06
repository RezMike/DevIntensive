package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.softdesign.devintensive.R;

public class RatingBarBehavior extends CoordinatorLayout.Behavior<LinearLayout> {
    private Context mContext;
    private float mMaxDependencyHeight;
    private int mChildHeight;

    public RatingBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, LinearLayout child) {
        Bundle state = new Bundle();
        state.putInt("childHeight", mChildHeight);
        state.putFloat("maxDependencyHeight", mMaxDependencyHeight);
        return state;
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, LinearLayout child, Parcelable saveState) {
        if (saveState instanceof Bundle) {
            Bundle state = (Bundle)saveState;
            mChildHeight = state.getInt("childHeight");
            mMaxDependencyHeight = state.getFloat("maxDependencyHeight");
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        child.setY(dependency.getBottom());
        float dependencyHeight = dependency.getBottom() - getActionBarHeight() - getStatusBarHeight();

        if (mChildHeight == 0) {
            mMaxDependencyHeight = dependencyHeight;
        }

        float minChildHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.max_rating_size_112) / 2;
        float diffHeight = dependencyHeight / mMaxDependencyHeight;

        if (diffHeight > 1){
            diffHeight = 1;
        }

        mChildHeight = (int) (minChildHeight + minChildHeight * diffHeight);
        child.setMinimumHeight(mChildHeight);

        return true;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getActionBarHeight() {
        final TypedArray styledAttributes = mContext.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarHeight;
    }
}
