package com.excessivemedia.walltone.widgets.Highlight;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.Consts;
import com.excessivemedia.walltone.helpers.HighlightUtils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;

class HighlightsAdapter extends RecyclerView.Adapter<HighlightsAdapter.HighlightsVH>{

    private final List<String> highlightList;
    private OnHighlightsClickListener onHighlightsClickListener;

    public void setOnHighlightsClickListener(OnHighlightsClickListener onHighlightsClickListener) {
        this.onHighlightsClickListener = onHighlightsClickListener;
    }

    public HighlightsAdapter(List<String> highlightList) {
        this.highlightList = highlightList;
    }

    @NonNull
    @Override
    public HighlightsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HighlightsVH(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.card_highlights,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final HighlightsVH holder, int position) {
        position = position % highlightList.size();
        String name = highlightList.get(position);
        holder.itemView.setTag(name);
        StorageReference imgRef = FirebaseStorage.getInstance()
                .getReference(Consts.WALLS).child(Consts.THUMBNAIL).child(name+".jpg");

        File localFile = HighlightUtils.getInstance().getHighlight(name);
        if(localFile !=null){
            Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            holder.imageView.setImageBitmap(bmp);
            holder.itemView.setOnClickListener(v->{
                if(onHighlightsClickListener !=null){
                    onHighlightsClickListener.onHighlightSelected(localFile);
                }
            });
        }else{
            try {
                File temp = File.createTempFile(name,"jpg");
                imgRef.getFile(temp).addOnSuccessListener(taskSnapshot -> {
                    File copyFile = HighlightUtils.getInstance().copyFile(name, temp);
                    Bitmap bmp = BitmapFactory.decodeFile(copyFile.getAbsolutePath());
                    holder.imageView.setImageBitmap(bmp);
                    holder.itemView.setOnClickListener(v->{
                        if(onHighlightsClickListener !=null){
                            onHighlightsClickListener.onHighlightSelected(copyFile);
                        }
                    });
                }).addOnFailureListener(exception -> holder.itemView.setVisibility(View.GONE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    @Override
    public int getItemCount() {
        if(highlightList.size()==0)return 0;
        return Byte.MAX_VALUE/2;
    }

    static class HighlightsVH extends RecyclerView.ViewHolder{
        private final ImageView imageView;
        public HighlightsVH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.highlightImageView);


        }
    }

}
