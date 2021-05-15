package com.excessivemedia.walltone.adapters;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.Consts;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RelatedAdapter extends RecyclerView.Adapter<RelatedAdapter.RelatedVH> {
    private final ArrayList<DocumentSnapshot> list;
    private final StorageReference thumbnails;
    private OnRelatedImageSelected listener;

    public RelatedAdapter(ArrayList<DocumentSnapshot> relatedDocs) {
        list =relatedDocs;
        thumbnails = FirebaseStorage.getInstance().getReference(Consts.WALLS).child(Consts.THUMBNAIL);
    }

    public void setOnRelatedSelectListener(OnRelatedImageSelected listener) {
        this.listener = listener;
    }

    public interface OnRelatedImageSelected{
        void onRelatedSelected(File file,String id);
    }

    @NonNull
    @Override
    public RelatedVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RelatedVH(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_related
                                ,parent,
                                false )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedVH holder, int position) {
        final DocumentSnapshot doc = list.get(position);
        String name = doc.getString("name");
        if(name == null || name.isEmpty()){
            holder.itemView.setVisibility(View.GONE);
            return;
        }
        try {
            File tempFile = File.createTempFile(name, ".jpg");
            thumbnails.child(name + ".jpg").getFile(tempFile).addOnSuccessListener(taskSnapshot -> {
                Picasso.get()
                        .load(tempFile)
                        .noPlaceholder()
                        .into(holder.relatedIV);
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRelatedSelected(tempFile,name );
                    }
                });
            }).addOnFailureListener(e -> {
                holder.itemView.setVisibility(View.GONE);
                Log.e(RelatedAdapter.class.getSimpleName(), e.getMessage());
            });
        }catch (IOException e){
            holder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(10,list.size());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class RelatedVH extends RecyclerView.ViewHolder{
        ImageView relatedIV;
        public RelatedVH(@NonNull View itemView) {
            super(itemView);
            relatedIV=itemView.findViewById(R.id.relatedImageView);
        }
    }
}
