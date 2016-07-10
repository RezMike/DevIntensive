package com.softdesign.devintensive.ui.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.UiHelper;

public class RatingBarBehavior<V extends LinearLayout> extends AppBarLayout.ScrollingViewBehavior {
    private final int mMaxAppbarHeight;
    private final int mMinAppbarHeight;
    private final int mMaxRatingBarHeight;
    private final int mMinRatingBarHeight;

    public RatingBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingBarBehavior);
        mMinRatingBarHeight = a.getDimensionPixelOffset(R.styleable.RatingBarBehavior_behavior_min_height, 56);
        a.recycle();
        mMinAppbarHeight = UiHelper.getStatusBarHeight() + UiHelper.getActionBarHeight();
        mMaxAppbarHeight = context.getResources().getDimensionPixelSize(R.dimen.profile_image_size_256);
        mMaxRatingBarHeight = context.getResources().getDimensionPixelSize(R.dimen.user_info_size_112);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float currentFriction = UiHelper.currentFriction(mMinAppbarHeight, mMaxAppbarHeight, dependency.getBottom());
        int currentHeight = UiHelper.lerp(mMinRatingBarHeight, mMaxRatingBarHeight, currentFriction);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.height = currentHeight;
        child.setLayoutParams(lp);

        return super.onDependentViewChanged(parent, child, dependency);
    }
}
