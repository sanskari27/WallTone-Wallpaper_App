package com.excessivemedia.walltone.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static android.app.WallpaperManager.FLAG_LOCK;

public class WallpaperChanger extends BroadcastReceiver {


    private static final String TAG = WallpaperChanger.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"Wallpaper Changer Triggered");
        String colorCode = context.getSharedPreferences("ColorPref",0).getString("seletedColor",null);

        CollectionReference walls = FirebaseFirestore.getInstance().collection("Walls");
        Query query;
        if(colorCode==null){
            query = walls.whereGreaterThan("name", getRandomChar());
        }else {
            query = walls.whereEqualTo("colorCode",colorCode);
        }
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
            if(documents.size()==0) return;
            DocumentSnapshot documentSnapshot = documents.get((int) (Math.random() * documents.size()));
            String name= documentSnapshot.getString("name");
            if(name == null) return;
            downloadWall(context, name);

        });
        toggleAutoWallpaper(context,true);
    }

    private void downloadWall(Context context, String name) {
        try{
            File tempFile = File.createTempFile(name, ".jpg");

            FirebaseStorage.getInstance()
                    .getReference("Walls").child(name+".jpg")
                    .getFile(tempFile)
                    .addOnSuccessListener(taskSnapshot -> setWallpaper(context, tempFile));
        }catch (IOException e){
            Log.d(TAG,e.getMessage());
        }
    }

    private void setWallpaper(Context context, File tempFile) {
        Bitmap bmp = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        bmp = scaleCenterCrop(bmp,screenHeight,screenWidth);
        WallpaperManager instance = WallpaperManager.getInstance(context);
        try {
            instance.setWallpaperOffsetSteps(1, 1);
            instance.suggestDesiredDimensions(screenWidth, screenHeight);
            instance.setBitmap(bmp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                instance.setBitmap(bmp,null,true,FLAG_LOCK);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRandomChar() {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
        return String.valueOf(AlphaNumericString.charAt((int) (AlphaNumericString.length()*Math.random())));
    }

    public static void toggleAutoWallpaper(Context context,boolean val){
        if(val) activate(context);
        else deactivate(context);
    }

    private static void activate(Context mContext){
        Intent myIntent = new Intent(mContext,WallpaperChanger.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0, myIntent,
                0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.add(Calendar.DAY_OF_YEAR,1);

        Log.wtf(TAG,String.valueOf(calendar.getTimeInMillis()-System.currentTimeMillis()));
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
   private static void deactivate(Context mContext){
       AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
       Intent myIntent = new Intent(mContext,WallpaperChanger.class);
       PendingIntent pendingIntent = PendingIntent.getBroadcast(
               mContext, 0, myIntent,
               PendingIntent.FLAG_UPDATE_CURRENT);
       pendingIntent.cancel();
       alarmManager.cancel(pendingIntent);
   }
    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

}