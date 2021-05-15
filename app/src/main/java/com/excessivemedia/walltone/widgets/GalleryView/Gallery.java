package com.excessivemedia.walltone.widgets.GalleryView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.excessivemedia.walltone.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Gallery extends FrameLayout {
    private GalleryAdapter galleryAdapter;
    private ArrayList<Object> adapterList;

    public Gallery(@NonNull Context context) {
        super(context);
        init(context);
    }

    public Gallery(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View view  = LayoutInflater.from(context).inflate(R.layout.widget_gallery,this,true);
        RecyclerView galleryRecycler = view.findViewById(R.id.galleryRecycler);

        galleryRecycler.setHasFixedSize(false);
        galleryRecycler.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        galleryRecycler.setItemAnimator(null);

        adapterList = new ArrayList<>();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        galleryRecycler.addItemDecoration(new Gallery_ItemDecorator(metrics));
        galleryAdapter = new GalleryAdapter(adapterList);
        galleryRecycler.setAdapter(galleryAdapter);

    }

    public void loadWalls(List<DocumentSnapshot> documents) {
        adapterList.addAll(documents);
        galleryAdapter.notifyDataSetChanged();
    }


    public void loadWalls(DocumentSnapshot document) {
        adapterList.add(document);
        galleryAdapter.notifyDataSetChanged();
    }
    public void setGallerySelectedListener(OnGalleryImageSelected onGalleryImageSelected){
        galleryAdapter.setOnGallerySelectListener(onGalleryImageSelected);
    }


    public void clear() {
        adapterList.clear();
        galleryAdapter.notifyDataSetChanged();
    }

    public ArrayList<DocumentSnapshot> getWallDocuments() {
        ArrayList<DocumentSnapshot> snapshotList = new ArrayList<>();
        for (Object obj:adapterList){

            if(obj instanceof DocumentSnapshot){
                snapshotList.add((DocumentSnapshot)obj);
            }

        }
        return snapshotList;
    }

    public void loadWalls(ArrayList<String> listOfAllImages) {
        adapterList.addAll(listOfAllImages);
        galleryAdapter.notifyDataSetChanged();
    }
    public void loadWalls(String filePath) {
        adapterList.add(filePath);
        galleryAdapter.notifyDataSetChanged();
    }
    public ArrayList<String> getWallAsFilePath(){
        ArrayList<String> filePaths = new ArrayList<>();
        for (Object obj:adapterList){
            if(obj instanceof String){
                filePaths.add((String) obj);
            }
        }
        return filePaths;
    }
}
