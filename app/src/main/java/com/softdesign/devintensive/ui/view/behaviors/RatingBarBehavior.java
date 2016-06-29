package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class RatingBarBehavior extends CoordinatorLayout.Behavior<LinearLayout> {
    private Context mContext;
    private float mMaxDependencyTop;
    private float mChildPadding;

    public RatingBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        Log.d("behavior", String.valueOf(dependency.getY()));

        if (mMaxDependencyTop == 0){
            mMaxDependencyTop = dependency.getTop();
            mChildPadding = child.getPaddingTop();
            dependency.setPadding(0, child.getHeight(), 0, 0);
        }

        float minDependencyTop = getActionBarHeight() + getStatusBarHeight();
        float diffPadding = (dependency.getTop() - minDependencyTop) / (mMaxDependencyTop - minDependencyTop);
        int padding = (int) (mChildPadding * diffPadding);
        child.setPadding(0, padding, 0, padding);
        dependency.setPadding(0, child.getHeight(), 0, 0);

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
