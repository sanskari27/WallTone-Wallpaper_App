package com.excessivemedia.walltone.widgets.Category;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;
import com.excessivemedia.walltone.helpers.CategoryUtils;
import com.excessivemedia.walltone.widgets.TransitionImageView.TransitionImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class Categories_Adapter extends RecyclerView.Adapter<Categories_Adapter.Categories_ViewHolder> {
    private final ArrayList<File> list;
    private OnCategorySelectedListener listener;
    private final int width;

    public Categories_Adapter(ArrayList<File> list, DisplayMetrics displayMetrics) {
        this.list = list;
        width = (int) (displayMetrics.widthPixels * 0.4475);
    }

    @NonNull
    @Override
    public Categories_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Categories_ViewHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_categories,parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull Categories_ViewHolder holder, int position) {
        File file = list.get(position);
        holder.itemView.setVisibility(View.GONE);
        CategoryClass category=CategoryUtils.getInstance().parseFile(file);
        if(category==null){
            return;
        }
        StorageReference imgRef = FirebaseStorage.getInstance().getReference("Walls").child("Thumbnail/"+category.getFileName()+".jpg");

        holder.name.setText(category.getCategoryName());

        File localFile = CategoryUtils.getInstance().getCategory(category.getFileName());
        if(localFile !=null){
            holder.itemView.setVisibility(View.VISIBLE);
            Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            holder.iv.setImageBitmap(bmp);

        }else{
            try {
                File temp = File.createTempFile(category.getFileName(),"jpg");
                imgRef.getFile(temp).addOnSuccessListener(taskSnapshot -> {
                    File copyFile = CategoryUtils.getInstance().copyFile(category.getFileName(), temp);
                    Bitmap bmp = BitmapFactory.decodeFile(copyFile.getAbsolutePath());
                    holder.iv.setImageBitmap(bmp);
                    holder.itemView.setVisibility(View.VISIBLE);
                }).addOnFailureListener(exception -> holder.itemView.setVisibility(View.GONE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        holder.itemView.setOnClickListener(v->{
            if(listener!=null){
                listener.onCategorySelected(category.getCategoryName());
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

   class Categories_ViewHolder extends RecyclerView.ViewHolder{
        private final TransitionImageView iv;
        private final TextView name;
        public Categories_ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.category_iv);
            name = itemView.findViewById(R.id.category_name);

            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.width = width;
            layoutParams.height = width/2;
            itemView.setLayoutParams(layoutParams);


        }
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }
}
