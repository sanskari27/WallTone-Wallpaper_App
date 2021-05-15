package com.excessivemedia.walltone.widgets.Category;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.CategoryUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class Categories extends FrameLayout {

    private Categories_Adapter adapter;
    private ArrayList<File> list;

    private int count;

    public Categories(@NonNull Context context) {
        super(context);
        init(context);
    }

    public Categories(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.Categories);
        count = a.getInt(R.styleable.Categories_categoriesCount,0);
        a.recycle();
        init(context);

    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_categories,this,true);
        RecyclerView recyclerView = view.findViewById(R.id.categories_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new GridLayoutManager(
                        context,
                        2,
                        RecyclerView.VERTICAL,
                        false
                )
        );

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        recyclerView.addItemDecoration(
                new Categories_ItemDecorator(displayMetrics)
        );

        list = new ArrayList<>();
        list.addAll(CategoryUtils.getInstance().getCategoryFiles());
        updateView();
        adapter = new Categories_Adapter(list,displayMetrics);
        recyclerView.setAdapter(adapter);


    }


    private void updateView() {
        while (count<list.size()){
            int random = (int) (Math.random() * list.size());
            list.remove(random);
        }
        Collections.shuffle(list);
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }

    public void setCount(int count){
        if(this.count == count) return;

        list.clear();
        list.addAll(CategoryUtils.getInstance().getCategoryFiles());
        if(count==-1){
            this.count = list.size();
        }else
        this.count = count;
        updateView();
    }
    public void setOnCategorySelectedListener(OnCategorySelectedListener onCategorySelectedListener){
        adapter.setOnCategorySelectedListener(onCategorySelectedListener);
    }
}
