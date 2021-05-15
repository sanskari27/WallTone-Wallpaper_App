package com.excessivemedia.walltone.activities;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.adapters.RelatedAdapter;
import com.excessivemedia.walltone.adapters.Related_ItemDecoration;
import com.excessivemedia.walltone.helpers.ViewAnimator;
import com.excessivemedia.walltone.helpers.LikeManager;
import com.excessivemedia.walltone.helpers.SimpleGestureListener;
import com.excessivemedia.walltone.helpers.Utils;
import com.excessivemedia.walltone.service.WallpaperChanger;
import com.excessivemedia.walltone.widgets.DownloadProgressIndicator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.app.WallpaperManager.FLAG_LOCK;

public class Showcase extends AppCompatActivity implements SimpleGestureListener.OnGestureDetected , RelatedAdapter.OnRelatedImageSelected {

    public static Object imageSource;
    public static ArrayList<DocumentSnapshot> relatedDocs;

    private SimpleGestureListener detector;

    private FrameLayout relatedLayout;
    private RecyclerView relatedRecycler;
    private ImageView showcase_iv;
    private int MAX_HEIGHT;
    private String name;

    private LikeManager likeManager ;

    private File wallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_showcase);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        likeManager= new LikeManager(this);
        initViews();


        initUI();
        loadRelated();

    }

    private void initUI() {
        if(imageSource instanceof String){
            String imageSource = (String) Showcase.imageSource;
            wallpaper = new File(imageSource);
            Bitmap bmp = BitmapFactory.decodeFile(imageSource);
            showcase_iv.setImageBitmap(bmp);
            this.name = Utils.stripExtension(wallpaper.getName());

            wallpaperStored();
            findViewById(R.id.like).setVisibility(View.GONE);
        }else if(imageSource instanceof File) {
            File imageSource = (File) Showcase.imageSource;
            Picasso.get()
                    .load(imageSource)
                    .noPlaceholder()
                    .into(showcase_iv);
            fetchWallpaper(Utils.stripExtension(imageSource.getName()));
        }else if(imageSource instanceof DocumentSnapshot){
            DocumentSnapshot doc = (DocumentSnapshot) imageSource;
            String thumbnailUri = getIntent().getStringExtra("thumbnailUri");
            if(thumbnailUri!=null){
                Picasso.get()
                        .load(thumbnailUri)
                        .into(showcase_iv);
            }
            fetchWallpaper(doc.getString("name"));

        }

    }

    private void fetchWallpaper(String nm) {
        if(this.name == null || this.name.isEmpty())
            this.name = nm;

        checkLiked();
        String pathDownloaded = isDownloaded(name);
        if(pathDownloaded !=null){
            wallpaper = new File(pathDownloaded);
            Bitmap bmp = BitmapFactory.decodeFile(pathDownloaded);
            showcase_iv.setImageBitmap(bmp);
            this.name = Utils.stripExtension(wallpaper.getName());
            wallpaperStored();
            return;
        }
        DownloadProgressIndicator progress  = new DownloadProgressIndicator(this);
        progress.setMessage("Loading...");
        progress.setIndeterminate(false);
        progress.show();
        try {
            File tempFile = File.createTempFile(name, ".jpg");
            StorageReference walls = FirebaseStorage.getInstance().getReference("Walls").child(name + ".jpg");
            FileDownloadTask file = walls.getFile(tempFile);
            file.addOnProgressListener(snapshot -> {
                long l = (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount();
                progress.setProgress((int) l);
            }).addOnSuccessListener(taskSnapshot -> {
                wallpaper = new File(tempFile.getAbsolutePath());
                Bitmap bmp = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                showcase_iv.setImageBitmap(bmp);
                progress.setProgress(100);
                progress.dismiss();
                wallpaperStored();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void checkLiked() {
        if(likeManager.isliked(name))
            ((FloatingActionButton)findViewById(R.id.like)).setImageResource(R.drawable.ic_heart_fill);
    }

    private void wallpaperStored() {
        if(isDownloaded(name)!=null){
            findViewById(R.id.downloadWallpaper).setVisibility(View.GONE);
        }
        findViewById(R.id.applyWallpaper).setOnClickListener(this::setWallpaper);
        findViewById(R.id.downloadWallpaper).setOnClickListener(this::downloadWallpaper);
        findViewById(R.id.like).setOnClickListener(this::likeWallpaper);
    }

    private void likeWallpaper(View view) {
        if(likeManager.toggleLiked(name))
            ((FloatingActionButton)findViewById(R.id.like)).setImageResource(R.drawable.ic_heart_fill);
        else
            ((FloatingActionButton)findViewById(R.id.like)).setImageResource(R.drawable.ic_heart);

    }

    private void loadRelated() {
        if(relatedDocs==null || relatedDocs.size()==0)return;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        relatedRecycler = findViewById(R.id.relatedWalls);
        relatedRecycler.setHasFixedSize(true);
        relatedRecycler.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));

        RelatedAdapter adapter = new RelatedAdapter(relatedDocs);
        relatedRecycler.addItemDecoration(new Related_ItemDecoration(displayMetrics));
        relatedRecycler.setAdapter(adapter);

        adapter.setOnRelatedSelectListener(this);

    }

    private void setWallpaper(View v) {
        Snackbar.make(v, "Setting Wallpaper... 😁", Snackbar.LENGTH_SHORT).show();
        new Handler().postDelayed(()->{
            Bitmap bmp = BitmapFactory.decodeFile(wallpaper.getAbsolutePath());
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenHeight = displayMetrics.heightPixels;
            int screenWidth = displayMetrics.widthPixels;

            bmp = WallpaperChanger.scaleCenterCrop(bmp,screenHeight,screenWidth);
            WallpaperManager instance = WallpaperManager.getInstance(Showcase.this);
            try {
                instance.setWallpaperOffsetSteps(1, 1);
                instance.suggestDesiredDimensions(screenWidth, screenHeight);
                instance.setBitmap(bmp);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    instance.setBitmap(bmp,null,true,FLAG_LOCK);
                }
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(100);
                Snackbar.make(v, "Wallpaper set 😃", Snackbar.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(Showcase.this,"Error Occurred 😖",Toast.LENGTH_SHORT).show();
            }
        },1500);
    }

    private void downloadWallpaper(View v){
        Bitmap bmp = BitmapFactory.decodeFile(wallpaper.getAbsolutePath());
        try {
            saveImage(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        showcase_iv = findViewById(R.id.showcase_iv);
        relatedLayout = findViewById(R.id.relatedLayout);
        detector = new SimpleGestureListener(this,this);
        MAX_HEIGHT = (int) (getResources().getDisplayMetrics().heightPixels*0.2);
    }

    @Override
    public void onSwipeUp() {
        TransitionManager.beginDelayedTransition(findViewById(R.id.mainLayout));
        findViewById(R.id.mainLayout).animate().scaleY(0.85f).setDuration(250).start();
        findViewById(R.id.mainLayout).animate().scaleX(0.85f).setDuration(250).start();
        ViewAnimator.toHeight(relatedLayout,MAX_HEIGHT,250);
        ViewAnimator.toHeight(relatedRecycler,MAX_HEIGHT,250);
    }
    
    @Override
    public void onSwipeDown() {
        TransitionManager.beginDelayedTransition(findViewById(R.id.mainLayout));
        findViewById(R.id.mainLayout).animate().scaleY(1f).setDuration(250).start();
        findViewById(R.id.mainLayout).animate().scaleX(1f).setDuration(250).start();
        ViewAnimator.toHeight(relatedLayout,0,250);
        ViewAnimator.toHeight(relatedRecycler,0,250);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        // Call onTouchEvent of SimpleGestureFilter class
        if(relatedDocs != null && relatedDocs.size()>0)
            this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    private void saveImage(Bitmap bitmap) throws IOException {
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Walltone" );
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + "Walltone";

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".jpg");
            fos = new FileOutputStream(image);

        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        Toast.makeText(this, "😍 Wallpaper Downloaded 😍", Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);
    }

    private String isDownloaded(String name) {
        String[] what = new String[]{ MediaStore.Images.ImageColumns.DATA };

        String where = MediaStore.Images.Media.DATA + " like ? ";
        String[] args = {"%Walltone/"+name+"%"};

        Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        what,
                        where,
                        args,
                        null);

        if (cursor.moveToNext()) {
            String path = cursor.getString(0);
               cursor.close();
               return path;
        }
        return null;
    }

    @Override
    public void onRelatedSelected(File file,String id) {
        imageSource = file;
        this.name = id;
        initUI();

    }

}

