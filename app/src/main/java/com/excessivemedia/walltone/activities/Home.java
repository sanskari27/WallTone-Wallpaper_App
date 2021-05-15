package com.excessivemedia.walltone.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.adapters.SearchAdapter;
import com.excessivemedia.walltone.helpers.ViewAnimator;
import com.excessivemedia.walltone.widgets.Category.Categories;
import com.excessivemedia.walltone.widgets.Category.OnCategorySelectedListener;
import com.excessivemedia.walltone.widgets.ColorPicker.CustomColorPicker;
import com.excessivemedia.walltone.widgets.GalleryView.Gallery;
import com.excessivemedia.walltone.widgets.GalleryView.OnGalleryImageSelected;
import com.excessivemedia.walltone.widgets.Highlight.Highlights;
import com.excessivemedia.walltone.widgets.Highlight.OnHighlightsClickListener;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Home extends AppCompatActivity
                    implements OnHighlightsClickListener ,
                                OnCategorySelectedListener ,
                                OnGalleryImageSelected ,
                                CustomColorPicker.ColorChangeListener,
                                SearchAdapter.OnSearchClickListener{


    private static final int PERMISSION_REQUEST_CODE = 0;
    private final int RECENT_THRESHOLD = 90;
    private final int ANIMATION_TIME = 400;

    private Highlights highlightsView;

    private FloatingActionButton searchFAB;
    private ConstraintLayout searchLayout;
    private CustomColorPicker colorPicker;

    private EditText searchET;
    private ArrayList<String> searchResults;

    private Gallery galleryView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        initViews();
        initSearchView();
        initNavigation();
        initRecent();

    }

    @Override
    protected void onStart() {
        super.onStart();
        showPermissionDialog();
    }

    private void initNavigation() {
        BottomAppBar bottomNav = findViewById(R.id.bottomAppBar);
        bottomNav.setOnMenuItemClickListener(item -> {
            Intent intent;
            switch (item.getItemId()){
                case R.id.liked:
                    intent = new Intent(this,SearchResult.class);
                    intent.putExtra("type","like");
                    startActivity(intent);
                    
                    break;
                case R.id.downloaded:
                    intent = new Intent(this,SearchResult.class);
                    intent.putExtra("type","download");
                    startActivity(intent);
                    
                    break;
            }
            return false;
        });

        bottomNav.setNavigationOnClickListener(v-> {
            BottomNavigation bottomSheet = new BottomNavigation();
            bottomSheet.show(getSupportFragmentManager(),
                    BottomNavigation.class.getSimpleName());
        });

    }

    private void initViews() {
        NestedScrollView rootScrollView = findViewById(R.id.nestedScrollView);
        highlightsView = findViewById(R.id.highlights);
        highlightsView.setOnHighlightsClickListener(this);
        rootScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> highlightsView.setScrollPointer(scrollY>oldScrollY,scrollY));
        Categories categories = findViewById(R.id.categories);
        categories.setOnCategorySelectedListener(this);
        findViewById(R.id.moreCategories).setOnClickListener(v-> categories.setCount(-1));
    }

    private void initRecent(){
        galleryView = findViewById(R.id.galleryView);
        galleryView.setGallerySelectedListener(this);
        FirebaseFirestore.getInstance().collection("Walls").get().addOnSuccessListener(documents->{
            List<DocumentSnapshot> list = documents.getDocuments();
//            createTag(list);
            CustomColorPicker.extractColors(list);
            Collections.shuffle(list);
            List<DocumentSnapshot> wallList = new ArrayList<>();

            while(wallList.size()<RECENT_THRESHOLD && list.size()>0){
                int index = (int) (Math.random() * list.size());
                wallList.add(list.get(index));
                list.remove(index);
            }
            galleryView.loadWalls(wallList);

        });
        findViewById(R.id.more).setOnClickListener(v->{
            Intent intent = new Intent(this,SearchResult.class);
            intent.putExtra("type","more");
            startActivity(intent);
            
        });
        findViewById(R.id.upload).setOnClickListener(v-> {
            toggleSearchLayout(false);
            startActivity(new Intent(this, Upload.class));
        });
    }

//    private void createTag(List<DocumentSnapshot> list) {
//        Map<String,String> map = new HashMap<>();
//        for (DocumentSnapshot ds: list             ) {
//            String category = ds.getString("category");
//            List<String> tags = (List<String>) ds.get("Tags");
//            if(category == null) continue;
//            if(!map.containsKey(category)){
//                map.put(category,category);
//                FirebaseDatabase.getInstance().getReference("Tags").child(category).child("name").setValue(category);
//            }
//            if(tags == null) continue;
//            for (String tag:tags){
//                if(!map.containsKey(tag)){
//                    map.put(tag,tag);
//                    FirebaseDatabase.getInstance().getReference("Tags").child(tag).child("name").setValue(tag);
//                }
//            }
//        }
//    }

    private void initSearchView() {
        searchET = findViewById(R.id.searchET);
        RecyclerView searchRecycler = findViewById(R.id.searchResults);
        searchFAB = findViewById(R.id.searchFAB);
        searchLayout = findViewById(R.id.searchLayout);
        searchResults = new ArrayList<>();
        colorPicker = findViewById(R.id.color_picker);
        colorPicker.setEnabled(true);
        colorPicker.setOnColorChangeListener(this);
        final DatabaseReference tags = FirebaseDatabase.getInstance().getReference("Tags");
        searchRecycler.setHasFixedSize(false);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        searchRecycler.setLayoutManager(layoutManager);

        SearchAdapter searchAdapter = new SearchAdapter(searchResults);
        searchRecycler.setAdapter(searchAdapter);

        searchFAB.setOnClickListener(v-> toggleSearchLayout(searchLayout.getVisibility()!=View.VISIBLE));
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().toLowerCase();
                System.out.println(text);
                tags.orderByChild("name").startAt(text)
                        .endAt(text+"\uf8ff").get().addOnSuccessListener(snapshot -> {
                            ArrayList<String> list = new ArrayList<>();
                            for (DataSnapshot ds:snapshot.getChildren()){
                                list.add(ds.getKey());
                            }
                            searchResults.clear();
                            searchResults.addAll(list);
                            searchAdapter.notifyDataSetChanged();
                        });
            }
        });
        searchET.setText("");

        searchAdapter.setOnSearchClickListener(this);


    }

    private void showPermissionDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            View view = LayoutInflater.from(this).inflate(R.layout.layout_storage_permission,null,false);
            AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setView(view)
                    .setCancelable(false)
                    .show();
            MaterialButton proceedBtn = view.findViewById(R.id.requestPermission);
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                proceedBtn.setText(getString(R.string.open_setings));
                proceedBtn.setOnClickListener(v -> {
                    dialog.dismiss();
                    try {
                        //Open the specific App Info page:
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + "com.excessivemedia.walltone"));
                        startActivity(intent);
                    } catch ( ActivityNotFoundException e ) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivity(intent);
                    }
                });
            }else{
                proceedBtn.setText(getString(R.string.ok));
                proceedBtn.setOnClickListener(v -> {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                            , PERMISSION_REQUEST_CODE
                    );
                });

            }

        }
    }


    @Override
    public void onBackPressed() {
        if(searchLayout.getVisibility() == View.VISIBLE){
            toggleSearchLayout(false);
        }else super.onBackPressed();
    }

    @Override
    public void onHighlightSelected(File file) {
        Showcase.imageSource = file;
        Showcase.relatedDocs = null;
        Intent intent = new Intent(this,Showcase.class);
        startActivity(intent);
        
    }

    @Override
    public void onCategorySelected(String category) {
        Intent intent = new Intent(this,SearchResult.class);
        intent.putExtra("search",category);
        intent.putExtra("type","category");
        startActivity(intent);
        
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
    public void onSearchResultClickListener(String searchText) {
        Intent intent = new Intent(this,SearchResult.class);
        intent.putExtra("search",searchText);
        intent.putExtra("type","tag");
        startActivity(intent);

        new Handler().postDelayed(()-> toggleSearchLayout(false),1000);
    }

    @Override
    public void onColorChanged(String color) {
        colorPicker.closeDialog();
        Intent intent = new Intent(this,SearchResult.class);
        intent.putExtra("search",color);
        intent.putExtra("type","color");
        startActivity(intent);

        new Handler().postDelayed(()-> toggleSearchLayout(false),1000);
    }

    private void toggleSearchLayout(boolean visible) {
        if(visible) {
            searchET.setText("");
            ViewAnimator.revealView(searchFAB, searchLayout, ANIMATION_TIME);
            searchFAB.setImageResource(R.drawable.ic_close);
            changeFabBackground("#000000", "#F3584A");
            getWindow().getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }else{
            ViewAnimator.hide(searchLayout,searchFAB ,ANIMATION_TIME);
            searchFAB.setImageResource(R.drawable.ic_search);
            changeFabBackground("#F3584A","#000000");
            new Handler().postDelayed(()->{
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
            },ANIMATION_TIME);
            getWindow().getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    //this is to animate whenever search fab is clicked
    private void changeFabBackground(String from, String to) {
        int start = Color.parseColor(from);
        int end = Color.parseColor(to);
        int startRed = Color.red(start);
        int startBlue = Color.blue(start);
        int startGreen = Color.green(start);
        int endRed = Color.red(end);
        int endBlue = Color.blue(end);
        int endGreen = Color.green(end);
        new CountDownTimer(ANIMATION_TIME, 1) {
            @Override
            public void onTick(long val) {
                val = (long) ANIMATION_TIME -val;
                int red = (int) (startRed + (val * (endRed - startRed) / (long) ANIMATION_TIME));
                int blue = (int) (startBlue + (val * (endBlue - startBlue) / (long) ANIMATION_TIME));
                int green = (int) (startGreen + (val * (endGreen - startGreen) / (long) ANIMATION_TIME));
                if(searchFAB!=null)
                    searchFAB.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(red,green,blue)));
            }

            @Override
            public void onFinish() {
                if(searchFAB!=null)
                    searchFAB.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(endRed,endGreen,endBlue)));
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                showPermissionDialog();
            }
        }
    }

}