package com.excessivemedia.walltone.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.Consts;
import com.excessivemedia.walltone.widgets.DownloadProgressIndicator;
import com.excessivemedia.walltone.widgets.GalleryView.Gallery;
import com.google.firebase.storage.FirebaseStorage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Upload extends AppCompatActivity {

    private static final int GALLERY_REQ = 1;
    private Gallery galleryView;
    private int i;
    private ArrayList<String> files;
    private DownloadProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_upload);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        galleryView = findViewById(R.id.uploadRecyclerView);
        progressIndicator = new DownloadProgressIndicator(this);
        findViewById(R.id.addFile).setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent,
                            getResources().getString(R.string.selectwallpaper)),
                    GALLERY_REQ);
        });
        findViewById(R.id.upload).setOnClickListener(this::upload);
    }

    private void upload(View v){
        i=1;
        files= galleryView.getWallAsFilePath();
        upload();
    }

    private void upload(){
        if(files.size()>0){
            progressIndicator.setMessage("Uploading "+i);
            progressIndicator.show();
            progressIndicator.setProgress(1);
            String path = files.get((int) (Math.random() * files.size()));
            files.remove(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap bmp = BitmapFactory.decodeFile(path);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            FirebaseStorage.getInstance().getReference(Consts.PENDING_WALLS)
                    .child(getAlphaNumericString()+".jpg")
                .putBytes(baos.toByteArray()).addOnProgressListener(snapshot -> {
                    long l = (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount();
                    progressIndicator.setProgress((int) l);
                }).addOnSuccessListener(taskSnapshot -> {
                    progressIndicator.setProgress(100);
                    i++;
                    upload();
                }).addOnFailureListener(e -> {
                    Toast.makeText(Upload.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    i++;
                    upload();
            });
        }else {
            progressIndicator.dismiss();
            finish();
        }
    }

    private String getAlphaNumericString() {
        int n=30;

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQ && resultCode ==RESULT_OK){
            if(data != null)
            {
                Uri selectedImage = data.getData();
                String wholeID = DocumentsContract.getDocumentId(selectedImage);
                // Split at colon, use second item in the array
                String id = wholeID.split(":")[1];
                // where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";
                String[] projection = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection, sel, new String[] { id }, null);
                if(cursor.moveToNext()) {
                    cursor.moveToFirst();
                    String picturePath = cursor.getString(0);
                    galleryView.loadWalls(picturePath);
                }
                cursor.close();


            }
        }
    }
}