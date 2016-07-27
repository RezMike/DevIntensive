package com.softdesign.devintensive.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.softdesign.devintensive.ui.custom.RoundedDrawable;

public class CircleImageView extends ImageView {
    public CircleImageView(Context context) {
        super(context);
        //далее юзаешь свои функции
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageResource(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        setImageBitmap(bitmap);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        RoundedDrawable drawable = new RoundedDrawable(bitmap);
        setImageDrawable(drawable);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable instanceof RoundedDrawable) {
            super.setImageDrawable(drawable);
        } else {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            RoundedDrawable roundedDrawable = new RoundedDrawable(bitmap);
            super.setImageDrawable(roundedDrawable);
        }
    }
}
