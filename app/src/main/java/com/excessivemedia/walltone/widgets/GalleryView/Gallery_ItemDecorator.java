package com.excessivemedia.walltone.widgets.GalleryView;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class Gallery_ItemDecorator extends RecyclerView.ItemDecoration {

    private final DisplayMetrics dm;

    public Gallery_ItemDecorator(DisplayMetrics dm) {
        this.dm = dm;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.left = outRect.right = (int) (dm.widthPixels*0.02);
        outRect.top = outRect.bottom = (int)(dm.widthPixels*0.0125);
    }
}
