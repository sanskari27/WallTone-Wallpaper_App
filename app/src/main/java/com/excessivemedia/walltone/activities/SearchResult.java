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
        searchType = getIntent().getStringExtra("type");
        searchString = getIntent().getStringExtra("search");
        title = findViewById(R.id.title);
        galleryView = findViewById(R.id.galleryView);
        galleryView.setGallerySelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        walls = FirebaseFirestore.getInstance().collection("Walls");
        if(!title.getText().toString().isEmpty())return;
        galleryView.clear();
        if(searchType.equalsIgnoreCase("more")){
            searchMore();
        }else if(searchType.equalsIgnoreCase("category")){
            searchCategory();
        }else if(searchType.equalsIgnoreCase("tag")){
            searchTag();
        }else if(searchType.equalsIgnoreCase("download")){
            openDownloads();
        }else if(searchType.equalsIgnoreCase("like")){
            likedWalls();
        }else if(searchType.equalsIgnoreCase("color")){
            searchColor();
        }
    }

    private void likedWalls() {
        title.setText(R.string.favourites);
        ArrayList<String> likedImagesId =  new LikeManager(this).getLikedImagesId();
        for (String s:likedImagesId){
            walls.whereEqualTo("name",s)
                    .get()
                    .addOnSuccessListener(documents -> galleryView.loadWalls(documents.getDocuments()));
        }
    }

    private void openDownloads() {
        title.setText(R.string.downloads);
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
            title.setText(R.string.downloads);
            galleryView.loadWalls(downloads);
        },100);

    }

    private void searchTag() {
        title.setText(searchString);
        walls.whereArrayContains("Tags",searchString).get().addOnSuccessListener(documents -> {
            List<DocumentSnapshot> list = documents.getDocuments();
            Collections.shuffle(list);
            galleryView.loadWalls(list);
        });
    }
    private void searchColor() {
        title.setText(getString(R.string.color));
        walls.whereEqualTo("colorCode",searchString).get().addOnSuccessListener(documents -> {
            List<DocumentSnapshot> list = documents.getDocuments();
            Collections.shuffle(list);
            galleryView.loadWalls(list);
            if(list.size()>0){
                title.setText(list.get(0).getString("colorName"));
            }
        });
    }

    private void searchCategory() {
        title.setText(searchString);
        walls.whereEqualTo("category",searchString).get().addOnSuccessListener(documents -> {
            List<DocumentSnapshot> list = documents.getDocuments();
            Collections.shuffle(list);
            galleryView.loadWalls(list);
        });
    }

    private void searchMore() {
        title.setText(R.string.wallpapers);
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