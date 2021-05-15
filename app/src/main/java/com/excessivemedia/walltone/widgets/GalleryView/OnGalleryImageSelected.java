package com.excessivemedia.walltone.widgets.GalleryView;

import android.net.Uri;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnGalleryImageSelected {

    void onGalleryImageSelected(DocumentSnapshot doc, Uri uri);
    default void onDownloadedImageSelected(String filePath){}
}
