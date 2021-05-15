package com.excessivemedia.walltone.widgets.ColorPicker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.excessivemedia.walltone.R;

import java.util.ArrayList;

class ColorPickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<String> colorList;
    private OnColorSelected onColorSelected;

    ColorPickerAdapter(ArrayList<String> colorList) {
        this.colorList = colorList;
    }

    public void setOnColorSelected(OnColorSelected onColorSelected) {
        this.onColorSelected = onColorSelected;
    }

    interface OnColorSelected{
        void onColorSelected(String color);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) {
            return new ColorPickerWheelVH(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.ccp_wheel_wraper,parent,false)
            );
        }
        return new ColorPickerVH(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.ccp_card,parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position)==0){
            ColorPickerWheelVH wheelVH = (ColorPickerWheelVH) holder;
            wheelVH.itemView.setOnClickListener(v->{
                if(onColorSelected !=null){
                    onColorSelected.onColorSelected(null);
                }
            });
        }else{
            String color = colorList.get(position);
            ColorPickerVH pickerVH = (ColorPickerVH) holder;
            pickerVH.colorpickerItem.setCardBackgroundColor(Color.parseColor(color));
            pickerVH.itemView.setOnClickListener(v->{
                if(onColorSelected !=null){
                    onColorSelected.onColorSelected(color);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return 0;
        }
        return 1;
    }


    @Override
    public int getItemCount() {
        return colorList.size();
    }

    static class ColorPickerVH extends RecyclerView.ViewHolder{
        private final CardView colorpickerItem;
        public ColorPickerVH(@NonNull View itemView) {
            super(itemView);
            colorpickerItem = itemView.findViewById(R.id.colorpicker_item);
        }
    }
    static class ColorPickerWheelVH extends RecyclerView.ViewHolder{
        public ColorPickerWheelVH(@NonNull View itemView) {
            super(itemView);
            (itemView.findViewById(R.id.color_wheel_icon)).setVisibility(View.VISIBLE);
        }
    }

}
