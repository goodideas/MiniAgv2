package com.kevin.miniagv2.views;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class SimpleSpeedSeekBarAdapter implements SpeedSeekBarAdapter {

    protected StateListDrawable[] mItems;

    public SimpleSpeedSeekBarAdapter(Resources resources, int[] items) {
        int size = items.length;
        mItems = new StateListDrawable[size];
        Drawable drawable;
        for (int i = 0; i < size; i++) {
            drawable = resources.getDrawable(items[i]);
            if (drawable instanceof StateListDrawable) {
                mItems[i] = (StateListDrawable) drawable;
            } else {
                mItems[i] = new StateListDrawable();
                mItems[i].addState(new int[] {}, drawable);
            }
        }
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public StateListDrawable getItem(int position) {
        return mItems[position];
    }

}
