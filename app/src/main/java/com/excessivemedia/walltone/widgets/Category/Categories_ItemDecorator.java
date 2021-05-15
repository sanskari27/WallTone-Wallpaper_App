package com.excessivemedia.walltone.widgets.Category;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class Categories_ItemDecorator extends RecyclerView.ItemDecoration {

    private final DisplayMetrics dm;

    public Categories_ItemDecorator(DisplayMetrics dm) {
        this.dm = dm;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if(parent.getChildAdapterPosition(view)%2==0){
            outRect.left = (int)(dm.widthPixels*0.04);
            outRect.right = (int)(dm.widthPixels*0.0125);
        }else{
            outRect.right = (int)(dm.widthPixels*0.04);
            outRect.left = (int)(dm.widthPixels*0.0125);

        }

        outRect.top = outRect.bottom = (int)(dm.widthPixels*0.0125);
    }
}
