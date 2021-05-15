package com.excessivemedia.walltone.adapters;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Related_ItemDecoration extends RecyclerView.ItemDecoration{

    private final DisplayMetrics dm;

    public Related_ItemDecoration(DisplayMetrics dm) {
        this.dm = dm;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.left = outRect.right = (int) (dm.widthPixels*0.02);
        outRect.top = outRect.bottom = (int)(dm.widthPixels*0.0125);
    }
}
