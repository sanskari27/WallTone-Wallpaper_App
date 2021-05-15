package com.excessivemedia.walltone.widgets.GalleryView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.Consts;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryVH> {

    private final ArrayList<Object> list;
    private final StorageReference thumbnails;
    private OnGalleryImageSelected listener;
    public GalleryAdapter(ArrayList<Object> list) {
        this.list = list;
        thumbnails = FirebaseStorage.getInstance().getReference("Walls").child("Thumbnail");
    }

    public void setOnGallerySelectListener(OnGalleryImageSelected listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public GalleryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GalleryVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_gallery ,parent,false ));
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryVH holder,final int position) {
        Object obj = list.get(position);
        if(obj instanceof DocumentSnapshot){
            final DocumentSnapshot doc = (DocumentSnapshot) obj;
            String name = doc.getString(Consts.NAME);
            thumbnails.child(name+".jpg").getDownloadUrl().addOnSuccessListener(uri -> {
                Picasso.get().load(uri).into(holder.galleryImageView);
                holder.itemView.setOnClickListener(v->{
                    if(listener!=null){
                        listener.onGalleryImageSelected(doc,uri);
                    }
                });
            }).addOnFailureListener(e -> {
                holder.itemView.setVisibility(View.GONE);
                Log.e(GalleryAdapter.class.getSimpleName(),e.getMessage());
            });
        }else if(obj instanceof String){
            String path =(String) obj;
            Bitmap bmp = BitmapFactory.decodeFile(path);
            holder.galleryImageView.setImageBitmap(bmp);
            if(bmp==null){
                holder.itemView.setVisibility(View.GONE);
            }else {
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDownloadedImageSelected(path);
                    }
                });
            }

        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class GalleryVH extends RecyclerView.ViewHolder{
        ImageView galleryImageView;
        public GalleryVH(@NonNull View itemView) {
            super(itemView);
            galleryImageView = itemView.findViewById(R.id.galleryImageView);
        }
    }
}
