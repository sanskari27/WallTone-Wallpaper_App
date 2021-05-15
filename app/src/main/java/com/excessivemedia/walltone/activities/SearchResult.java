package com.excessivemedia.walltone.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.Consts;
import com.excessivemedia.walltone.helpers.LikeManager;
import com.excessivemedia.walltone.widgets.GalleryView.Gallery;
import com.excessivemedia.walltone.widgets.GalleryView.OnGalleryImageSelected;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchResult extends AppCompatActivity implements OnGalleryImageSelected {
    private String searchType;
    private String searchString;
    private Gallery galleryView;
    private TextView title;
    private CollectionReference walls;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_search_result);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        searchType = getIntent().getStringExtra(Consts.TYPE);
        searchString = getIntent().getStringExtra(Consts.SEARCH);
        title = findViewById(R.id.title);
        galleryView = findViewById(R.id.galleryView);
        galleryView.setGallerySelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        walls = FirebaseFirestore.getInstance().collection(Consts.WALLS);
        if(!title.getText().toString().isEmpty())return;
        galleryView.clear();
        switch (searchType) {
            case Consts.MORE:
                searchMore();
                break;
            case Consts.CATEGORY:
                searchCategory();
                break;
            case Consts.TAGS:
                searchTag();
                break;
            case Consts.DOWNLOADS:
                openDownloads();
                break;
            case Consts.LIKE:
                likedWalls();
                break;
            case Consts.COLOR:
                searchColor();
                break;
        }
    }

    private void likedWalls() {
        title.setText(R.string.favourites);
        ArrayList<String> likedImagesId =  new LikeManager(this).getLikedImagesId();
        for (String s:likedImagesId){
            walls.whereEqualTo(Consts.NAME,s)
                    .get()
                    .addOnSuccessListener(documents -> galleryView.loadWalls(documents.getDocuments()));
        }
    }

    private void openDownloads() {
        title.setText(Consts.DOWNLOADS);
        new Handler().postDelayed(()->{
            ArrayList<String> downloads = new ArrayList<>();
            String[] what = new String[]{ MediaStore.Images.ImageColumns.DATA };

            String where = MediaStore.Images.Media.DATA + " like ? ";
            String[] args = {"%Walltone%"};

            Cursor cursor = getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            what,
                            where,
                            args,
                            null);

            while (cursor.moveToNext()) {
                downloads.add(cursor.getString(0));
            }
            cursor.close();
            galleryView.loadWalls(downloads);
        },100);

    }

    private void searchTag() {
        title.setText(searchString);
        walls.whereArrayContains(Consts.TAGS,searchString).get().addOnSuccessListener(documents -> {
            List<DocumentSnapshot> list = documents.getDocuments();
            Collections.shuffle(list);
            galleryView.loadWalls(list);
        });
    }
    private void searchColor() {
        title.setText(Consts.COLOR);
        walls.whereEqualTo(Consts.COLOR_CODE,searchString).get().addOnSuccessListener(documents -> {
            List<DocumentSnapshot> list = documents.getDocuments();
            Collections.shuffle(list);
            galleryView.loadWalls(list);
            if(list.size()>0){
                title.setText(list.get(0).getString(Consts.COLOR_NAME));
            }
        });
    }

    private void searchCategory() {
        title.setText(searchString);
        walls.whereEqualTo(Consts.CATEGORY,searchString).get().addOnSuccessListener(documents -> {
            List<DocumentSnapshot> list = documents.getDocuments();
            Collections.shuffle(list);
            galleryView.loadWalls(list);
        });
    }

    private void searchMore() {
        title.setText(Consts.WALLPAPERS);
        walls.get().addOnSuccessListener(documents->{
            List<DocumentSnapshot> list = documents.getDocuments();
            Collections.shuffle(list);
            galleryView.loadWalls(list);

        });
    }

    @Override
    public void onGalleryImageSelected(DocumentSnapshot doc, Uri uri) {
        Showcase.imageSource = doc;
        Showcase.relatedDocs = galleryView.getWallDocuments();
        Intent intent = new Intent(this,Showcase.class);
        intent.putExtra("thumbnailUri",String.valueOf(uri));
        startActivity(intent);
    }

    @Override
    public void onDownloadedImageSelected(String filePath) {
        Showcase.imageSource = filePath;
        Showcase.relatedDocs = null;
        Intent intent = new Intent(this,Showcase.class);
        startActivity(intent);
    }

}